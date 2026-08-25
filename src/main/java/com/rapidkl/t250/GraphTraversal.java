package com.rapidkl.t250;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GraphTraversal - the ABSTRACT parent of every traversal algorithm.
 *
 * DFS and BFS share the same skeleton: validate the starting stop, reset
 * the shared state, run the algorithm core, return the visit order. Only
 * the CORE differs (recursion vs queue). That skeleton lives here as a
 * TEMPLATE METHOD (final, so subclasses cannot break it), while the core
 * is declared abstract and implemented by the subclasses:
 *
 *      GraphTraversal (abstract)          <- this class
 *          |-- DepthFirstSearch           <- recursive core
 *          |-- BreadthFirstSearch         <- queue-based core
 *
 * This is why the project needs an abstract class: it holds the state
 * (visited set, order list) and the common code that both algorithms reuse.
 */
public abstract class GraphTraversal {

    /** The network being traversed (programmed against the interface). */
    protected final GraphADT graph;

    /** Stops already reached - prevents infinite loops on the cyclic route. */
    protected final Set<String> visited = new HashSet<>();

    /** The visit order produced by the last traversal. */
    protected final List<String> order = new ArrayList<>();

    protected GraphTraversal(GraphADT graph) {
        this.graph = graph;
    }

    /** Algorithm label used by the console output ("DFS" / "BFS"). */
    public abstract String getName();

    /**
     * TEMPLATE METHOD - the fixed skeleton of every traversal.
     * Subclasses only supply the algorithm core, never the boilerplate.
     */
    public final List<String> traverse(String startName) {
        order.clear();
        visited.clear();
        if (!graph.containsStop(startName)) {
            return order;                        // unknown start -> empty result
        }
        traverseFrom(startName);
        return order;
    }

    /** The algorithm core: implemented with recursion in DFS, a queue in BFS. */
    protected abstract void traverseFrom(String startName);

    /** Marks one stop as reached and appends it to the visit order. */
    protected void visit(String stopName) {
        visited.add(stopName);
        order.add(stopName);
    }
}
