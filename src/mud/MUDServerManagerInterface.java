package mud;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MUDServerManagerInterface extends Remote {
    int rmiPortNumber = 50010;
    int serverManagerPort = 50011;

    String getAllServers() throws RemoteException;
    String clientConnected(String clientName) throws RemoteException;
    String clientMenu() throws RemoteException;
    String clientChoice(String choice) throws RemoteException;
}
