package com.rapidkl.t250;

import java.util.ArrayDeque;

/**
 * BreadthFirstSearch - CONCRETE traversal algorithm (extends GraphTraversal).
 *
 * BFS visits stops level by level: first the start, then every stop one
 * segment away, then two segments away, and so on - nearest stops first.
 * The FIFO queue (ArrayDeque) is what produces this level-by-level order.
 */
public class BreadthFirstSearch extends GraphTraversal {

    public BreadthFirstSearch(GraphADT graph) {
        super(graph);
    }

    @Override
    public String getName() {
        return "BFS";
    }

    @Override
    protected void traverseFrom(String startName) {
        ArrayDeque<String> queue = new ArrayDeque<>();

        visit(startName);            // mark at ENQUEUE time so nothing can
        queue.addLast(startName);    // enter the queue twice

        while (!queue.isEmpty()) {
            String current = queue.pollFirst();

            // Goal check at dequeue time. Because BFS expands level by
            // level, the FIRST time the goal comes out of the queue it is
            // via a route with the FEWEST segments - this is exactly why
            // BFS is used for shortest-path search on unweighted graphs.
            if (goal != null && current.equals(goal)) {
                return;
            }

            for (RouteSegment seg : graph.getNeighbours(current)) {
                String neighbour = seg.getDestination();
                if (!visited.contains(neighbour)) {
                    recordParent(neighbour, current);
                    visit(neighbour);

                    if (goal != null && neighbour.equals(goal)) {
                        return;      // found while expanding: cannot get shorter
                    }
                    queue.addLast(neighbour);
                }
            }
        }
    }
}
