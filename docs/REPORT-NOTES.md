# Report Notes — AMCS2034 Assignment Material

> 素材用途：直接改写进 Google Doc 报告模板的对应章节（英文报告，故正文用英文）。
> 覆盖评分表：#6 Introduction (5%)、#7 Pseudocode Design (10%)、
> #8 Algorithm Analysis (10%)。

---

## 1. Introduction (as per proposal)

### 1a. Application Selected

**Rapid KL Bus Route System — Feeder Route T250**
(LRT Wangsa Maju ⇄ Setapak Sentral, Kuala Lumpur)

### 1b. Description of the Application

Rapid KL feeder bus T250 runs as a loop service connecting LRT Wangsa Maju
station to Setapak Sentral through TAR UMT, Taman Danau Kota and Wangsa Maju
Section 2, covering about 13.9 km with 26 distinct stop locations. Our system
models this route network as an **undirected weighted graph**: every bus stop
is a **vertex** and every direct road link between two neighbouring stops is a
**weighted edge** whose weight is the approximate road distance in kilometres.

The program lets a user build and modify the network interactively from a
console menu, search for any bus stop, run **Depth First Search (DFS)** or
**Breadth First Search (BFS)** from any starting stop, and finally display the
whole network in a **JavaFX graphical window** where visited stops and the
travelled path are highlighted after every traversal.

### 1c. Functions of the Program

| # | Function | Menu |
|---|----------|------|
| 1 | Auto-load the full real T250 data set (26 stops / 28 segments ≈ 13.1 km) at startup | automatic |
| 2 | Add a bus stop (vertex) with name, official code and hub flag; duplicates rejected | Create Graph → 1 |
| 3 | Remove a bus stop together with ALL its incident segments (no dangling edges remain) | Create Graph → 2 |
| 4 | Add a route segment (edge) between two existing stops with distance weight; self-loops and duplicate edges rejected | Create Graph → 3 |
| 5 | Remove a route segment from both endpoints | Create Graph → 4 |
| 6 | Display the complete network as an adjacency list | Create Graph → 5 |
| 7 | Clear the whole network (to demonstrate building a graph from scratch) | Create Graph → 6 |
| 8 | DFS goal-directed search: start stop -> destination stop; terminates as soon as the goal is reached and reports the discovered route, its segment count and total km | Search → 1 |
| 9 | BFS goal-directed search with the same interface; guarantees the fewest-segments route on the unweighted view. Leaving the goal empty switches either algorithm into full-component traversal mode | Search → 2 |
| 10 | JavaFX map window: vertices, weighted edges, legend; traversal overlay with numbered badges and highlighted path | View Network |

### 1d. Graph Representation Description

The graph is implemented with an **adjacency list** using
`LinkedHashMap<String, LinkedList<RouteSegment>>`:

- Each key is the unique name of a `Stop` (the vertex).
- Each value is a `LinkedList` of `RouteSegment` objects (the edges), where a
  segment stores its destination stop name and distance.
- Because the route is undirected and buses travel both ways, every edge is
  inserted into **both** endpoint lists.

**Why adjacency list instead of adjacency matrix?** A bus route network is a
* sparse* graph — each stop connects directly to only about 2–3 neighbours, so
E ≈ V. The adjacency list needs **O(V + E)** memory while a V×V matrix would
need **O(V²)** cells, almost all of them empty (for 26 stops: ~700 wasted
entries). Neighbour scanning during traversal also costs O(degree) per vertex,
not O(V).

**Object-oriented structure.** The graph is exposed through the `GraphADT`
**interface** (the contract: vertex/edge operations, queries and traversals),
and `BusRouteGraph` is the **concrete** adjacency-list implementation — the
console UI is programmed against the interface, not the implementation. The
traversal algorithms share an **abstract** parent `GraphTraversal` (template
method pattern): its final `traverse()` method holds the common skeleton
(validate the start stop, reset the visited set and order list), while the
abstract `traverseFrom()` core is implemented by the `DepthFirstSearch`
(recursion) and `BreadthFirstSearch` (queue) subclasses.

### 1e. Assumptions

1. Inbound and outbound stops sharing a location (e.g. both sides of TAR UMT
   Main Gate) are merged into ONE vertex.
2. Edge weights are approximate distances estimated so the loop totals
   ≈ 13.1 km against the published 13.9 km route length.
3. Buses can travel both directions on every link, hence the undirected model;
   each undirected edge is stored twice (once per endpoint).
4. The network may be temporarily disconnected when a user removes stops;
   traversals then report how many stops were unreachable.
5. The default network is pre-loaded at startup so the system opens with the
   real T250 route; "Clear the whole network" (Create Graph → 6) empties it
   when a demonstration of building a graph from scratch is needed.
6. With the tutor's approval, extra test edges outside the published T250
   alignment may be added through Create Graph to demonstrate branching and
   to compare DFS/BFS routes between arbitrary stops.

---

## 2. Pseudocode

### 2a. Graph Operations

```
ALGORITHM AddStop(graph, name, code, hub)
    IF graph contains name THEN
        RETURN "stop already exists"
    END IF
    graph.stops[name] ← new Stop(name, code, hub)
    graph.adjacency[name] ← empty list
    RETURN success
END ALGORITHM
// Time: O(1) average (hash-map insert)

ALGORITHM RemoveStop(graph, name)
    IF name not in graph THEN RETURN error
    removedEdges ← 0
    FOR EACH stop s IN graph EXCEPT name DO
        delete every segment e in graph.adjacency[s] WHERE e.destination = name
        removedEdges ← removedEdges + number deleted
    END FOR
    delete graph.adjacency[name]
    delete graph.stops[name]
    RETURN removedEdges          // keeps integrity: no dangling edges
END ALGORITHM
// Time: O(V + E)

ALGORITHM AddSegment(graph, stopA, stopB, km)
    IF stopA or stopB missing THEN RETURN error
    IF stopA = stopB THEN RETURN error              // no self-loop
    IF edge(stopA, stopB) already exists THEN RETURN error
    append Segment(stopB, km) to adjacency[stopA]   // undirected:
    append Segment(stopA, km) to adjacency[stopB]   // stored on BOTH ends
    RETURN success
END ALGORITHM
// Time: O(deg(A) + deg(B)) ≤ O(E)

ALGORITHM RemoveSegment(graph, stopA, stopB)
    IF either stop missing THEN RETURN error
    deletedA ← remove segment to stopB from adjacency[stopA]
    deletedB ← remove segment to stopA from adjacency[stopB]
    IF deletedA AND deletedB THEN RETURN success ELSE RETURN error
END ALGORITHM
// Time: O(deg(A) + deg(B))
```

### 2b. Graph Traversal

Both traversals are goal-directed searches: an optional GOAL parameter makes
them terminate early once the destination is reached (DFS unwinds the
recursion with a boolean return; BFS stops when the goal is dequeued/enqueued).
With no goal they traverse the whole connected component.

```
ALGORITHM DFS(graph, start, goal)
    order ← empty list
    IF start not in graph THEN RETURN order
    visited ← empty set; parent ← empty map
    IF DfsVisit(start) = REACHED THEN   // boolean unwind stops the search
        rebuild path by walking parent links backwards from goal
    RETURN order

    PROCEDURE DfsVisit(current)                // recursive
        ADD current TO visited                 // mark BEFORE recursing
        APPEND current TO order
        IF current = goal THEN RETURN REACHED  // GOAL STATE found - stop
        FOR EACH segment e IN adjacency[current] DO
            neighbour ← e.destination
            IF neighbour NOT IN visited THEN
                parent[neighbour] ← current
                IF DfsVisit(neighbour) = REACHED THEN RETURN REACHED
            END IF
        END FOR
        RETURN NOT_REACHED
    END PROCEDURE
END ALGORITHM
// Time: O(V + E) worst case with adjacency list

ALGORITHM BFS(graph, start, goal)
    order ← empty list
    IF start not in graph THEN RETURN order
    visited ← {start}; parent ← empty map     // mark at ENQUEUE time
    queue ← empty queue; enqueue start
    WHILE queue not empty DO
        current ← dequeue queue
        APPEND current TO order
        IF current = goal THEN EXIT WHILE      // first dequeue of the goal is
                                               // via a fewest-segments route
        FOR EACH segment e IN adjacency[current] DO
            neighbour ← e.destination
            IF neighbour NOT IN visited THEN
                ADD neighbour TO visited
                parent[neighbour] ← current
                enqueue neighbour
            END IF
        END FOR
    END WHILE
    rebuild path by walking parent links backwards from goal
    RETURN order
END ALGORITHM
// Time: O(V + E); visits nearest stops first (fewest segments away)
```

---

## 3. Algorithm Analysis (Big-O)

Let **V** = number of stops (vertices) and **E** = number of segments (edges).
With the adjacency-list implementation, Σ(degree of all vertices) = 2E.

**DFS analysis.** Every vertex is enqueued into the recursion exactly once
because it is marked visited *before* recursing. Every adjacency list is
scanned exactly once, and the total work over all lists is O(2E) = O(E).
Adding the hash-set/set lookups of O(1) per operation gives:

> **DFS = O(V + E)** time, **O(V)** extra space (visited set + recursion stack;
> worst-case recursion depth V − 1 on a path-shaped graph).

**BFS analysis.** Identical accounting: each vertex enters the queue once
(marking happens at enqueue time, preventing duplicates), each edge is examined
twice (once from each endpoint):

> **BFS = O(V + E)** time, **O(V)** extra space (visited set + queue holds at
> most one level of vertices, ≤ V).

For our default T250 network V = 26, E = 28, so both traversals finish in
O(54) elementary steps — effectively instant, which we verified in testing.

**Operation summary**

| Operation | Big-O | Note |
|---|---|---|
| addStop | O(1) avg | hash-map insert |
| removeStop | O(V + E) | must clean all incident edges |
| addSegment / removeSegment | O(deg A + deg B) | bounded by O(E) |
| hasDirectSegment | O(deg) | linear scan of one list |
| DFS / BFS traversal | **O(V + E)** | core requirement |
| Display adjacency list | O(V + E) | prints everything once |

**DFS vs BFS on the T250 loop.** Starting at *LRT Wangsa Maju*, DFS follows one
branch deep into the Danau Kota side before backtracking to the Section 2
loop, while BFS expands outward in rings of increasing distance (in number of
segments). Both visit all 26 stops because the loaded network is connected.

---

## 4. References (Harvard style)

1. Liang, Y.D. 2023, *Introduction to Java Programming and Data Structures:
   Comprehensive Version*, 13th edn, Prentice Hall.
2. Schildt, H. & Coward, D. 2024, *Java: The Complete Reference*, 13th edn,
   McGraw Hill, New York,
   https://www.accessengineeringlibrary.com/content/book/9781265058432.
3. Rapid Bus route information: 'T250 (Rapid KL bus route)', *Malaysian Public
   Transport Wiki*, viewed August 2026,
   https://mypt.miraheze.org/wiki/T250_(Rapid_KL_bus_route).
4. Transit schedule and stop listing for route T250, *TransitRun*, viewed
   August 2026, https://transitrun.com/ms/public-transit-line_T250_127574.
5. Oracle 2024, *JavaFX Documentation*, viewed August 2026,
   https://docs.oracle.com/javase/8/javafx/api/.
