package com.rapidkl.t250;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * BusRouteGraph - the CONCRETE implementation of the {@link GraphADT}
 * interface: the core graph data structure of the Rapid KL Bus Route System.
 *
 * The T250 route network is modelled as an UNDIRECTED, WEIGHTED graph
 * implemented with an ADJACENCY LIST:
 *
 *      vertex  =  Stop          (a bus stop)
 *      edge    =  RouteSegment  (a direct road link between two stops,
 *                                 weighted by approximate distance in km)
 *
 * Why adjacency list? A bus route network is a SPARSE graph - each stop is
 * directly connected to only a few neighbouring stops. The list needs only
 * O(V + E) memory, while an adjacency matrix would waste O(V^2) space on
 * entries that are always empty. Traversing the neighbours of a stop is also
 * faster: O(degree) instead of scanning a whole matrix row of V cells.
 *
 * LinkedHashMap is used so that vertices keep their insertion order. This
 * makes the console display and the DFS/BFS visit order stable and easy to
 * verify during testing.
 */
public class BusRouteGraph implements GraphADT {

    // Result codes (RESULT_OK, ERR_...) are declared in the GraphADT
    // interface and inherited by this implementing class.

    /** All vertices (bus stops), keyed by their unique name. */
    private final Map<String, Stop> stops = new LinkedHashMap<>();

    /**
     * Adjacency list: for every stop name, the list of direct RouteSegments.
     * LinkedList is used so that removing all segments of a deleted stop is
     * efficient and simple.
     */
    private final Map<String, LinkedList<RouteSegment>> adjacency = new LinkedHashMap<>();

    // =====================================================================
    //  Vertex (bus stop) operations
    // =====================================================================

    /**
     * Adds a bus stop (vertex) to the network.
     * @return RESULT_OK, or ERR_STOP_EXISTS if the name is already used.
     */
    public int addStop(String name, String code, boolean hub) {
        if (stops.containsKey(name)) {
            return ERR_STOP_EXISTS;
        }
        stops.put(name, new Stop(name, code, hub));
        adjacency.put(name, new LinkedList<>());
        return RESULT_OK;
    }

    /**
     * Removes a bus stop together with ALL segments connected to it.
     * Data integrity rule: no dangling edge may point to a removed stop,
     * so the segment is erased from every neighbour's list as well.
     *
     * @return -1 if the stop does not exist; otherwise the number of
     *         segments that were removed together with it.
     */
    public int removeStop(String name) {
        if (!stops.containsKey(name)) {
            return -1;
        }
        int removedSegments = 0;
        for (Map.Entry<String, LinkedList<RouteSegment>> entry : adjacency.entrySet()) {
            if (!entry.getKey().equals(name)) {
                int before = entry.getValue().size();
                entry.getValue().removeIf(seg -> seg.getDestination().equals(name));
                removedSegments += before - entry.getValue().size();
            }
        }
        adjacency.remove(name);
        stops.remove(name);
        return removedSegments;
    }

    /** Checks whether a stop with this exact name exists in the network. */
    public boolean containsStop(String name) {
        return stops.containsKey(name);
    }

    /** Removes ALL stops and segments, returning the network to an empty state. */
    public void clear() {
        stops.clear();
        adjacency.clear();
    }

    // =====================================================================
    //  Edge (route segment) operations
    // =====================================================================

    /**
     * Adds an undirected route segment between two existing stops.
     * @return RESULT_OK, ERR_STOP_NOT_FOUND, ERR_SELF_LOOP or ERR_SEGMENT_EXISTS.
     */
    public int addSegment(String stopA, String stopB, double distanceKm) {
        if (!stops.containsKey(stopA) || !stops.containsKey(stopB)) {
            return ERR_STOP_NOT_FOUND;
        }
        if (stopA.equals(stopB)) {
            return ERR_SELF_LOOP;               // a bus cannot loop onto itself
        }
        if (hasDirectSegment(stopA, stopB)) {
            return ERR_SEGMENT_EXISTS;
        }
        adjacency.get(stopA).add(new RouteSegment(stopB, distanceKm));
        adjacency.get(stopB).add(new RouteSegment(stopA, distanceKm));
        return RESULT_OK;
    }

    /**
     * Removes the direct segment between two stops from BOTH endpoints.
     * @return RESULT_OK, ERR_STOP_NOT_FOUND or ERR_SEGMENT_MISSING.
     */
    public int removeSegment(String stopA, String stopB) {
        if (!stops.containsKey(stopA) || !stops.containsKey(stopB)) {
            return ERR_STOP_NOT_FOUND;
        }
        boolean removedFromA = adjacency.get(stopA).removeIf(
                seg -> seg.getDestination().equals(stopB));
        boolean removedFromB = adjacency.get(stopB).removeIf(
                seg -> seg.getDestination().equals(stopA));
        if (removedFromA && removedFromB) {
            return RESULT_OK;
        }
        return ERR_SEGMENT_MISSING;
    }

    /** True if a direct segment exists between the two named stops. */
    public boolean hasDirectSegment(String stopA, String stopB) {
        if (!stops.containsKey(stopA)) {
            return false;
        }
        for (RouteSegment seg : adjacency.get(stopA)) {
            if (seg.getDestination().equals(stopB)) {
                return true;
            }
        }
        return false;
    }

    /** Returns the direct segments (neighbour links) of one stop. */
    public List<RouteSegment> getNeighbours(String stopName) {
        List<RouteSegment> result = new ArrayList<>();
        LinkedList<RouteSegment> list = adjacency.get(stopName);
        if (list != null) {
            result.addAll(list);
        }
        return result;
    }

    // =====================================================================
    //  Queries used by the console UI
    // =====================================================================

    public boolean isEmpty() {
        return stops.isEmpty();
    }

    public int getStopCount() {
        return stops.size();
    }

    /** Number of undirected segments = sum of all adjacency sizes / 2. */
    public int getSegmentCount() {
        int halfEdges = 0;
        for (LinkedList<RouteSegment> list : adjacency.values()) {
            halfEdges += list.size();
        }
        return halfEdges / 2;
    }

    /** Total length of all route segments combined, in km. */
    public double getTotalDistanceKm() {
        double total = 0;
        for (LinkedList<RouteSegment> list : adjacency.values()) {
            for (RouteSegment seg : list) {
                total += seg.getDistanceKm();
            }
        }
        return total / 2;                       // every segment was counted twice
    }

    public Stop getStop(String name) {
        return stops.get(name);
    }

    /** Names of all stops in insertion order. */
    public List<String> getStopNames() {
        return new ArrayList<>(stops.keySet());
    }

    /**
     * Case-insensitive lookup. Returns the exact stored name of the stop,
     * or null when no such stop exists. Keeps the UI forgiving about
     * capitalisation without allowing ambiguous duplicates.
     */
    public String resolveStopName(String userInput) {
        String query = userInput.trim();
        for (String name : stops.keySet()) {
            if (name.equalsIgnoreCase(query)) {
                return name;
            }
        }
        return null;
    }

    /** "Did you mean..." support: all stops whose name contains the text. */
    public List<String> suggestStops(String partialName) {
        String query = partialName.trim().toLowerCase();
        List<String> matches = new ArrayList<>();
        for (String name : stops.keySet()) {
            if (name.toLowerCase().contains(query)) {
                matches.add(name);
            }
        }
        return matches;
    }

    /**
     * Translates a graph operation result code into a readable message,
     * so the same wording is reused everywhere in the console UI.
     */
    @Override
    public String describeResult(int resultCode, String stopA, String stopB) {
        switch (resultCode) {
            case RESULT_OK:           return "";
            case ERR_STOP_NOT_FOUND:  return "One or both of the bus stops do not exist in the network.";
            case ERR_STOP_EXISTS:     return "The bus stop \"" + stopA + "\" already exists in the network.";
            case ERR_SEGMENT_EXISTS:  return "There is already a direct route between \""
                                              + stopA + "\" and \"" + stopB + "\".";
            case ERR_SEGMENT_MISSING: return "There is NO direct route between \""
                                              + stopA + "\" and \"" + stopB + "\".";
            case ERR_SELF_LOOP:       return "Invalid: the two stops must be different.";
            default:                  return "Unknown operation result.";
        }
    }

    // =====================================================================
    //  Graph traversal algorithms
    // =====================================================================

    /**
     * BREADTH FIRST SEARCH - delegates to the BreadthFirstSearch concrete
     * class (queue-based, visits nearest stops first, level by level).
     * @param startName stop where the traversal begins
     * @return the visit order; empty list if the start stop does not exist
     */
    @Override
    public List<String> breadthFirstSearch(String startName) {
        return new BreadthFirstSearch(this).traverse(startName);
    }

    // =====================================================================
    //  Text display of the whole structure (adjacency list view)
    // =====================================================================

    /**
     * Builds the full adjacency-list listing shown in the console and used
     * for quick manual verification of every graph operation.
     */
    public String formatAdjacencyList() {
        StringBuilder sb = new StringBuilder();
        for (Stop stop : stops.values()) {
            sb.append(String.format("  %-34s", stop.getName()));
            sb.append("[").append(stop.getCode()).append("]");
            if (stop.isHub()) {
                sb.append("  *HUB*");
            }
            sb.append("\n");

            List<RouteSegment> neighbours = getNeighbours(stop.getName());
            if (neighbours.isEmpty()) {
                sb.append("      (no direct route - isolated stop)\n");
            } else {
                for (RouteSegment seg : neighbours) {
                    sb.append(String.format("      --> %-32s %.1f km%n",
                            seg.getDestination(), seg.getDistanceKm()));
                }
            }
        }
        return sb.toString();
    }
}
