package mud;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class MUDServerManager implements MUDServerManagerInterface {
    private static BufferedReader input = new BufferedReader( new InputStreamReader( System.in ));

    private Integer MUDGamesLimit = 2;
    private String menuMessage = "";
    private Map<String, MUDGame> MUDGames = new HashMap<>();
    private Map<String, String> connectedPlayers = new HashMap<>();
    private Boolean running;

    private MUDServerManager(){}

    private  Boolean register() {
        try {
            String url;
            String hostname;
            MUDServerManagerInterface stub;

            System.setProperty("java.security.policy", "mud.policy");
            System.setSecurityManager(new SecurityManager());
            hostname = (InetAddress.getLocalHost()).getCanonicalHostName();
            stub = (MUDServerManagerInterface) UnicastRemoteObject.exportObject(this, serverManagerPort);
            url = "rmi://" + hostname + ":" + rmiPortNumber + "/ServerManager";
            Naming.rebind(url, stub);
            this.running = true;
            this.menuMessage = "Server Manager registered successfully at " + url + "\n";

            FileWriter urlFile = new FileWriter("./url.txt");
            urlFile.write(url);
            urlFile.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void serverCreateGame() {
        try {
            String name;
            String maxPlayers;

            System.out.println("Please enter the name of the new game:");
            name = input.readLine();
            System.out.println("Please enter the maximum number of players:");
            maxPlayers = input.readLine();

            this.createGame("Admin", name, Integer.valueOf(maxPlayers));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String serverMenu() {
        return  "-_-_-_-_-_-_-_-_-_-_-_-_\n" +
                (this.menuMessage.isEmpty() ? "" : ("New messages:\n" + this.menuMessage))
                + getAllGames() + "\n" +
                "\n[C]reate a new game" +
                "\n[E]dit maximum number of MUDGames" +
                "\n[V]iew connected users" +
                "\n[K]ick user" +
                "\n[S]hutdown";
    }

    private void serverAction(String choice) {
        this.menuMessage = "";
        switch (choice.toLowerCase()) {
            case "c": this.serverCreateGame(); break;
            case "e": this.changeNumberOfMUDGames(); break;
            case "k": this.kickUser();
            case "v": this.displayUsers(); break;
            case "s": this.shutdown(); break;
        }
    }

    private void changeNumberOfMUDGames() {
        Integer limit = MUDGamesLimit;
        System.out.println("Enter the new MUDGames limit:");
        try {
            limit = Integer.valueOf(input.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.MUDGamesLimit = limit;
        this.menuMessage += "Maximum number of game istances is now set to " + limit+ ".\n";
        System.out.println(serverMenu());
    }

    private void kickUser() {
        String userName;

        System.out.println(this.getConnectedUsers());
        System.out.println("Enter the name of the user to kick:");
        try {
            userName = input.readLine();
            this.playerDisconnectGame(userName);
            this.connectedPlayers.put(userName, kickedMessage);
            this.menuMessage += userName + " was kickedMessage.\n";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayUsers() {
        this.menuMessage += "Currently connected users are:\n" + this.getConnectedUsers();
    }

    private String getConnectedUsers() {
        String currentServer;
        StringJoiner connectedClients = new StringJoiner("\n","","\n");

        for (String userName : this.connectedPlayers.keySet()) {
            currentServer = this.connectedPlayers.get(userName);

            if (! (currentServer.isEmpty() || currentServer.equals(kickedMessage))) {
                connectedClients.add(userName + " - " + currentServer);
            } else {
                connectedClients.add(userName);
            }
        }
        return connectedClients.toString();
    }

    private MUDGame getPlayersGame(String playerName) {
        String currentGameName = this.connectedPlayers.get(playerName);
        return this.MUDGames.get(currentGameName);
    }

    private String playersInSameGameAs(String playerName) {
        MUDGame game = this.getPlayersGame(playerName);
        return "Players currently in " + game.getName() + ":\n" + game.getCurrentPlayersList();
    }

    private void shutdown() {
        System.out.println("Server Manager shutting down, goodbye.");
        this.running = false;
    }

    public String createGame(String playerName, String name, Integer maxPlayersConnected) {
        if (this.MUDGames.size() < this.MUDGamesLimit) {
            this.MUDGames.put(name, new MUDGame(name,maxPlayersConnected));
            this.menuMessage += playerName + " created game " + name + ".\n";
            System.out.println(this.serverMenu());
            return gameCreationMessage;
        } else {
            this.menuMessage += playerName + " tried to create " + name +". Maximum limit of active games has been reached.\n";
            System.out.println(this.serverMenu());
        }
        return gameCreationError;
    }

    public synchronized String joinGame(String playerName, String gameName) {
        MUDGame game = this.MUDGames.get(gameName);
        String info;

        info = game.addPlayer(playerName);
        if (info.equals(maxPlayersError))
            return maxPlayersError;
        this.connectedPlayers.put(playerName, gameName);

        this.menuMessage += playerName + " has joined " + gameName + ".\n";
        System.out.println(this.serverMenu());
        return info;
    }

    public String playerAction(String playerName, String action) {
        if (this.connectedPlayers.get(playerName).equals(kickedMessage))
            return kickedMessage;
        switch (action.toLowerCase()) {
            case "e": return this.playerExitServer(playerName);
            case "d": return this.playerDisconnectGame(playerName);
            case "v": return this.getConnectedUsers();
            case "s": return this.playersInSameGameAs(playerName);
            default:
                MUDGame game = this.getPlayersGame(playerName);
                return game.playerAction(playerName, action);
        }
    }

    public String playerConnect(String clientName) {
        this.connectedPlayers.put(clientName, "");
        this.menuMessage += clientName + " has connected.\n";
        System.out.println(this.serverMenu());
        return welcomeMessage;
    }

    public String playerExitServer(String clientName) {
        if (this.connectedPlayers.containsKey(clientName)) {
            MUDGame currentGame = this.getPlayersGame(clientName);

            this.connectedPlayers.remove(clientName);
            try {
                this.menuMessage += currentGame.removePlayer(clientName);
            } catch (NullPointerException ignored) {}
            this.menuMessage += clientName + " has left the server.\n";
            System.out.println(this.serverMenu());
        }
        return exitMessage;
    }

    public String playerDisconnectGame(String playerName) {
        MUDGame currentGame = this.getPlayersGame(playerName);

        this.connectedPlayers.put(playerName, "");
        this.menuMessage += currentGame.removePlayer(playerName);
        System.out.println(this.serverMenu());

        return disconnectedMessage;
    }

    public String getAllGames() {
        if (!this.MUDGames.isEmpty()) {
            MUDGame game;
            String gameName;
            Integer connectedPlayers;
            Integer maxPlayers;
            StringBuilder gameList = new StringBuilder("\nHere is the list of currently running games:");

            for (Map.Entry<String, MUDGame> entry : this.MUDGames.entrySet()) {
                game = entry.getValue();
                gameName = game.getName();
                connectedPlayers = game.getPlayersConnectedCount();
                maxPlayers = game.getMaxPlayersConnected();

                gameList.append("\n").append(gameName).append(" - ").append(connectedPlayers).append("/").append(maxPlayers);
            }

            return gameList.toString();
        } else {
            return "";
        }
    }

    public static void main(String[] args) throws IOException {
        String choice;
        MUDServerManager server = new MUDServerManager();

        LocateRegistry.createRegistry(rmiPortNumber);

        if (!server.register()){
            System.out.println("Failed to register the Server Manager.");
            return;
        }

        server.createGame("Admin", "first", 2);

        while (server.running) {
            System.out.println(server.serverMenu());
            choice = input.readLine();
            server.serverAction(choice);
        }

        System.exit(0);
    }
}
