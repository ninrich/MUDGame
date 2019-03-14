package mud;

import java.rmi.Remote;
import java.rmi.RemoteException;

interface MUDInterface extends Remote {
    void createMUD(String MUDName) throws RemoteException;

}
