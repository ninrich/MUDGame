package mud;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

class MUDGame implements MUDServerInterface {
    private MUD MUDMap;
    private String name;



    private MUD createMap() {
        return new MUD("mymud.edg","mymud.msg","mymud.thg");
    }

    MUDGame(String serverName) {
        this.name = serverName;
        this.MUDMap = this.createMap();
    }
}
