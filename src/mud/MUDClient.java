package mud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import static mud.MUDServerManagerInterface.*;

public class MUDClient {
    private static BufferedReader input = new BufferedReader( new InputStreamReader( System.in ));

    private MUDServerManagerInterface service;

    private String name;
    private String menuMessage = "";
    private Boolean connected = true;
    private String gameInfo;
    private String action;
    private Boolean isInGame;


    private Boolean connectToServerManager() {
        try {
            String serverManagerURL = Files.readString(Paths.get("./url.txt"));

            this.service = (MUDServerManagerInterface) Naming.lookup(serverManagerURL);
            if (this.service.playerConnect(this.name).equals(welcomeMessage)) {
                System.out.println("You have successfully connected to the MUDGame server manager!");
                return true;
            } else {
                System.out.println("Could not connect to the MUDGame server manager!");
                return false;
            }

        } catch (IOException | NotBoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setName() {
        try {
            System.out.println("Enter new client name:");
            this.name = input.readLine();
        }
        catch (IOException e) {
            setName();
        }
    }

    private String clientMenu()  {
        return  "-_-_-_-_-_-_-_-_-_-_-_-_\n" +
                (this.menuMessage.isEmpty() ? "" : ("New messages:\n" + this.menuMessage)) +
                "\nWhat would you like to do?" +
                "\n[J]oin game" +
                "\n[C]reate new game" +
                "\n[E]xit";
    }

    private void clientAction(String choice) throws IOException {
        this.menuMessage = "";
        switch (choice.toLowerCase()) {
            case "j": this.chooseGameToJoin(); break;
            case "c": this.clientCreateGame(); break;
            case "e": this.exit();
        }
    }

    private void exit() {
        try {
            this.service.playerExitServer(this.name);
            System.exit(0);
        } catch (RemoteException e) {
            System.exit(0);
        }
    }

    private void clientCreateGame() {
        try {
            String name;
            String maxPlayers;

            System.out.println("Please enter the name of the new game:");
            name = input.readLine();
            System.out.println("Please enter the maximum number of players:");
            maxPlayers = input.readLine();

            if (this.service.createGame(this.name, name, Integer.valueOf(maxPlayers)).equals(gameCreationMessage)) {
                this.menuMessage += name + " was created successfully!\n";
            } else {
                this.menuMessage += "Error creating " + name +". Server has reached maximum number of concurrent games.\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chooseGameToJoin() throws IOException {
        String activeGames = this.service.getAllGames();
        String choice;

        if (activeGames.isEmpty()) {
            this.menuMessage += "No game is currently running.\n";
            return;
        }

        System.out.println(activeGames);
        System.out.println("Write the name of the game you wish to join:");
        choice = input.readLine();
        this.gameInfo = this.service.joinGame(this.name, choice);

        if (this.gameInfo.equals(maxPlayersError)) {
            System.out.println("Can't log into " + choice + " because it is full. " +
                    "You will be connected once a spot becomes available...");
            while ((this.gameInfo = this.service.joinGame(this.name, choice)).equals(maxPlayersError)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
        this.isInGame = true;
        this.inGame();

    }

    private void inGame() {
        //display in game menus and shit
        while (this.isInGame) {
            this.printGameInfo();
            this.recordCommand();
            this.sendCommand();
            this.analyseInfo();
        }
    }

    private void analyseInfo() {
        switch (this.gameInfo) {
            case kickedMessage: {
                this.isInGame = false;
                this.menuMessage += "You have been kickedMessage from the game.\n";
                break;
            }
            case exitMessage: {
                System.out.println("Shutting down, goodbye.");
                System.exit(0);
                break;
            }
            case disconnectedMessage: {
                this.isInGame = false;
                this.menuMessage += "You have disconnectedMessage from the game.\n";
                break;
            }
        }
    }

    private void sendCommand()  {
        try {
            this.gameInfo = this.service.playerAction(this.name, this.action);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void recordCommand() {
        System.out.println("What would you like to do?");
        try {
            this.action = (input.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printGameInfo() {
        System.out.println("-_-_-_-_-_-_-_-_-_-_-_-_");
        System.out.println(this.gameInfo);
        System.out.println(
                "\n[move] + direction" +
                "\n[take] + item" +
                "\n[place] + item\n" +

                "\n[V]iew all players" +
                "\n[S]how players in this game\n"+

                "\n[D]isconnect from game" +
                "\n[E]xit server");
    }

    private MUDClient() {}

    public static void main(String[] args) throws IOException {
        MUDClient client = new MUDClient();
        client.setName();
        if (!client.connectToServerManager()){
            System.out.println("Could not connect to the Server Manager");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(client::exit));

        while (client.connected){
            System.out.println(client.clientMenu());
            String choice = input.readLine();
            client.clientAction(choice);
        }
    }
}
