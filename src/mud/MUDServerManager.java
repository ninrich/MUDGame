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
    // TODO: make this some player class
    private HashSet<String> connectedClients = new HashSet<String>();
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

    private void createMUDGame() {

        try {
            String name;

            System.out.println("Please enter new server name:");
            name = input.readLine();
            this.MUDGames.put(name, new MUDGame(name));
            this.menuMessage = name + " was created successfully!\n";
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String clientMenu() {
        return getAllServers() +
                "\nWhat would you like to do?\n" +
                "Create [N]ew Server\n" +
                "[C]onnect\n" +
                "[E]xit\n";
    }

    public String clientChoice(String choice) {
        switch (choice.toLowerCase()) {
            case "n":
            case "c":
            case "e":
        }
        return null;
    }

    public String clientConnected(String clientName) {
        System.out.println(clientName + " has connected");
        this.connectedClients.add(clientName);
        return clientName + ", welcome to the MUDGame server manager!";
    }


    public String getAllServers() {
        StringBuilder serverList = new StringBuilder("Here is the list of currently running servers:");
        for (String s : this.MUDGames.keySet()) {
            serverList.append('\n').append(s);
        }
        return serverList.toString();
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
        String msg = this.menuMessage;
        this.menuMessage = "";
        return msg + getAllServers() + "\n" +
                "\n[C]reate a new game" +
                "\n[D]elete a game" +
                "\n[V]iew connected users" +
                "\n[K]ick user" +
                "\n[S]hutdown";
    }

    private void serverAction(String choice) {
        switch (choice.toLowerCase()) {
            case "c" : this.createMUDGame(); break;
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
        for (String client : this.connectedClients) {
            connectedClients.add(client);
        }
        return connectedClients.toString();
    }

    private void shutdown() {
        System.out.println("Server Manager shutting down, goodbye.");
        this.running = false;
    }


}
