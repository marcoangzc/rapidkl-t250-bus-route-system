# ONBOARDING — 组员上手指南（Rapid KL T250 巴士路线系统）

> 给零基础组员：这份文档带你从「跑不起来」到「Viva 能讲清楚」。
> 全程只需要：一台 Windows 电脑 + 30 分钟装环境 + 2 小时读文档。
> 有不懂的先看第 8 节「常见问题」，再群里问。

---

## 目录

1. [先把程序跑起来（两种方式）](#1-先把程序跑起来)
2. [NetBeans 完整部署（Viva 推荐用这个）](#2-netbeans-完整部署)
3. [这个项目到底在做什么（5 分钟版）](#3-这个项目在做什么)
4. [7 个文件地图 + 阅读顺序](#4-文件地图)
5. [Java 最小必修课（只讲项目里用到的）](#5-java-最小必修课)
6. [DSA 核心概念（Viva 的重中之重）](#6-dsa-核心概念)
7. [Viva 英文问答 16 题（背这个就够了）](#7-viva-英文问答)
8. [常见报错速查表](#8-常见报错速查表)
9. [建议分工与演示流程](#9-建议分工与演示流程)

---

## 1. 先把程序跑起来

### 方式 A：双击脚本（最快，30 秒）

前提：电脑装了 **JDK 17 或更新版本**（检查方法：打开 cmd 输入 `java -version`，看到 17 以上就行）。

```
双击 compile.bat   → 编译（第一次必须）
双击 run.bat       → 启动系统
```

看到控制台菜单就成功了。选 `1` → `6` 载入 T250 数据，再选 `3` 看地图窗口。

### 方式 B：命令行（和方式 A 等效）

```bash
cd 项目文件夹
mvn clean compile     # 编译（第一次会联网下载依赖，耐心等）
mvn javafx:run        # 运行
```

---

## 2. NetBeans 完整部署

> Viva 当天建议用 NetBeans 打开项目演示（显得专业，tutor 也常要求 IDE）。
> 本项目是 **Maven 项目**（Java with Maven + Simple JavaFX），NetBeans 打开即用，
> **不需要**手动配置 Module-path 或下载 jar——依赖自动下载。
> 以下步骤用 **Apache NetBeans 13 或更新版本** 测试过。

### 第 1 步：确认 JDK 版本

`Tools → Java Platforms`。列表里必须有 **JDK 17+**（推荐 21）。
没有就去 https://adoptium.net 下载 Temurin 21（免费），装完后在同一个窗口 `Add Platform` 注册。

### 第 2 步：拿到项目

二选一：

- **A. 组长发你 zip**：解压到任意文件夹（路径不要带中文）
- **B. 从 GitHub 克隆**：`Team → Git → Clone...` → 粘贴仓库地址 → Finish

### 第 3 步：打开项目

1. `File → Open Project`
2. 选中项目文件夹（图标带 **M** 的是 Maven 项目标志，认准它）
3. 打开后 NetBeans 右下角会自动下载依赖（第一次约 1-2 分钟，等进度条走完）
4. 展开 `Source Packages → com.rapidkl.t250`，能看到 7 个 .java 文件 = 成功

### 第 4 步：运行

右键项目（不是文件）→ `Run`（或 F6）。

- 正常情况：直接弹出控制台菜单
- 若 F6 无反应：右键项目 → `Custom → Goals...` → 输入 `javafx:run` → OK

> ⚠ 运行的是**项目**不是单个文件。右键 `Main.java → Run File` 会因为
> 缺少 JavaFX 模块参数而报错，这是 Maven JavaFX 项目的正常行为。

### 报错了？直接跳到第 8 节速查表。

---

## 3. 这个项目在做什么

一句话：**把吉隆坡真实的 T250 巴士环线变成一张「图」，程序能增删站点和路线、能遍历、能画出来。**

对照现实理解图的四个关键词：

| DSA 术语 | 现实对应 | 代码里的类 |
|---|---|---|
| **Vertex** 顶点 | 一个巴士站（如 LRT Wangsa Maju） | `Stop` |
| **Edge** 边 | 两个相邻站之间的一段路 | `RouteSegment` |
| **Weight** 权重 | 那段路的公里数（如 0.9 km） | `RouteSegment.distanceKm` |
| **Undirected** 无向 | 巴士能 A→B 也能 B→A | 每条边存进**两端**的邻接表 |

我们的 T250 数据：**26 个站、28 段路、总长 ≈13.1 km**，是一个**环线**
（从 LRT Wangsa Maju 出发绕一圈回到原地）——记住「环线」这三个字，
后面 DFS/BFS 为什么要标记 visited 就靠它解释。

程序三大功能（对应主菜单 1/2/3）：

1. **Create Graph**：增删顶点和边（带查重、防自环、删站自动清边）
2. **Search**：DFS / BFS 遍历，打印访问顺序表
3. **View**：JavaFX 地图窗口，遍历过的站变黄 + 序号徽章

---

## 4. 文件地图

按这个顺序读，每个文件 10-20 分钟：

| 顺序 | 文件 | 干什么 | 难度 |
|---|---|---|---|
| ① | `Stop.java` | 顶点类：站名 + 站码 + 是否枢纽。**最简单，从这里建立信心** | ⭐ |
| ② | `RouteSegment.java` | 边类：目的地 + 公里数。同样简单 | ⭐ |
| ③ | `T250Data.java` | 种子数据：26 站 28 边的真实数据表 | ⭐ |
| ④ | `BusRouteGraph.java` | **核心**：图本身。增删顶点/边 + DFS + BFS | ⭐⭐⭐ |
| ⑤ | `Main.java` | 控制台菜单：所有用户交互和输入验证 | ⭐⭐ |
| ⑥ | `GraphView.java` | 画图：把图变成圆圈和线（JavaFX） | ⭐⭐ |
| ⑦ | `NetworkViewer.java` | 地图窗口的外壳（标题、图例、刷新） | ⭐⭐ |

> 分工建议：每人认领 1-2 个文件当「Owner」，Viva 时主讲自己的部分，
> 但其他文件也要能讲个大概（见第 9 节）。

---

## 5. Java 最小必修课

只讲项目里用到的，学完就能读懂全部代码。

### 5.1 类和对象（看 Stop.java 就懂）

```java
public class Stop {
    private final String name;   // 字段(field)：对象的属性
    private final String code;
    private final boolean hub;

    public Stop(String name, String code, boolean hub) {  // 构造器：new 的时候执行
        this.name = name;        // this.name = 字段; name = 参数
        this.code = code;
        this.hub = hub;
    }
    public String getName() { return name; }   // getter：给别的类读字段用
}
```

- `new Stop("LRT Wangsa Maju", "KL2097", true)` 创建一个站对象
- `private` = 只有本类能碰；`public` = 谁都能用（封装封装）
- `final` = 赋值后不能再改（站名不该变，所以加 final）
- `@Override toString()` = 规定打印这个对象时显示什么

### 5.2 项目里的四个集合类（重点！）

| 集合 | 项目里的位置 | 一句话理解 |
|---|---|---|
| `LinkedList<RouteSegment>` | 邻接表的值 | **一串挂起来的节点**，存某个站的所有邻居 |
| `LinkedHashMap<String, Stop>` | `BusRouteGraph.stops` | **字典**：站名 → 站对象；Linked = 记住插入顺序，显示稳定 |
| `LinkedHashMap<String, LinkedList>` | `BusRouteGraph.adjacency` | **邻接表本体**（见第 6.2 节） |
| `ArrayDeque<String>` | BFS 的队列 | **排队**：`addLast` 入队尾，`pollFirst` 出队头（先来先走） |
| `HashSet<String>` | visited 集合 | **点名册**：`contains(x)` O(1) 查「走没走过」 |

泛型 `<String, Stop>` 的意思：字典的钥匙是 String，值是 Stop。类型写死，取出来不用强转。

### 5.3 其他高频语法

```java
for (RouteSegment seg : adjacency.get(current)) { ... }   // for-each：逐个拿出集合元素
List.of()                                                  // 不可变空列表（当默认值）
try { Integer.parseInt(line); } catch (NumberFormatException e) { ... }  // 字符串转数字，转不动就走 catch
input.nextLine()                                           // Scanner 读一行用户输入
String.format("%-4d", i)                                   // 格式化：%-4d = 左对齐占 4 格
sb.append("...")                                           // StringBuilder 拼字符串（比 + 快）
```

### 5.4 static 是什么

`Main` 里的方法和字段几乎全是 `static`：不需要 `new Main()` 就能直接用，
因为整个程序只需要一份菜单逻辑、一个图实例。读代码时看到
`network.addStop(...)`，`network` 就是那个唯一的图对象。

---

## 6. DSA 核心概念

### 6.1 为什么用邻接表（Adjacency List）不用矩阵（Matrix）

两种存法对比（V = 站数 = 26，E = 边数 = 28）：

- **矩阵**：26×26 = 676 格的二维数组，`matrix[A][B] = 距离`。
  只用到 28×2 = 56 格，**620 格全是空的**，浪费内存。
  查某站的所有邻居要扫整行 26 格。
- **邻接表**：每个站挂一条邻居链，`adjacency.get("LRT Wangsa Maju")`
  直接返回它的 2 个邻居。内存只要 **O(V + E)**。

> Viva 标准答案：*Bus networks are sparse graphs — each stop connects to only
> 2-3 neighbours. Adjacency list uses O(V+E) memory while a matrix wastes
> O(V²) cells; scanning a stop's neighbours costs O(degree) instead of O(V).*

### 6.2 邻接表长什么样（真实数据）

```
adjacency = {
  "LRT Wangsa Maju" → [ →Tar Villa Setapak (0.9km), →AEON Alpha Angle (0.6km) ]
  "Tar Villa Setapak" → [ →LRT Wangsa Maju (0.9km), →PULAPOT (0.6km) ]
  ...
}
```

注意 LRT Wangsa Maju 出现在自己的表里，Tar Villa 的表里也有 LRT Wangsa Maju
——**无向图的边存两份**，这就是「无向」的实现方式。

### 6.3 DFS 深度优先（递归，走到底再回头）

代码就这几行（BusRouteGraph.java）：

```java
private void dfsVisit(String current, Set<String> visited, List<String> order) {
    visited.add(current);          // ① 先点名！
    order.add(current);
    for (RouteSegment seg : adjacency.get(current)) {
        String neighbour = seg.getDestination();
        if (!visited.contains(neighbour)) {   // ② 没走过的邻居才进去
            dfsVisit(neighbour, visited, order);   // ③ 递归：一头扎到底
        }
    }
}
```

**真实运行结果**（从 LRT Wangsa Maju 出发，见 docs/sample-console-output.txt）：

```
1 LRT Wangsa Maju → 2 Tar Villa → 3 PULAPOT → 4 Surau TBR → 5 TAR UMT Gate 4
→ 6 Main Gate → 7 Gate 2 → 8 Surau Al-Amin → ... 一路向东走到死胡同
→ 15 Setapak Central → 16 Vista Wirajaya   ← 走到底了
→ 17 AEON Alpha Angle                       ← 回溯！跳回起点方向走另一条分支
→ 18 Wangsa Metroview → ... → 26 Flat S2 Selatan
```

像走迷宫：沿右手边一直走，撞墙才回头。**递归 = Java 自动帮你记「回头路」**（方法调用栈）。

### 6.4 BFS 广度优先（队列，一圈一圈扩散）

```java
visited.add(startName);
queue.addLast(startName);              // 起点入队
while (!queue.isEmpty()) {
    String current = queue.pollFirst();   // 队头出队
    order.add(current);
    for (RouteSegment seg : adjacency.get(current)) {
        if (!visited.contains(neighbour)) {
            visited.add(neighbour);        // 注意：入队时就点名！
            queue.addLast(neighbour);
        }
    }
}
```

**真实运行结果**（从 Setapak Central 出发）：

```
第 1 圈(直连):  1 Setapak Central → 2 Columbia Hospital → 3 Vista Wirajaya
第 2 圈(隔一站): 4 PV 16 → 5 TAR UMT Gate 2 → 6 PV 10 ...
第 3 圈:        7 Main Gate → 8 Surau Al-Amin → 9 PV 12 ...
```

像丢石头进水里的波纹，**按「经过几段路」从近到远**访问。

### 6.5 visited 为什么是救命稻草（Viva 必考）

T250 是**环线**：LRT Wangsa Maju → ... → Flat S2 Selatan → **Wangsa Metroview → ... → 回到 LRT Wangsa Maju**。
如果不标记 visited，DFS 会沿着环**永远转圈**，程序卡死（StackOverflowError）。

两个细节：
- DFS 在**递归前**标记（`visited.add` 在 for 循环前面）——防止同一个站被递归两次
- BFS 在**入队时**标记（不是出队时）——防止同一个站进队列两次

### 6.6 时间复杂度 O(V + E) 怎么来的

- 每个顶点只被访问 **1 次**：贡献 V
- 每条边被扫 **2 次**（无向图，从两端各扫一遍）：贡献 E
- visited 查询是 HashSet，O(1)
- 合计 **O(V + E)**。我们的图：O(26 + 28) = O(54) 步，瞬间完成

对比：如果用矩阵存图，找邻居要扫整行，就变成 O(V²) = 676 步。

### 6.7 图操作的数据完整性（Rubric 20 分的重点）

| 规则 | 代码在哪 | 为什么 |
|---|---|---|
| 不能加重复站名 | `addStop` 查 `containsKey` | 两个同名站会让字典冲突 |
| 不能加自环（A→A） | `addSegment` 查 `equals` | 巴士站到自己没有意义 |
| 不能加重复边 | `hasDirectSegment` 扫邻居表 | 一条路存两份会让遍历重复计数 |
| **删站必须删光相连的边** | `removeStop` 遍历所有站 `removeIf` | 否则留下**悬挂边**（指向不存在的站），遍历时 NullPointerException |
| 删边两边一起删 | `removeSegment` 删 A 表也删 B 表 | 无向图边存两份，只删一份 = 数据不一致 |

---

## 7. Viva 英文问答

> 每题答案 2-3 句，背熟。括号里是中文提示。

1. **Q: Why did you choose an adjacency list instead of an adjacency matrix?**
   A: A bus network is a sparse graph — each stop connects to only 2-3 neighbours.
   The adjacency list needs O(V+E) memory, while a matrix wastes O(V²) cells.
   Scanning a stop's neighbours is also faster: O(degree) instead of O(V).
   （稀疏图省内存，找邻居快）

2. **Q: Why is your graph undirected?**
   A: Buses travel in both directions on every road link, so each segment is
   stored in BOTH endpoints' adjacency lists. （巴士双向行驶，边存两端）

3. **Q: What does the weight represent?**
   A: The approximate road distance in kilometres between two neighbouring stops.
   （权重 = 两站间的近似公路距离）

4. **Q: Explain how DFS works.**
   A: DFS starts at a stop, marks it visited, then recursively visits the first
   unvisited neighbour, going as deep as possible before backtracking.
   It uses an implicit stack — the method call stack. （走到底再回头，栈记路）

5. **Q: Explain how BFS works.**
   A: BFS uses a queue. It enqueues the start stop, then repeatedly dequeues a
   stop and enqueues all its unvisited neighbours, exploring level by level —
   nearest stops first. （队列，一圈一圈由近到远）

6. **Q: Why do you need a visited set?**
   A: The T250 route is a loop. Without marking visited stops, traversal would
   cycle forever and crash with StackOverflowError. （环线不标记就死循环）

7. **Q: What is the time complexity of DFS and BFS, and why?**
   A: O(V + E). Each vertex is visited exactly once and each edge is examined
   twice (once from each endpoint, because the graph is undirected).
   （每点一次每边两次）

8. **Q: What happens when you remove a vertex? How do you keep data integrity?**
   A: We must also remove ALL segments connected to it from every neighbour's
   list, otherwise dangling edges remain and traversal would crash with a
   NullPointerException. （删站清边，防悬挂边）

9. **Q: How do you prevent invalid edges, like self-loops or duplicates?**
   A: addSegment checks: both stops must exist, the two stops must be different,
   and hasDirectSegment scans the neighbour list first. It returns error codes
   that the UI translates into friendly messages. （三道检查 + 错误码）

10. **Q: Why did you use LinkedHashMap and not HashMap?**
    A: LinkedHashMap keeps insertion order, so the console display and the DFS/BFS
    visit order are stable and easy to verify during testing. （保序，显示稳定）

11. **Q: Which Java collection do you use as the BFS queue?**
    A: ArrayDeque — addLast to enqueue, pollFirst to dequeue, both O(1).
    （ArrayDeque 两端操作 O(1)）

12. **Q: What if the network is disconnected?**
    A: The traversal still returns the reachable component, and the program
    reports how many stops were UNREACHABLE from the starting stop.
    （返回可达部分 + 提示几个站到不了）

13. **Q: Why is removeStop O(V + E)?**
    A: To keep integrity we scan every stop's adjacency list to delete segments
    pointing to the removed stop — that touches all vertices and edges once.
    （要扫全表清边）

14. **Q: How does the JavaFX view reflect a traversal?**
    A: After DFS/BFS, Main passes the visit order to GraphView. Visited stops are
    repainted yellow with numbered badges, and consecutive travelled segments are
    drawn thicker in green, so the visit order can be verified visually.
    （黄色高亮 + 序号徽章 + 绿色路径）

15. **Q: What are the assumptions of your model?**
    A: Stops on opposite sides of the road with the same name are merged into one
    vertex; distances are approximate and total about 13.1 km against the
    published 13.9 km; buses travel both ways, hence the undirected graph.
    （同名站合并/距离近似/双向）

16. **Q: If you had to find the shortest path between two stops, which traversal would you use?**
    A: BFS — on an unweighted view of the graph, the first time BFS reaches a stop
    is via the fewest segments. For real shortest distance we would upgrade to
    Dijkstra's algorithm with the km weights. （BFS 最少段数；真最短路要 Dijkstra）

---

## 8. 常见报错速查表

| 报错（关键词） | 原因 | 解法 |
|---|---|---|
| `Cannot run program "...javac"` / `invalid flag: --release` | JDK 太旧（<17） | 装 JDK 21（adoptium.net），Tools → Java Platforms 注册，项目 Properties 里选它 |
| `JavaFX runtime components are missing` | 直接 Run 了单个 .java 文件 | 要右键**项目** → Run；或 Custom → Goals 输入 `javafx:run` |
| `Could not resolve dependencies` / `Failed to read artifact descriptor` | 依赖没下载完 / 断网 | 检查网络，右键项目 → `Clean and Build` 重试 |
| `The build could not be completed... proxy` | 校园网/公司网拦 Maven 仓库 | 换手机热点重试一次即可 |
| `error: release version 17 not supported` | NetBeans 用的 JDK < 17 | Tools → Java Platforms 注册新 JDK，右键项目 → Properties → Build → Compile → Java Platform 选它 |
| F6 没反应或弹的不是本项目 | 选错窗口/项目 | 点一下项目名再按 F6；确认标题栏是本项目 |
| 地图窗口开了但是空白 | 显卡驱动问题（少见） | 换台电脑演示，或更新显卡驱动 |
| `class Main is public, should be declared in a file named Main.java` | 文件被改名了 | 文件名必须和类名一模一样（git 里恢复即可） |

---

## 9. 建议分工与演示流程

### 分工（4 人，Viva 每人主讲自己的部分）

| 成员 | 主讲模块 | 必须能白板画出的图 |
|---|---|---|
| A | `Stop` + `RouteSegment` + `T250Data`（数据层） | 一个站对象里有什么；种子数据怎么进图 |
| B | `BusRouteGraph` 的图操作（增删顶点/边） | 删一个中间站前后，邻接表的变化 |
| C | `BusRouteGraph` 的 DFS + BFS | 同一个起点两种遍历的访问顺序对比 |
| D | `Main`（菜单/验证）+ `GraphView`/`NetworkViewer`（画图） | 用户输入怎么变成图上的高亮 |

> 所有人都要会：第 6 节的概念 + 第 7 节全部 16 题（tutor 会随机抽人）。

### 演示流程（5-8 分钟，照着走）

1. `1` Create Graph → `6` 载入 T250 数据（报出 26 站 / 28 段 / 13.1 km）
2. `3` 打开地图：指橙色 = 枢纽站，蓝 = 普通站，线上数字 = 公里
3. `2` → `2` DFS 从 `LRT Wangsa Maju`：念前 5 站顺序，切地图看黄色高亮和序号徽章
4. `2` → `3` BFS 同起点：对比「DFS 一条路走到底，BFS 一圈一圈」
5. 稳定性演示：加重复站名 → 被拒；加自环 → 被拒；删 `TAR UMT Gate 4` → `5` 显示邻接表证明相连的边全没了
6. 搜不存在的站 → 展示「Did you mean」建议

### 组内 Onboarding 会议建议（90 分钟）

1. **0-15 min**：所有人按第 1/2 节把项目跑起来（卡住的看第 8 节）
2. **15-40 min**：主讲人（你）带读第 3/4 节 + 现场演示一遍
3. **40-70 min**：每人认领模块，精读自己的文件 + 第 5/6 节对应小节
4. **70-90 min**：互相抽问第 7 节的 16 题，答不出的标红，会后再考
