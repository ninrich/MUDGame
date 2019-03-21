package mud; /***********************************************************************
 * cs3524.solutions.mud.mud.Edge
 ***********************************************************************/

// Represents an path in the mud.MUD (an edge in a graph).
class Edge
{
    Vertex _dest;   // Your destination if you walk down this path
    String _view;   // What you see if you look down this path
    
    Edge(Vertex destination, String view)
    {
        _dest = destination;
	    _view = view;
    }
}

