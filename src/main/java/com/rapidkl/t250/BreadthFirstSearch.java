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
            for (RouteSegment seg : graph.getNeighbours(current)) {
                String neighbour = seg.getDestination();
                if (!visited.contains(neighbour)) {
                    visit(neighbour);
                    queue.addLast(neighbour);
                }
            }
        }
    }
}
