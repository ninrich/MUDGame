package mud;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MUDServerManagerInterface extends Remote {
    int rmiPortNumber = 50010;
    int serverManagerPort = 50011;

    String welcomeMessage = "You have successfully connected to the server.";
    String gameCreationMessage = "The game was created successfully.";
    String gameCreationError = "Maximum number of game slots reached.";
    String maxPlayersError = "The server is full.";
    String kickedMessage = "kickedMessage";
    String disconnectedMessage = "disconnected";
    String exitMessage = "exit successful";

    String getAllGames() throws RemoteException;
    String createGame(String creatorName, String name, Integer maxPlayersConnected) throws RemoteException;
    String joinGame(String playerName, String gameName) throws RemoteException;
    String playerAction(String playerName, String action) throws RemoteException;
    String playerConnect(String clientName) throws RemoteException;
    String playerDisconnectGame(String playerName) throws RemoteException;
    String playerExitServer(String clientName) throws RemoteException;
}
