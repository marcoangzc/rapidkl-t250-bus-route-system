# RapidKL Bus Route System — Proposal (UPDATED CONTENT)

> 使用说明：Proposal 在最终提交时会被拆出来当 Introduction，所以内容必须和
> 最终系统一致。封面页（课程/组员/导师信息）保持原样，替换下面正文即可。
> 与旧版的核心差异：T250 真实路线（26 站 28 边带权）、goal state 搜索、
> 自动载入、Maven + JavaFX、interface/abstract/concrete 三类文件结构。

---

# Proposed Application/System

The proposed system, the **RapidKL Bus Route System**, models the RapidKL feeder route **T250 (LRT Wangsa Maju ⇄ Setapak Sentral, Kuala Lumpur)** as a graph so that bus stops and the route segments connecting them can be created, managed, searched, and visualised. Each bus stop is treated as a **vertex**, and each direct road link between two neighbouring stops is treated as an **undirected, weighted edge**, whose weight is the approximate road distance in kilometres — allowing commuters to explore how stops are connected and to find a route from a chosen origin to a chosen destination. The system will be built as a **Java with Maven + Simple JavaFX** project and will provide the following core functionalities through an interactive console menu:

- **Create Graph** – add or remove a bus stop (vertex), and add or remove a route segment (weighted edge) between two existing stops, with full data-integrity checks (duplicate stops, self-loops and duplicate edges rejected; removing a stop removes all its incident segments).
- **Route Search (DFS / BFS with goal state)** – find a route from a user-chosen start stop to a user-chosen destination stop (the **goal state**). The search terminates as soon as the goal is reached and reports the discovered route with its segment count and total distance; leaving the goal empty performs a full traversal of the network instead.
- **View the Bus Route Network** – display the complete network as a text-based adjacency list, showing every stop, its official stop code, and its direct connections with distances.
- **Launch Graph Visualiser** – open an interactive JavaFX window that draws the network as coloured nodes and weighted edges; after every traversal, visited stops are repainted yellow with numbered visit-order badges and the travelled path is highlighted in green.

The system will be **pre-loaded automatically at start-up with the complete real T250 route — 26 bus stops and 28 route segments (≈ 13.1 km)** — so the graph operations and traversal algorithms can be demonstrated immediately upon start-up. A "Clear the whole network" option allows the graph to be rebuilt from scratch during the demonstration, and (with the tutor's approval) extra edges outside the published T250 alignment may be added to test branching behaviour.

# Graph Representation

The graph will be implemented with an **Adjacency List**: `LinkedHashMap<String, LinkedList<RouteSegment>>`, where each key is the unique name of a `Stop` (the vertex) and each value is a list of `RouteSegment` objects (the edges), each storing its destination stop and distance weight. Because the route is bidirectional, every edge is stored at **both** endpoints.

**Justification.** A bus route network is a *sparse* graph — each stop connects directly to only about 2–3 neighbours, so E ≈ V. The adjacency list needs **O(V + E)** memory, while an adjacency matrix would need **O(V²)** cells, almost all of them empty (for 26 stops, ~620 wasted entries). Scanning a stop's neighbours during traversal also costs O(degree) instead of scanning a full matrix row.

The system will follow object-oriented principles with the three required file types:

- **Interface** — `GraphADT`: the graph contract (vertex/edge operations, queries, traversals) plus shared result-code constants.
- **Abstract class** — `GraphTraversal`: the shared traversal skeleton using the **Template Method pattern** (a final `traverse(start, goal)` that validates the start and resets the shared state; an abstract `traverseFrom()` for the algorithm core).
- **Concrete classes** — `BusRouteGraph` (implements GraphADT with the adjacency list), `DepthFirstSearch` and `BreadthFirstSearch` (extend GraphTraversal), plus `Stop`, `RouteSegment`, `T250Data`, `Main`, `GraphView` and `NetworkViewer`.

# Proposed Graph Traversal Algorithm

Two standard graph traversal algorithms are proposed for the Route Search module: **Depth First Search (DFS)** and **Breadth First Search (BFS)**. Both are implemented as **goal-directed searches**: they accept an optional destination (goal state) and terminate as soon as it is reached, reconstructing the route from parent links; without a goal they traverse the entire connected component.

## Depth First Search (DFS)

DFS explores as deep as possible along one branch before backtracking, using **recursion** (the method call stack is the implicit stack) together with a Visited Set. When the goal is found, a boolean return value unwinds the whole recursion stack and stops the search immediately. DFS is useful for verifying overall network connectivity and for tracing a single continuous path through the network — it finds *a* valid route quickly, but not necessarily the shortest one.

## Breadth First Search (BFS)

BFS uses a **Queue** (ArrayDeque, FIFO) together with a Visited Set. It explores the network level by level, visiting all stops one segment away before moving on to stops two segments away, and so on. Because of this level-by-level expansion, **the first time BFS reaches the goal is via a route with the fewest segments** — which makes BFS the right choice for the "find a route" feature, mirroring how a commuter looks for the route with the fewest stops in between.

Both algorithms run in **O(V + E)** time, since every vertex is processed exactly once (guaranteed by the Visited Set) and every edge is inspected at most twice, and both use the Visited Set to prevent infinite loops in the cyclic T250 route.

# Pseudocode

## DFS Search(start, goal) — recursive, with early termination

```
DFS(start, goal):
    order <- empty list;  visited <- empty set;  parent <- empty map
    IF start not in graph THEN RETURN order
    DfsVisit(start)
    RETURN order

DfsVisit(current):
    add current to visited;  append current to order
    IF current = goal THEN RETURN REACHED          // goal state - unwind
    FOR EACH neighbour OF current (insertion order):
        IF neighbour not in visited THEN
            parent[neighbour] <- current
            IF DfsVisit(neighbour) = REACHED THEN RETURN REACHED
    RETURN NOT_REACHED
```

## BFS Search(start, goal) — queue, fewest-segments guarantee

```
BFS(start, goal):
    order <- empty list;  visited <- {start};  parent <- empty map
    queue <- empty queue;  enqueue(queue, start)
    WHILE queue is not empty:
        current <- dequeue(queue)
        append current to order
        IF current = goal THEN EXIT WHILE          // first dequeue of the goal
        FOR EACH neighbour OF current:             //   is via fewest segments
            IF neighbour not in visited THEN
                add neighbour to visited
                parent[neighbour] <- current
                IF neighbour = goal THEN EXIT WHILE
                enqueue(queue, neighbour)
    rebuild route by walking parent links backwards from goal
    RETURN order
```

# Big O Analysis

The table below summarises the time and space complexity of the graph operations, where V is the number of bus stops (vertices) and E is the number of route segments (edges).

| Operation | Time Complexity | Space Complexity |
|---|---|---|
| addStop(name, code, hub) | O(1) amortised | O(1) |
| removeStop(name) | O(V + E) | O(1) |
| addSegment(a, b, km) | O(deg(A) + deg(B)) | O(1) |
| removeSegment(a, b) | O(deg(A) + deg(B)) | O(1) |
| hasDirectSegment(a, b) | O(deg(v)) | O(1) |
| DFS traverse(start, goal) | O(V + E) worst case | O(V) |
| BFS traverse(start, goal) | O(V + E) worst case | O(V) |
| getGoalPath() (parent-link walk) | O(P), P = path length | O(P) |
| formatAdjacencyList() | O(V + E) | O(V) |
| Overall graph storage | – | O(V + E) |

DFS and BFS both run in **O(V + E)** worst-case time because the Visited Set guarantees each vertex is processed only once and every edge is inspected at most twice (once from each endpoint of the undirected graph). With a goal state, both may terminate early — exploring only the stops up to the depth at which the goal is found.

# Assumptions (if any)

- Routes between bus stops are bidirectional — if a bus travels from Stop A to Stop B, it can also travel from Stop B back to Stop A — so the graph is modelled as **undirected**.
- Edges are **weighted by approximate road distance (km)**, estimated so the modelled T250 loop totals ≈ 13.1 km against the published 13.9 km route length; finding the distance-optimal route (Dijkstra's algorithm) is out of scope and noted as a future enhancement.
- Stops on opposite sides of the road sharing the same name are merged into **one vertex**.
- Each bus stop name is unique and is used as the vertex key; duplicate stop names are rejected by the system.
- No self-loops are allowed — a stop cannot have a direct route to itself.
- No duplicate/parallel edges — only one direct route is stored between any given pair of stops.
- The real T250 network is **pre-loaded automatically at start-up**; "Clear the whole network" empties it for from-scratch demonstrations.
- With the tutor's approval, extra test edges outside the published T250 alignment may be added to demonstrate branching and to compare DFS/BFS routes.
- The graph may become disconnected after stop removals; traversals report how many stops were unreachable, and a goal search reports the destination as UNREACHABLE.
