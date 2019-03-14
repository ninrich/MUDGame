package mud;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

class MUDGame implements MUDServerInterface {
    private MUD MUDMap;
    private String name;
    private List<Player> players = new ArrayList<>();

    Boolean addPlayer(Player player) {
        // here I could implement max players in a game
        this.players.add(player);
        return true;
    }

    String getName() {
        return this.name;
    }

    private MUD createMap() {
        return new MUD("mymud.edg","mymud.msg","mymud.thg");
    }

    MUDGame(String serverName) {
        this.name = serverName;
        this.MUDMap = this.createMap();
    }
}
