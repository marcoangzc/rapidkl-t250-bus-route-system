package com.rapidkl.t250;

/**
 * DepthFirstSearch - CONCRETE traversal algorithm (extends GraphTraversal).
 *
 * DFS explores as far as possible along each branch before backtracking,
 * following the order in which neighbours were added to each stop. The
 * "memory" of the path back is the method call stack itself - recursion
 * replaces an explicit stack.
 */
public class DepthFirstSearch extends GraphTraversal {

    public DepthFirstSearch(GraphADT graph) {
        super(graph);
    }

    @Override
    public String getName() {
        return "DFS";
    }

    @Override
    protected void traverseFrom(String startName) {
        dfsVisit(startName);
    }

    /**
     * Recursive core. The stop is marked visited BEFORE recursing, so on a
     * cyclic route (T250 is a loop!) no stop can ever be entered twice and
     * the recursion always terminates.
     */
    private void dfsVisit(String current) {
        visit(current);
        for (RouteSegment seg : graph.getNeighbours(current)) {
            String neighbour = seg.getDestination();
            if (!visited.contains(neighbour)) {
                dfsVisit(neighbour);
            }
        }
    }
}
