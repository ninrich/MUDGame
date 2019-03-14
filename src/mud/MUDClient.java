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
    private Boolean connected = true;


    private Boolean connectToServerManager() {
        try {
            this.service = (MUDServerManagerInterface) Naming.lookup(serverManagerURL);
            String welcome_message = this.service.clientConnected(this.name);
            System.out.println(welcome_message);

            return true;
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


    private MUDClient() {}

    public static void main(String[] args) throws IOException {
        MUDClient client = new MUDClient();
        client.setName();
        if (!client.connectToServerManager()){
            System.out.println("Could not connect to the Server Manager");
            return;
        }
        while (client.connected){
            System.out.println(client.service.clientMenu());
            String choice = input.readLine();
            client.service.clientChoice(choice);
        }
    }
}
