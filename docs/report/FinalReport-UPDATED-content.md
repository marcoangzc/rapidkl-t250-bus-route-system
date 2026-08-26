# RapidKL Bus Route System — Final Report (UPDATED CONTENT)

> 使用说明：本文件是按**最新代码**（goal-state 搜索 + 自动载入 + interface/abstract
> 结构 + Maven）重写的报告正文。封面页、Originality Declaration、Module &
> Task 表、Group Contract 保持你们原样不动；把下面第 1–5 节替换进 Google Doc
> 即可。表格里的截图位置用你们自己的运行截图填入（建议用
> docs/screenshot-dfs-map.png 和控制台截图）。

---

# 1. Introduction

The RapidKL Bus Route System is a Java application developed to model the RapidKL feeder bus route **T250 (LRT Wangsa Maju ⇄ Setapak Sentral, Kuala Lumpur)** as a graph Abstract Data Type (ADT), allowing bus stops and the route segments connecting them to be created, modified, searched, and visually inspected. Each bus stop is represented as a **vertex**, and each direct road link between two neighbouring stops is represented as an **undirected, weighted edge**, whose weight is the approximate road distance in kilometres. The system is built as a **Java with Maven + Simple JavaFX** project: the console menu drives all graph operations, and a JavaFX window visualises the network as a live route map.

## Objectives

- To design and implement a reusable Graph ADT using object-oriented principles — an **interface** (`GraphADT`), an **abstract class** (`GraphTraversal`), and **concrete classes** (`BusRouteGraph`, `DepthFirstSearch`, `BreadthFirstSearch`, etc.).
- To represent the RapidKL T250 bus network using an **Adjacency List** (`LinkedHashMap<String, LinkedList<RouteSegment>>`) with weighted edges.
- To implement and compare two goal-directed traversal algorithms, **Depth First Search (DFS)** and **Breadth First Search (BFS)**, which accept an optional **goal state**: the search terminates as soon as the destination stop is reached and the discovered route is reconstructed from parent links.
- To provide a text-based console menu for managing the graph (add/remove stops and segments) and an interactive JavaFX window that visualises the network and highlights the result of every traversal.

## Scope

The system starts with the **complete real T250 route pre-loaded automatically (26 bus stops, 28 route segments, ≈ 13.1 km total)**, so every feature can be demonstrated immediately upon start-up. The supported functions are:

| # | Function | Menu |
|---|----------|------|
| 1 | Auto-load the full T250 network at start-up | automatic |
| 2 | Add a bus stop (vertex) with name, official stop code and hub flag; duplicate names rejected | Create Graph → 1 |
| 3 | Remove a bus stop together with ALL its incident segments (no dangling edges remain) | Create Graph → 2 |
| 4 | Add a route segment (weighted edge) between two existing stops; self-loops and duplicate edges rejected | Create Graph → 3 |
| 5 | Remove a route segment from both endpoints | Create Graph → 4 |
| 6 | Display the complete network as a text adjacency list | Create Graph → 5 |
| 7 | Clear the whole network (to demonstrate building a graph from scratch) | Create Graph → 6 |
| 8 | **DFS goal search**: start stop → destination stop; stops at the goal and reports the route, segment count and total distance | Route Search → 1 |
| 9 | **BFS goal search**: same interface; guarantees the fewest-segments route. Leaving the goal empty switches either algorithm into full-network traversal mode | Route Search → 2 |
| 10 | JavaFX map window: vertices, weighted edges and a legend; after a traversal, visited stops are repainted yellow with numbered badges and the travelled path is highlighted | View Network |

Weighted shortest-path algorithms by distance (e.g. Dijkstra's algorithm) are outside the current scope and are identified as possible future enhancements.

## Graph Representation Description

The graph is implemented with an **adjacency list**: `LinkedHashMap<String, LinkedList<RouteSegment>>`. Each key is the unique name of a `Stop` (the vertex); each value is a list of `RouteSegment` objects (the edges), where a segment stores its destination stop name and its distance weight. Because buses travel in both directions, every edge is inserted into **both** endpoints' lists.

**Why an adjacency list instead of an adjacency matrix?** A bus route network is a *sparse* graph — each stop connects directly to only about 2–3 neighbours, so E ≈ V. The adjacency list needs **O(V + E)** memory, while a V×V matrix would need **O(V²)** cells, almost all of them empty (for 26 stops, ~620 wasted entries). Scanning a stop's neighbours during traversal also costs O(degree) instead of scanning a full matrix row of V cells.

## Assumptions

1. Routes between bus stops are bidirectional — if a bus travels from Stop A to Stop B, it can also travel from Stop B back to Stop A — so the graph is modelled as **undirected**.
2. Edge weights are **approximate road distances in kilometres**, estimated so that the modelled loop totals ≈ 13.1 km against the published 13.9 km route length.
3. Stops on opposite sides of the road that share the same name (e.g. the TAR UMT gate stops) are merged into **one vertex**.
4. Each bus stop name is unique and is used as the vertex key; duplicate names are rejected. No self-loops and no duplicate/parallel edges are allowed.
5. The real T250 network is **pre-loaded at start-up**; "Clear the whole network" (Create Graph → 6) empties it when a from-scratch demonstration is required.
6. With the tutor's approval, extra test edges outside the published T250 alignment may be added through Create Graph to demonstrate branching and to compare DFS/BFS routes between arbitrary stops.
7. The graph may become disconnected after stop removals; traversals then report how many stops were unreachable, and a goal search reports the destination as UNREACHABLE.

# 2. Algorithm Analysis

This section analyses the two goal-directed traversal algorithms implemented in `DepthFirstSearch.java` and `BreadthFirstSearch.java` (subclasses of the abstract `GraphTraversal`), and demonstrates their behaviour on the pre-loaded T250 network (V = 26 stops, E = 28 segments).

## Sample Network – Adjacency List

The table below is the adjacency list produced by Create Graph → 5 for the pre-loaded network (26 vertices, 28 undirected edges, ≈ 13.1 km). The full listing is reproduced in **Appendix A**.

| Bus Stop | Direct Connections (distance) |
|---|---|
| LRT Wangsa Maju *HUB* | Tar Villa Setapak (0.9 km), AEON Alpha Angle (0.6 km) |
| Tar Villa Setapak | LRT Wangsa Maju (0.9), PULAPOT (0.6), Surau Taman Bunga Raya (1.0) |
| PULAPOT | Tar Villa Setapak (0.6), Surau Taman Bunga Raya (0.5) |
| Surau Taman Bunga Raya | PULAPOT (0.5), TAR UMT Gate 4 (0.5), Tar Villa Setapak (1.0) |
| TAR UMT Gate 4 | Surau Taman Bunga Raya (0.5), TAR UMT Main Gate (0.3) |
| TAR UMT Main Gate | TAR UMT Gate 4 (0.3), TAR UMT Gate 2 (0.3) |
| TAR UMT Gate 2 | TAR UMT Main Gate (0.3), Surau Al-Amin (0.6), Vista Wirajaya (0.9) |
| Surau Al-Amin | TAR UMT Gate 2 (0.6), Indah Apartments (0.4) |
| Indah Apartments | Surau Al-Amin (0.4), SK Danau Kota (0.5) |
| SK Danau Kota | Indah Apartments (0.5), PV 12 Platinum Lake (0.5) |
| PV 12 Platinum Lake | SK Danau Kota (0.5), PV 10 Platinum Lake (0.2) |
| PV 10 Platinum Lake | PV 12 Platinum Lake (0.2), PV 16 Platinum Lake (0.2) |
| PV 16 Platinum Lake | PV 10 Platinum Lake (0.2), Columbia Hospital Danau Kota (0.4) |
| Columbia Hospital Danau Kota | PV 16 Platinum Lake (0.4), Setapak Central (0.6) |
| Setapak Central *HUB* | Columbia Hospital Danau Kota (0.6), Vista Wirajaya (0.6) |
| Vista Wirajaya | Setapak Central (0.6), TAR UMT Gate 2 (0.9) |
| AEON Alpha Angle | LRT Wangsa Maju (0.6), Wangsa Metroview (0.3) |
| Wangsa Metroview | AEON Alpha Angle (0.3), Desa Setapak (0.4), Flat WM Sec 2 (Selatan) (0.6) |
| Desa Setapak | Wangsa Metroview (0.4), Flat WM Sec 2 (Timur) (0.4) |
| Flat WM Sec 2 (Timur) | Desa Setapak (0.4), Pasar & Penjaja WM Sec 2 (0.3) |
| Pasar & Penjaja WM Sec 2 | Flat WM Sec 2 (Timur) (0.3), Flat WM Sec 2 (Utara) (0.3) |
| Flat WM Sec 2 (Utara) | Pasar & Penjaja WM Sec 2 (0.3), Flat WM Sec 2 (Barat) (0.3) |
| Flat WM Sec 2 (Barat) | Flat WM Sec 2 (Utara) (0.3), Hospital Tentera (Utara) (0.4) |
| Hospital Tentera (Utara) | Flat WM Sec 2 (Barat) (0.4), Hospital Tentera (Selatan) (0.2) |
| Hospital Tentera (Selatan) | Hospital Tentera (Utara) (0.2), Flat WM Sec 2 (Selatan) (0.3) |
| Flat WM Sec 2 (Selatan) | Hospital Tentera (Selatan) (0.3), Wangsa Metroview (0.6) |

*Total stops V = 26, total segments E = 28, total length ≈ 13.1 km.*

## BFS Goal Search – Worked Example (start: LRT Wangsa Maju, goal: Setapak Central)

BFS uses a **Queue** (ArrayDeque, FIFO) together with a Visited Set. It explores the network level by level, so the **first time the goal is reached is via a route with the fewest segments**. The actual output of Route Search → 2 is:

| Bus Stop Visited | Hops from Origin |
|---|---|
| LRT Wangsa Maju | 0 |
| Tar Villa Setapak, AEON Alpha Angle | 1 |
| PULAPOT, Surau Taman Bunga Raya, Wangsa Metroview | 2 |
| TAR UMT Gate 4, Desa Setapak, Flat WM Sec 2 (Selatan) | 3 |
| TAR UMT Main Gate, Flat WM Sec 2 (Timur), Hospital Tentera (Selatan) | 4 |
| TAR UMT Gate 2, Pasar & Penjaja WM Sec 2, Hospital Tentera (Utara) | 5 |
| Surau Al-Amin, Vista Wirajaya, Flat WM Sec 2 (Utara) | 6 |
| Flat WM Sec 2 (Barat), Indah Apartments, **Setapak Central (GOAL)** | 7 |

The search explored **21 of 26 stops** before terminating and reported:

> **GOAL FOUND!** LRT Wangsa Maju → Tar Villa Setapak → Surau Taman Bunga Raya → TAR UMT Gate 4 → TAR UMT Main Gate → TAR UMT Gate 2 → Vista Wirajaya → Setapak Central
> **Segments travelled: 7 | Total distance: 4.5 km** — the fewest-segments route.

## DFS Goal Search – Worked Example (same start and goal)

DFS **recursion** dives as deep as possible along one branch before backtracking; when the goal is found, a boolean return value unwinds the whole recursion stack and stops the search immediately. On the same trip the output is:

> **GOAL FOUND!** LRT Wangsa Maju → Tar Villa Setapak → PULAPOT → Surau Taman Bunga Raya → TAR UMT Gate 4 → TAR UMT Main Gate → TAR UMT Gate 2 → Surau Al-Amin → Indah Apartments → SK Danau Kota → PV 12 Platinum Lake → PV 10 Platinum Lake → PV 16 Platinum Lake → Columbia Hospital Danau Kota → Setapak Central
> **Segments travelled: 14 | Total distance: 6.5 km** (explored 15 of 26 stops)

**Comparison.** Both algorithms found *a* valid route, but DFS followed one branch deep around the Danau Kota side of the loop (14 segments) while BFS exploited the shortcut edge Surau Taman Bunga Raya → Tar Villa Setapak and needed only **7 segments — half of DFS's count**. This demonstrates experimentally that DFS guarantees *reachability* but not minimality, whereas BFS guarantees the fewest-segments route on an unweighted view of the graph. (A truly distance-optimal route would require Dijkstra's algorithm on the km weights.)

## Full-Network Traversal (goal left empty)

Leaving the destination empty runs a plain traversal of the whole connected component. Both DFS (from LRT Wangsa Maju) and BFS (from Setapak Central) visited **all 26 of 26 stops**, confirming the T250 network is fully connected. The complete visit orders are reproduced in **Appendix B**; the furthest stop from Setapak Central is Flat WM Sec 2 (Barat) at 13 hops, while DFS reached it at recursion depth 7 after backtracking.

## Complexity Analysis

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
| formatAdjacencyList() / getTotalDistanceKm() | O(V + E) | O(V) |
| Overall graph storage | – | O(V + E) |

Both DFS and BFS run in **O(V + E)** worst-case time: the Visited Set (HashSet, O(1) lookups) guarantees each vertex is processed exactly once, and each undirected edge is inspected exactly twice (once from each endpoint). The auxiliary recursion stack / queue and the Visited Set both require O(V) space. With a goal state, both algorithms may terminate early, exploring only the stops up to the depth at which the goal is found (15 of 26 for DFS, 21 of 26 for BFS in the worked example). BFS is the appropriate choice when the fewest-segments route is wanted; DFS is appropriate for reachability checking and for tracing one continuous exploratory path.

# 3. Coding

The system follows a layered, object-oriented architecture that separates the Graph ADT contract, the shared traversal skeleton, and the concrete implementations, in line with the abstraction, inheritance, encapsulation and polymorphism principles covered in AMCS2034. The project is built with **Maven** (Java with Maven + Simple JavaFX structure; `mvn javafx:run` launches it) and requires JDK 17+.

## Architecture Overview

| Class | Type | Responsibility |
|---|---|---|
| `GraphADT` | **Interface** | Declares the Graph ADT contract — vertex operations (addStop, removeStop), edge operations (addSegment, removeSegment), queries and traversals — plus the shared result-code constants (RESULT_OK, ERR_STOP_EXISTS, …). |
| `GraphTraversal` | **Abstract class** | Template Method pattern: the final `traverse(start, goal)` skeleton validates the start stop and resets the shared state (visited set, order list, parent map, goal); the abstract `traverseFrom()` core is implemented by the subclasses. Also provides getGoalPath() path reconstruction. |
| `DepthFirstSearch` | Concrete subclass | Recursive DFS core with boolean-unwind early termination at the goal. |
| `BreadthFirstSearch` | Concrete subclass | Queue-based (ArrayDeque) BFS core; checks the goal at dequeue/enqueue time, guaranteeing the fewest-segments route. |
| `BusRouteGraph` | Concrete class | `implements GraphADT` — the adjacency-list storage (LinkedHashMap of LinkedList) and all graph operations with full data-integrity checks. |
| `Stop` | Concrete class | Vertex: stop name, official Rapid KL code, hub flag. |
| `RouteSegment` | Concrete class | Edge: destination stop + distance weight (km). |
| `T250Data` | Concrete utility | Seeds the 26-stop / 28-segment real T250 network at start-up. |
| `Main` | Concrete driver | Console menus, input validation, goal prompts, route reporting; programs against the GraphADT interface (`GraphADT network = new BusRouteGraph()`). |
| `GraphView` | Concrete (JavaFX) | Renders vertices and weighted edges; overlays traversal results (yellow stops, numbered badges, green path). |
| `NetworkViewer` | Concrete (JavaFX) | Map window on its own JavaFX thread with legend and auto-refresh after each traversal. |

This layered design means the storage structure could be replaced (e.g. by an adjacency matrix implementing GraphADT) or new behaviours added without changing the console UI or the traversal classes.

## Key Code Excerpt – Traversal Skeleton (GraphTraversal.java, abstract class)

```java
/** TEMPLATE METHOD - the fixed skeleton of every traversal. */
public final List<String> traverse(String startName, String goalName) {
    order.clear();  visited.clear();  parent.clear();
    goal = (goalName == null || goalName.isEmpty()) ? null : goalName;
    if (!graph.containsStop(startName)) {
        return order;                    // unknown start -> empty result
    }
    traverseFrom(startName);             // algorithm core (DFS or BFS)
    return order;
}

/** The algorithm core: recursion in DFS, a queue in BFS. */
protected abstract void traverseFrom(String startName);
```

## Key Code Excerpt – BFS Core with Goal State (BreadthFirstSearch.java)

```java
visit(startName);            // mark at ENQUEUE time so nothing can
queue.addLast(startName);    // enter the queue twice
while (!queue.isEmpty()) {
    String current = queue.pollFirst();
    if (goal != null && current.equals(goal)) {
        return;              // first dequeue of the goal is via a
    }                        // fewest-segments route - stop here
    for (RouteSegment seg : graph.getNeighbours(current)) {
        String neighbour = seg.getDestination();
        if (!visited.contains(neighbour)) {
            recordParent(neighbour, current);   // for path rebuild
            visit(neighbour);
            if (goal != null && neighbour.equals(goal)) {
                return;      // found while expanding: cannot get shorter
            }
            queue.addLast(neighbour);
        }
    }
}
```

## Key Code Excerpt – Adjacency-List Storage (BusRouteGraph.java)

```java
/** All vertices (bus stops), keyed by their unique name. */
private final Map<String, Stop> stops = new LinkedHashMap<>();

/** Adjacency list: for every stop name, the list of direct segments. */
private final Map<String, LinkedList<RouteSegment>> adjacency = new LinkedHashMap<>();

public int addSegment(String stopA, String stopB, double distanceKm) {
    if (!stops.containsKey(stopA) || !stops.containsKey(stopB)) return ERR_STOP_NOT_FOUND;
    if (stopA.equals(stopB))                                    return ERR_SELF_LOOP;
    if (hasDirectSegment(stopA, stopB))                         return ERR_SEGMENT_EXISTS;
    adjacency.get(stopA).add(new RouteSegment(stopB, distanceKm));
    adjacency.get(stopB).add(new RouteSegment(stopA, distanceKm));  // undirected: both ends
    return RESULT_OK;
}
```

### Print-screens (insert your captures here, max 2 per page, each with a caption)

- Figure 1: Start-up — the T250 network is pre-loaded automatically; adjacency list showing 26 stops / 28 segments / 13.1 km. *(Create Graph → 5)*
- Figure 2: JavaFX route map of the T250 network (blue = stop, orange = interchange hub, edge labels = km).
- Figure 3: DFS goal search from LRT Wangsa Maju to Setapak Central — route found (14 segments, 6.5 km).
- Figure 4: BFS goal search on the same trip — fewest-segments route (7 segments, 4.5 km).
- Figure 5: Map window after BFS — visited stops repainted yellow with numbered visit-order badges, travelled path highlighted in green.
- Figure 6: Data-integrity checks — duplicate stop name rejected, self-loop rejected, duplicate edge rejected; removing a stop also removes all its segments.

# 4. Reference

References are listed in APA style, in alphabetical order.

- Cormen, T. H., Leiserson, C. E., Rivest, R. L., & Stein, C. (2022). *Introduction to algorithms* (4th ed.). MIT Press.
- GeeksforGeeks. (2024). *Breadth first search or BFS for a graph*. https://www.geeksforgeeks.org/breadth-first-search-or-bfs-for-a-graph/
- GeeksforGeeks. (2024). *Depth first search or DFS for a graph*. https://www.geeksforgeeks.org/depth-first-search-or-dfs-for-a-graph/
- Liang, Y. D. (2023). *Introduction to Java programming and data structures: Comprehensive version* (13th ed.). Prentice Hall.
- Malaysian Public Transport Wiki. (2026). *T250 (Rapid KL bus route)*. https://mypt.miraheze.org/wiki/T250_(Rapid_KL_bus_route)
- Oracle. (2024). *The Java® Tutorials – Collections framework*. https://docs.oracle.com/javase/tutorial/collections/
- Oracle. (2024). *JavaFX documentation*. https://docs.oracle.com/javase/8/javafx/api/
- Prasetya Group. (2024). *RapidKL bus route information*. Rapid Bus Sdn. Bhd. https://www.myrapid.com.my/
- Schildt, H., & Coward, D. (2024). *Java: The complete reference* (13th ed.). McGraw Hill.

# 5. Appendices

## Appendix A – Full Adjacency List of the Pre-loaded T250 Network

*(Create Graph → 5 output; 26 stops, 28 segments, ≈ 13.1 km. Each undirected edge is listed at both endpoints.)*

```
LRT Wangsa Maju [KL2097] *HUB*
    --> Tar Villa Setapak                0.9 km
    --> AEON Alpha Angle                 0.6 km
Tar Villa Setapak [KL193]
    --> LRT Wangsa Maju                  0.9 km
    --> PULAPOT                          0.6 km
    --> Surau Taman Bunga Raya           1.0 km
PULAPOT [KL164]
    --> Tar Villa Setapak                0.6 km
    --> Surau Taman Bunga Raya           0.5 km
Surau Taman Bunga Raya [KL2100]
    --> PULAPOT                          0.5 km
    --> TAR UMT Gate 4                   0.5 km
    --> Tar Villa Setapak                1.0 km
TAR UMT Gate 4
    --> Surau Taman Bunga Raya           0.5 km
    --> TAR UMT Main Gate                0.3 km
TAR UMT Main Gate [KL163]
    --> TAR UMT Gate 4                   0.3 km
    --> TAR UMT Gate 2                   0.3 km
TAR UMT Gate 2
    --> TAR UMT Main Gate                0.3 km
    --> Surau Al-Amin                    0.6 km
    --> Vista Wirajaya                   0.9 km
Surau Al-Amin [KL980]
    --> TAR UMT Gate 2                   0.6 km
    --> Indah Apartments                 0.4 km
Indah Apartments [KL981]
    --> Surau Al-Amin                    0.4 km
    --> SK Danau Kota                    0.5 km
SK Danau Kota [KL970]
    --> Indah Apartments                 0.5 km
    --> PV 12 Platinum Lake              0.5 km
PV 12 Platinum Lake [KL942]
    --> SK Danau Kota                    0.5 km
    --> PV 10 Platinum Lake              0.2 km
PV 10 Platinum Lake [KL1519]
    --> PV 12 Platinum Lake              0.2 km
    --> PV 16 Platinum Lake              0.2 km
PV 16 Platinum Lake [KL1520]
    --> PV 10 Platinum Lake              0.2 km
    --> Columbia Hospital Danau Kota     0.4 km
Columbia Hospital Danau Kota [KL1598]
    --> PV 16 Platinum Lake              0.4 km
    --> Setapak Central                  0.6 km
Setapak Central [KL680] *HUB*
    --> Columbia Hospital Danau Kota     0.6 km
    --> Vista Wirajaya                   0.6 km
Vista Wirajaya [KL973]
    --> Setapak Central                  0.6 km
    --> TAR UMT Gate 2                   0.9 km
AEON Alpha Angle [KL171]
    --> LRT Wangsa Maju                  0.6 km
    --> Wangsa Metroview                 0.3 km
Wangsa Metroview [KL2103]
    --> AEON Alpha Angle                 0.3 km
    --> Desa Setapak                     0.4 km
    --> Flat WM Sec 2 (Selatan)          0.6 km
Desa Setapak [KL1604]
    --> Wangsa Metroview                 0.4 km
    --> Flat WM Sec 2 (Timur)            0.4 km
Flat WM Sec 2 (Timur) [KL172]
    --> Desa Setapak                     0.4 km
    --> Pasar & Penjaja WM Sec 2         0.3 km
Pasar & Penjaja WM Sec 2 [KL173]
    --> Flat WM Sec 2 (Timur)            0.3 km
    --> Flat WM Sec 2 (Utara)            0.3 km
Flat WM Sec 2 (Utara) [KL174]
    --> Pasar & Penjaja WM Sec 2         0.3 km
    --> Flat WM Sec 2 (Barat)            0.3 km
Flat WM Sec 2 (Barat) [KL175]
    --> Flat WM Sec 2 (Utara)            0.3 km
    --> Hospital Tentera (Utara)         0.4 km
Hospital Tentera (Utara) [KL176]
    --> Flat WM Sec 2 (Barat)            0.4 km
    --> Hospital Tentera (Selatan)       0.2 km
Hospital Tentera (Selatan) [KL177]
    --> Hospital Tentera (Utara)         0.2 km
    --> Flat WM Sec 2 (Selatan)          0.3 km
Flat WM Sec 2 (Selatan) [KL178]
    --> Hospital Tentera (Selatan)       0.3 km
    --> Wangsa Metroview                 0.6 km
```

## Appendix B – Full-Network Traversal Outputs (goal left empty)

### B.1 DFS from LRT Wangsa Maju (recursive; visits all 26 stops)

```
 1 LRT Wangsa Maju            (depth 0)
 2 Tar Villa Setapak          (1)     3 PULAPOT (2)     4 Surau Taman Bunga Raya (3)
 5 TAR UMT Gate 4 (4)         6 TAR UMT Main Gate (5)   7 TAR UMT Gate 2 (6)
 8 Surau Al-Amin (7)          9 Indah Apartments (8)   10 SK Danau Kota (9)
11 PV 12 Platinum Lake (10)  12 PV 10 Platinum Lake (11)
13 PV 16 Platinum Lake (12)  14 Columbia Hospital Danau Kota (13)
15 Setapak Central (14)      16 Vista Wirajaya (15)
17 AEON Alpha Angle (1)      18 Wangsa Metroview (2)   19 Desa Setapak (3)
20 Flat WM Sec 2 (Timur) (4) 21 Pasar & Penjaja WM Sec 2 (5)
22 Flat WM Sec 2 (Utara) (6) 23 Flat WM Sec 2 (Barat) (7)
24 Hospital Tentera (Utara) (8)   25 Hospital Tentera (Selatan) (9)
26 Flat WM Sec 2 (Selatan) (10)
```

*(Numbers in brackets are the DFS tree depth. Stops 17–26 show backtracking: after Vista Wirajaya the search returns up the stack to LRT Wangsa Maju and explores the AEON branch.)*

### B.2 BFS from Setapak Central (queue; visits all 26 stops level by level)

```
hop 0:  Setapak Central
hop 1:  Columbia Hospital Danau Kota, Vista Wirajaya
hop 2:  PV 16 Platinum Lake, TAR UMT Gate 2
hop 3:  PV 10 Platinum Lake, TAR UMT Main Gate, Surau Al-Amin
hop 4:  PV 12 Platinum Lake, TAR UMT Gate 4, Indah Apartments
hop 5:  SK Danau Kota, Surau Taman Bunga Raya
hop 6:  PULAPOT, Tar Villa Setapak
hop 7:  LRT Wangsa Maju, AEON Alpha Angle
hop 8:  Wangsa Metroview
hop 9:  Desa Setapak, Flat WM Sec 2 (Selatan)
hop 10: Flat WM Sec 2 (Timur), Hospital Tentera (Selatan)
hop 11: Pasar & Penjaja WM Sec 2, Hospital Tentera (Utara)
hop 12: Flat WM Sec 2 (Utara)
hop 13: Flat WM Sec 2 (Barat)          <- furthest stop from the origin
```

## Appendix C – Source Code Files

The full annotated source code is submitted separately as a ZIP archive (Maven project `rapidkl-t250-bus-route-system`): `pom.xml`, and `src/main/java/com/rapidkl/t250/` containing GraphADT.java, GraphTraversal.java, DepthFirstSearch.java, BreadthFirstSearch.java, BusRouteGraph.java, Stop.java, RouteSegment.java, T250Data.java, Main.java, GraphView.java, NetworkViewer.java (11 files).
