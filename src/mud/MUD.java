package mud;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A class that can be used to represent a mud.MUD; essentially, this is a
 * graph.
 */

public class MUD
{

    // A record of all the vertices in the mud.MUD graph. HashMaps are not
    //  but we don't really need this to be synchronised.
    private Map<String,Vertex> vertexMap = new HashMap<String,Vertex>();
    private String _startLocation = "";

    /**
     * Add a new edge to the graph.
     */
    private void addEdge( String sourceName, String destName, String direction, String view )
    {
        Vertex v = getOrCreateVertex( sourceName );
        Vertex w = getOrCreateVertex( destName );
        v._routes.put( direction, new Edge( w, view ) );
    }

    /**
     * Change the message associated with a location.
     */
    private void changeMessage( String loc, String msg )
    {
		Vertex v = getOrCreateVertex( loc );
		v._msg = msg;
    }

    /**
     * If vertexName is not present, add it to vertexMap.  In either
     * case, return the mud.Vertex. Used only for creating the mud.MUD.
     */
    private Vertex getOrCreateVertex( String vertexName )
    {
        Vertex v = vertexMap.get( vertexName );
        if (v == null) {
            v = new Vertex( vertexName );
            vertexMap.put( vertexName, v );
        }
        return v;
    }

    /**
     *
     */
    private Vertex getVertex( String vertexName )
    {
		return vertexMap.get( vertexName );
    }

    /**
     * Creates the edges of the graph on the basis of a file with the
     * following fromat:
     * source direction destination message
     */
    private void create_edges(String edgesfile ) {
		try {
			FileReader fin = new FileReader( edgesfile );
			BufferedReader edges = new BufferedReader( fin );
			String line;
			while((line = edges.readLine()) != null) {
				StringTokenizer st = new StringTokenizer( line );
				if( st.countTokens( ) < 3 ) {
					System.err.println( "Skipping ill-formatted line " + line );
					continue;
				}
				String source = st.nextToken();
				String dir    = st.nextToken();
				String dest   = st.nextToken();
				StringBuilder msg = new StringBuilder();
				while (st.hasMoreTokens()) {
					msg.append(st.nextToken()).append(" ");
				}
				addEdge( source, dest, dir, msg.toString());
			}
		}

		catch( IOException e ) {
			System.err.println( "Graph.create_edges( String " + edgesfile + ")\n" + e.getMessage() );
		}
    }

    /**
     * Records the messages assocated with vertices in the graph on
     * the basis of a file with the following format:
     * location message
     * The first location is assumed to be the starting point for
     * users joining the mud.MUD.
     */

	private void record_messages(String messagesfile ) {
		try {
			FileReader fin = new FileReader( messagesfile );
			BufferedReader messages = new BufferedReader( fin );
			String line;
			boolean first = true; // For recording the start location.
			while( (line = messages.readLine()) != null) {
				StringTokenizer st = new StringTokenizer( line );
				if( st.countTokens( ) < 2 ) {
					System.err.println( "Skipping ill-formatted line " + line );
					continue;
				}
				String location = st.nextToken();
				StringBuilder msg = new StringBuilder();
				while (st.hasMoreTokens()) {
					msg.append(st.nextToken()).append(" ");
				}
				changeMessage( location, msg.toString());
				if (first) {      // Record the start location.
					_startLocation = location;
					first = false;
				}
			}
		}
		catch( IOException e ) {
			System.err.println( "Graph.record_messages( String " +
					messagesfile + ")\n" + e.getMessage() );
		}
    }

    /**
     * Records the things associated with vertices in the graph on
     * the basis of a file with the following format:
     * location thing1 thing2 ...
     */
    private void record_things(String thingsfile ) {
		try {
			FileReader fin = new FileReader( thingsfile );
			BufferedReader things = new BufferedReader( fin );
			String line;
			while((line = things.readLine()) != null) {
				StringTokenizer st = new StringTokenizer( line );
				if( st.countTokens( ) < 2 ) {
					System.err.println( "Skipping ill-formatted line " + line );
					continue;
				}
				String loc = st.nextToken();
				while (st.hasMoreTokens()) {
					addThing( loc, st.nextToken());
				}
			}
		}
		catch( IOException e ) {
			System.err.println( "Graph.record_things( String " + thingsfile + ")\n" + e.getMessage() );
		}
    }

    /*
      All the public stuff. These methods are designed to hide the
      internal structure of the mud.MUD. Could declare these on an
      interface and have external objects interact with the mud.MUD via
      the interface.
     */

    /**
     * A constructor that creates the mud.MUD.
     */
	public MUD(String edges_file, String messages_file, String things_file)
    {
		create_edges(edges_file);
		record_messages( messages_file );
		record_things( things_file );
    }

    /** This method enables us to display the entire MUD (mostly used
    	for testing purposes so that we can check that the structure
    	defined has been successfully parsed.
	 */
    public String toString() {
		StringBuilder summary = new StringBuilder();
		Iterator iter = vertexMap.keySet().iterator();
		String loc;
		while (iter.hasNext()) {
			loc = (String)iter.next();
			summary.append("Node: ").append(loc);
			summary.append(vertexMap.get(loc).toString());
		}
		summary.append("Start location = ").append(_startLocation);
		return summary.toString();
    }

    /**
     * A method to provide a string describing a particular location.
     */
    public String locationInfo( String loc ) {
    	return getVertex( loc ).toString();
    }

    /**
     * Get the start location for new mud.MUD users.
     */
    public String startLocation() {
    	return _startLocation;
    }

    /**
     * Add a thing to a location; used to enable us to add new users.
     */
    void addThing( String loc, String thing )
    {
		Vertex v = getVertex( loc );
		v._things.add( thing );
    }

	/**
	 * Add a player to the starting location.
	 */
    void addPlayer( String playerName) {
    	Vertex v = getVertex( startLocation() );
    	v._players.add( playerName );
	}

    /**
     * Remove a thing from a location.
     */
	Boolean deleteThing(String loc, String thing)
    {
		Vertex v = getVertex( loc );
		return v._things.remove( thing );
    }

	/**
	 * Remove a player from a location.
	 */
	Boolean deletePlayer(String loc, String player)
	{
		Vertex v = getVertex( loc );
		return v._players.remove( player );
	}

    /**
     * A method to enable a player to move through the mud.MUD (a player
     * is a thing). Checks that there is a route to travel on. Returns
     * the location moved to.
     */
	String movePlayer(String location, String direction, String playerName)
    {
		Vertex v = getVertex( location );
		Edge e = v._routes.get( direction );
		if (e == null)   		// if there is no route in that direction
			return location;  	// no move is made; return current location.
			v._players.remove( playerName );
			e._dest._players.add( playerName );
		return e._dest._name;
    }

    /**
     * A main method that can be used to testing purposes to ensure
     * that the mud.MUD is specified correctly.
     */
    public static void main( String[] args)
    {
		if (args.length != 3) {
			System.err.println("Usage: java Graph <edgesfile> <messagesfile> <thingsfile>");
			return;
		}
		MUD m = new MUD( args[0], args[1], args[2] );
		System.out.println( m.toString() );
    }
}
