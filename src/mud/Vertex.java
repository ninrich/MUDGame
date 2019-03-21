package mud;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;

// Represents a location in the mud.MUD (a vertex in the graph).
class Vertex
{
    String _name;             	// mud.Vertex name
    String _msg = "";         	// Message about this location
    Map<String, Edge> _routes; 	// Association between direction (e.g. "north") and a path (mud.Edge)
	List<String> _things;	    // The things (e.g. players) at this location
	List<String> _players;		// The players at this location

    Vertex(String name)
    {
		_name = name;
		_routes = new HashMap<String, Edge>(); // Not synchronised
		_things = new Vector<String>();       // Synchronised
		_players = new Vector<String>();
    }

    public String toString()
    {
		StringBuilder summary = new StringBuilder("\n" + _msg + "\n");
		Iterator iter = _routes.keySet().iterator();
		String direction;

		while (iter.hasNext()) {
			direction = (String)iter.next();
			summary.append("To the ").append(direction).append(" there is ").append(_routes.get(direction)._view).append("\n");
		}

		iter = _things.iterator();
		if (iter.hasNext()) {
			summary.append("\nItems here: ");
			do {
				summary.append(iter.next()).append(" ");
			} while (iter.hasNext());
		}

		iter = _players.iterator();
		if (iter.hasNext()) {
			summary.append("\nPlayers here: ");
			do {
				summary.append(iter.next()).append(" ");
			} while (iter.hasNext());
		}

		summary.append("\n\n");
		return summary.toString();
    }

}

