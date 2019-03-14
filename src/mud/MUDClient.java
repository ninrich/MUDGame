package mud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class MUDClient {
    private static final String serverManagerURL = "rmi://DESKTOP-427ICI9.lan:50010/ServerManager";
    private static BufferedReader input = new BufferedReader( new InputStreamReader( System.in ));

    private MUDServerManagerInterface service;

    private String name;
    private String menuMessage = "";
    private Boolean connected = true;


    private Boolean connectToServerManager() {
        try {
            this.service = (MUDServerManagerInterface) Naming.lookup(serverManagerURL);
            if (this.service.clientConnected(this.name)) {
                System.out.println("You have successfully connected to the MUDGame server manager!");
                return true;
            } else {
                System.out.println("Could not connect to the MUDGame server manager!");
                return false;
            }

        } catch (RemoteException | NotBoundException | MalformedURLException e) {
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
        return  this.menuMessage +
                "\nWhat would you like to do?" +
                "\n[J]oin game" +
                "\n[C]reate new game" +
                "\n[E]xit";
    }

    private void clientChoice(String choice) throws IOException {
        switch (choice.toLowerCase()) {
            case "j": this.chooseGameToJoin(); break;
            case "c": this.clientCreateGame(); break;
            case "e":
        }
    }

    private void clientCreateGame() throws IOException {
        try {
            String name;

            System.out.println("Please enter the name of the new game:");
            name = input.readLine();
            if (this.service.createGame(this.name, name)) {
                this.menuMessage = name + " was created successfully!\n";
            } else {
                this.menuMessage = "Error creating game " + name;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chooseGameToJoin() throws IOException {
        this.service.getAllGames();
        System.out.println("Write the name of the server you wish to join");
        String choice = input.readLine();
        if (this.service.joinGame(this.name, choice)) {
            System.out.println("You have successfully joined " + choice);
            this.inGame();
        } else {
            System.out.println("Failed to join " + choice);
        }

    }

    private Boolean inGame() {
        //display in game menus and shit
        return true;
    }

    private MUDClient() {}

    public static void main(String[] args) throws IOException {
        MUDClient client = new MUDClient();
        client.setName();
        if (!client.connectToServerManager()){
            System.out.println("Could not connect to the Server Manager");
            return;
        }
        while (client.connected){
            System.out.println(client.clientMenu());
            String choice = input.readLine();
            client.clientChoice(choice);
        }
    }
}
