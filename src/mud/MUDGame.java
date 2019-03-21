package mud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static mud.MUDServerManagerInterface.maxPlayersError;

class MUDGame implements MUDServerInterface {
    private MUD MUDMap;
    private Integer playersConnected;
    private final Integer maxPlayersConnected;
    private String name;
    private String playerStartLocation;
    private Map<String, ArrayList<String>> playerItems = new HashMap<>();
    private Map<String, String> playerLocations = new HashMap<>();
    private Map<String, ArrayList<String>> craftingFormulas = new HashMap<>();

    String addPlayer(String playerName) {
        if (this.playersConnected.equals(this.maxPlayersConnected))
            return maxPlayersError;
        ++this.playersConnected;
        this.playerItems.put(playerName, new ArrayList<>());
        this.playerLocations.put(playerName, this.playerStartLocation);
        this.MUDMap.addPlayer(playerName);

        return this.playerInfo(playerName, "You have successfully joined " + this.getName());
    }

    String removePlayer(String playerName) {
        String playerLocation = this.getPlayerLocation(playerName);
        ArrayList<String> playerInventory = this.playerItems.get(playerName);

        for (String item : playerInventory)
            this.placeItem(playerName, item);
        this.playerItems.remove(playerName);

        this.MUDMap.deletePlayer(playerLocation, playerName);
        this.playerLocations.remove(playerName);
        --this.playersConnected;
        return playerName + " left " + this.getName() + ".\n";
    }

    private String getPlayerLocation(String playerName) {
        return this.playerLocations.get(playerName);
    }

    String getName() {
        return this.name;
    }

    private void createMap() {
        this.MUDMap = new MUD("mymud.edg","mymud.msg","mymud.thg");
        this.playerStartLocation = this.MUDMap.startLocation();
    }

    MUDGame(String serverName, Integer maxPlayersConnected) {
        this.name = serverName;
        this.playersConnected = 0;
        this.createCraftingFormulas();
        this.createMap();
        this.maxPlayersConnected = maxPlayersConnected;
    }

    private void createCraftingFormulas() {
        // could read from a file
        ArrayList<String> shovel = new ArrayList<>();
        shovel.add("scrap");
        shovel.add("stick");
        shovel.add("wire");
        this.craftingFormulas.put("shovel", shovel);
    }

    private String getPlayerItems(String playerName) {
        StringJoiner playerItems = new StringJoiner(", ","","");
        for (String s : this.playerItems.get(playerName))
            playerItems.add(s);
        return playerItems.toString();
    }

    private String playerInfo(String playerName, String customMessage) {
        StringBuilder info = new StringBuilder();
        String playerLocation = this.playerLocations.get(playerName);

        info.append(this.MUDMap.locationInfo(playerLocation));

        info.append(customMessage);

        if( !getPlayerItems(playerName).isEmpty()) {
            info.append("\n\nYour items: ");
            info.append(getPlayerItems(playerName));
        }
        return info.toString();
    }

    String playerAction(String playerName, String action) {
        String[] parsedAction = action.split("\\s+");
        String message = "";
        String playerCurrentLocation = this.playerLocations.get(playerName);

        switch (parsedAction[0]) {
            case "take":
                String itemToTake = parsedAction[1];
                if (this.MUDMap.deleteThing(playerCurrentLocation, itemToTake)) {
                    this.playerItems.get(playerName).add(itemToTake);
                    message = "You added " + itemToTake + " to your inventory.";
                } else {
                    message = "There is no " + itemToTake + " to take at your location!";
                }
                break;

            case "place":
                String itemToPlace = parsedAction[1];
                message = this.placeItem(playerName, itemToPlace);
                break;

            case "craft":
                String itemToCraft = parsedAction[1];
                message =  this.CraftItem(playerName, itemToCraft);
                break;

            case "move":
                String playerDirection = parsedAction[1];
                // new location might be the starting location if playerDirection does not exist.
                String newLocation = this.MUDMap.movePlayer(playerCurrentLocation, playerDirection, playerName);
                this.playerLocations.put(playerName, newLocation);
                if (newLocation.equals(playerCurrentLocation)) {
                    message = "There is no path " + playerDirection + " from here. You stay at your previous location.";
                } else {
                    message = "You moved to " + newLocation + ".";
                }
                break;
        }

        return playerInfo(playerName, message);
    }

    private String placeItem(String playerName, String itemToPlace) {
        String message;
        String playerCurrentLocation = this.playerLocations.get(playerName);
        ArrayList playerInventory = this.playerItems.get(playerName);

        if (playerInventory.contains(itemToPlace)) {
            playerInventory.remove(itemToPlace);
            this.MUDMap.addThing(playerCurrentLocation, itemToPlace);
            message = "You have placed " + itemToPlace + "here.";
        } else {
            message = "You are trying to place " + itemToPlace + ", however it is not in your inventory!";
        }
        return message;
    }

    private String CraftItem(String playerName, String itemToCraft) {
        ArrayList<String> playerInventory = this.playerItems.get(playerName);
        ArrayList<String> itemsNeeded = this.craftingFormulas.get(itemToCraft);

        for (String requiredItem: itemsNeeded ){
            if (!playerInventory.contains(requiredItem))
                return "You don't have the required items to craft " + itemToCraft + "!";
        }

        for (String requiredItem: itemsNeeded){
            playerInventory.remove(requiredItem);
        }

        playerInventory.add(itemToCraft);
        return itemToCraft + " was crafted successfully!";
    }

    Integer getPlayersConnectedCount() {
        return playersConnected;
    }

    Integer getMaxPlayersConnected() {
        return maxPlayersConnected;
    }

    String getCurrentPlayersList() {
        StringJoiner connectedClients = new StringJoiner(", ","","\n");

        for (String playerName : this.playerItems.keySet()) {
            connectedClients.add(playerName);
        }
        return connectedClients.toString();
    }
}
