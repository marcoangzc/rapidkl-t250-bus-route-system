package com.rapidkl.t250;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
 * GOAL STATE: traverse(start, goal) turns the traversal into a classic
 * graph SEARCH - the algorithm stops as soon as the goal stop is reached,
 * and the route that led there can be rebuilt from the parent links.
 * Calling traverse(start) without a goal simply visits the whole
 * connected component.
 */
public abstract class GraphTraversal {

    /** The network being traversed (programmed against the interface). */
    protected final GraphADT graph;

    /** Stops already reached - prevents infinite loops on the cyclic route. */
    protected final Set<String> visited = new HashSet<>();

    /** The visit order produced by the last traversal. */
    protected final List<String> order = new ArrayList<>();

    /** parent[child] = the stop it was reached from (rebuilds the path). */
    protected final Map<String, String> parent = new HashMap<>();

    /** Destination stop of a goal-directed search; null = plain traversal. */
    protected String goal = null;

    protected GraphTraversal(GraphADT graph) {
        this.graph = graph;
    }

    /** Algorithm label used by the console output ("DFS" / "BFS"). */
    public abstract String getName();

    /** Plain full traversal of the connected component containing startName. */
    public final List<String> traverse(String startName) {
        return traverse(startName, null);
    }

    /**
     * TEMPLATE METHOD - the fixed skeleton of every traversal.
     *
     * @param startName stop where the search begins
     * @param goalName  destination stop (goal state), or null/"" for a
     *                  full traversal without early termination
     */
    public final List<String> traverse(String startName, String goalName) {
        order.clear();
        visited.clear();
        parent.clear();
        goal = (goalName == null || goalName.isEmpty()) ? null : goalName;

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

    /** Records how a stop was reached, so the found path can be rebuilt. */
    protected void recordParent(String child, String reachedFrom) {
        parent.put(child, reachedFrom);
    }

    /** True when a goal was set AND actually reached by the last traverse(). */
    public boolean isGoalReached() {
        return goal != null && visited.contains(goal);
    }

    /** Number of stops explored by the last traverse() call. */
    public int getVisitCount() {
        return order.size();
    }

    /** The goal stop of the last search (null when none was given). */
    public String getGoal() {
        return goal;
    }

    /**
     * The path from the start stop to the goal, rebuilt by walking the
     * parent links backwards. Empty unless a goal was set and reached.
     */
    public List<String> getGoalPath() {
        List<String> path = new ArrayList<>();
        if (goal == null || !visited.contains(goal)) {
            return path;
        }
        String current = goal;
        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }
        Collections.reverse(path);
        return path;
    }
}
