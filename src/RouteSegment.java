/**
 * RouteSegment - represents an EDGE in the bus route graph.
 *
 * A segment is a direct road link between two neighbouring bus stops,
 * weighted by the approximate road distance in km.
 *
 * The route network is modelled as an UNDIRECTED graph: if a bus can travel
 * from stop A to stop B, it can also travel back from B to A. Therefore every
 * segment is stored once on EACH of its two endpoints in the adjacency list.
 */
public class RouteSegment {

    private final String destination;   // name of the stop this segment leads to
    private final double distanceKm;    // approximate road distance of the segment

    public RouteSegment(String destination, double distanceKm) {
        this.destination = destination;
        this.distanceKm = distanceKm;
    }

    public String getDestination() {
        return destination;
    }

    public double getDistanceKm() {
        return distanceKm;
    }
}
