package com.rapidkl.t250;

import java.util.List;

/**
 * GraphADT - the INTERFACE (contract) of the bus route graph.
 *
 * The interface declares WHAT a route network must be able to do
 * (vertex operations, edge operations, queries, traversals) without
 * saying HOW it is done. The HOW lives in the implementing class
 * {@link BusRouteGraph}, which stores the graph as an adjacency list.
 *
 * Main, GraphView and the traversal classes all program against this
 * interface, so the storage structure could be replaced (e.g. by an
 * adjacency matrix) without touching any of them.
 */
public interface GraphADT {

    // =================================================================
    //  Result codes shared by every implementation. Interface constants
    //  are implicitly public static final.
    // =================================================================
    int RESULT_OK           = 0;
    int ERR_STOP_NOT_FOUND  = 1;
    int ERR_STOP_EXISTS     = 2;
    int ERR_SEGMENT_EXISTS  = 3;
    int ERR_SEGMENT_MISSING = 4;
    int ERR_SELF_LOOP       = 5;

    // =================================================================
    //  Vertex (bus stop) operations
    // =================================================================

    /** Adds a bus stop; ERR_STOP_EXISTS if the name is already used. */
    int addStop(String name, String code, boolean hub);

    /**
     * Removes a stop together with ALL its segments (no dangling edges).
     * @return -1 if the stop does not exist, otherwise the number of
     *         segments removed with it.
     */
    int removeStop(String name);

    /** True if a stop with this exact name exists. */
    boolean containsStop(String name);

    /** Removes ALL stops and segments. */
    void clear();

    // =================================================================
    //  Edge (route segment) operations
    // =================================================================

    /** Adds an undirected weighted segment between two existing stops. */
    int addSegment(String stopA, String stopB, double distanceKm);

    /** Removes the segment from BOTH endpoints. */
    int removeSegment(String stopA, String stopB);

    /** True if a direct segment exists between the two stops. */
    boolean hasDirectSegment(String stopA, String stopB);

    /** All direct segments (neighbour links) of one stop. */
    List<RouteSegment> getNeighbours(String stopName);

    // =================================================================
    //  Queries used by the console UI
    // =================================================================

    boolean isEmpty();
    int getStopCount();
    int getSegmentCount();
    double getTotalDistanceKm();
    Stop getStop(String name);
    List<String> getStopNames();

    /** Case-insensitive lookup; returns the exact stored name or null. */
    String resolveStopName(String userInput);

    /** All stops whose name contains the given text ("did you mean..."). */
    List<String> suggestStops(String partialName);

    // =================================================================
    //  Traversal algorithms
    // =================================================================

    /** Depth First Search visit order from a starting stop. */
    List<String> depthFirstSearch(String startName);

    /** Breadth First Search visit order from a starting stop. */
    List<String> breadthFirstSearch(String startName);

    // =================================================================
    //  Display helpers
    // =================================================================

    /** The full adjacency list as printable text. */
    String formatAdjacencyList();

    /** Translates a result code into a friendly message. */
    String describeResult(int resultCode, String stopA, String stopB);
}
