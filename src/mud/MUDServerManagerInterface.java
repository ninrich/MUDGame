package mud;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MUDServerManagerInterface extends Remote {
    int rmiPortNumber = 50010;
    int serverManagerPort = 50011;

    String getAllGames() throws RemoteException;
    Boolean clientConnected(String clientName) throws RemoteException;
    Boolean joinGame(String playerName, String gameName) throws RemoteException;
    Boolean createGame(String creatorName, String name) throws RemoteException;
    }
