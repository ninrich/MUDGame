package mud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class MUDServerManager implements MUDServerManagerInterface {
    private static BufferedReader input = new BufferedReader( new InputStreamReader( System.in ));

    private String url;
    private String menuMessage = "";
    private Map<String, MUDGame> MUDGames = new HashMap<String, MUDGame>();
    private Map<String, Player> connectedPlayers = new HashMap<String, Player>();
    private Boolean running;


    private MUDServerManager(){}

    private  Boolean register() {
        try {
            String hostname = (InetAddress.getLocalHost()).getCanonicalHostName();
            System.setProperty("java.security.policy", "mud.policy");
            System.setSecurityManager(new SecurityManager());
            MUDServerManagerInterface stub = (MUDServerManagerInterface) UnicastRemoteObject.exportObject(this, serverManagerPort);
            this.url = "rmi://" + hostname + ":" + rmiPortNumber + "/ServerManager";
            Naming.rebind(this.url, stub);
            this.running = true;
            this.menuMessage = "Server Manager registered successfully at " + this.url + "\n";
            return true;
        } catch (RemoteException | UnknownHostException | MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void serverCreateGame() {
        try {
            String name;

            System.out.println("Please enter the name of the new game:");
            name = input.readLine();
            if (this.createGame(name)) {
                this.menuMessage = name + " was created successfully!\n";
            } else {
                this.menuMessage = "Error creating game " + name;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Boolean createGame(String name) {
        this.MUDGames.put(name, new MUDGame(name));
        return true;
    }

    public Boolean createGame(String creatorName, String name) {
        this.MUDGames.put(name, new MUDGame(name));
        this.menuMessage += creatorName + " created game " + name + '\n';
        System.out.println(this.serverMenu());
        return true;
    }

    public Boolean joinGame(String playerName, String gameName) {
        MUDGame game = this.MUDGames.get(gameName);
        Player player = this.connectedPlayers.get(playerName);
        if (game.addPlayer(player)) {
            player.connectToMUDGame(game);
            System.out.println(player.getName() + " has joined " + gameName);
            return true;
        } else {
            System.out.println(player.getName() + " unsuccessfully tried to connect to " + gameName);
            return false;
        }
    }



    public Boolean clientConnected(String clientName) {
        Player newPlayer = new Player(clientName);
        this.connectedPlayers.put(clientName, newPlayer);
        this.menuMessage += clientName + " has connected\n";
        System.out.println(this.serverMenu());
        return true;
    }


    public String getAllGames() {
        if (!this.MUDGames.isEmpty()) {
            StringBuilder gameList = new StringBuilder("Here is the list of currently running games:");
            for (String s : this.MUDGames.keySet()) {
                gameList.append('\n').append(s);
            }
            return gameList.toString();
        } else {
            return "No game is currently running.";
        }
    }


    public static void main(String[] args) throws IOException {
        String choice;
        MUDServerManager server = new MUDServerManager();

        if (!server.register()){
            System.out.println("Failed to register the Server Manager");
            return;
        }

        while (server.running) {
            System.out.println(server.serverMenu());
            choice = input.readLine();
            server.serverAction(choice);
        }
    }

    private String serverMenu() {
        return this.menuMessage + getAllGames() + "\n" +
                "\n[C]reate a new game" +
                "\n[D]elete a game" +
                "\n[V]iew connected users" +
                "\n[K]ick user" +
                "\n[S]hutdown";
    }

    private void serverAction(String choice) {
        this.menuMessage = "";
        switch (choice.toLowerCase()) {
            case "c" : this.serverCreateGame(); break;
            case "d": break;
            case "k": break;
            case "v": this.displayUsers(); break;
            case "s": this.shutdown(); break;
        }
    }

    private void displayUsers() {
        System.out.println("Currently connected users are:\n" + this.getConnectedUsers());
    }

    private String getConnectedUsers() {
        StringJoiner connectedClients = new StringJoiner(", ","","\n");
        for (String s : this.connectedPlayers.keySet()) connectedClients.add(s);
        return connectedClients.toString();
    }

    private void shutdown() {
        System.out.println("Server Manager shutting down, goodbye.");
        this.running = false;
    }


}
