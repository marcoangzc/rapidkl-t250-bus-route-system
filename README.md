# Rapid KL Bus Route System — Route T250

AMCS2034 数据结构作业项目：用 **Java + JavaFX** 把 Rapid KL 接驳巴士 **T250 线**
（LRT Wangsa Maju ⇄ Setapak Sentral，吉隆坡）建模为**无向带权图**，
支持图操作、DFS/BFS 遍历和图形化网络地图。

## 运行前准备

- 已安装 **JDK 17 或以上**（开发环境：Oracle JDK 25）
- **Apache Maven**（装了 Apache NetBeans 就自带，无需另装；或从 maven.apache.org 下载）
- JavaFX 依赖由 Maven 按 `pom.xml` 自动下载，**无需手动管理 jar**

> 🎓 **组员请看 [ONBOARDING.md](ONBOARDING.md)**——NetBeans 部署步骤、
> 零基础 Java/DSA 补课、Viva 英文问答 16 题全在里面。

## 编译与运行

本项目是标准 **Maven 项目**（Java with Maven + Simple JavaFX 结构），NetBeans 打开即用。

方式一（最简单）——双击（自动寻找 Maven，含 NetBeans 自带版）：

```
compile.bat   → 编译
run.bat       → 启动系统（mvn javafx:run）
```

方式二 —— 命令行：

```bash
mvn clean compile     # 编译
mvn javafx:run        # 运行（JavaFX 模块路径自动配置）
```

方式三 —— NetBeans：`File → Open Project` 选中本项目文件夹（含 pom.xml
即被识别为 Maven 项目）→ 等右下角依赖下载完成 → 右键项目 `Run`。
（若 F6 无反应：右键项目 → `Custom → Goals...` 输入 `javafx:run`。）

## 功能一览（对照评分标准）

| 菜单 | 功能 | Rubric 对应 |
|---|---|---|
| 启动 | **自动载入完整真实 T250 数据**（26 站 28 段 ≈13.1 km），无需逐站添加 | Completeness (15%) |
| 1 → 1 | 添加巴士站（顶点），查重、可设 hub | Graph Operations (20%) |
| 1 → 2 | 删除巴士站（顶点），自动清理所有相连边（数据完整性） | 同上 |
| 1 → 3 | 添加路线段（边，带 km 权重），禁止自环/重复边 | 同上 |
| 1 → 4 | 删除路线段（双边同步删除） | 同上 |
| 1 → 5 | 邻接表文本显示全网络 | Program Output (5%) |
| 1 → 6 | 清空网络（二次确认，可从零演示建图） | Graph Operations |
| 2 | **DFS 搜索**：输入起点 + 终点（goal state），找到即停并报告路径、段数、总公里数 | Traversal (10%) |
| 2 | **BFS 搜索**：同上，且保证「最少段数」路径；也可不输终点做全网遍历 | Traversal (10%) |
| 3 | JavaFX 地图窗口：站点圆圈 + 边 + 距离标注 + 图例 | Program Output |

> 💡 演示亮点：同一对起终点跑 DFS 和 BFS，DFS 会沿一条支路绕远
> （如 14 段），BFS 给出最少段数的路径（如 7 段）——直观展示两种
> 遍历的差异。老师已同意在真实 T250 之外自由加边开分支做测试。

**面向对象结构**（满足老师 interface / abstract / concrete 三类文件要求）：
`GraphADT`（接口）→ `BusRouteGraph`（具体实现类）；
`GraphTraversal`（抽象类）→ `DepthFirstSearch` / `BreadthFirstSearch`（具体子类）。

遍历后若地图窗口开着，会自动刷新：被访问站点变黄并带序号徽章，
走过的路段加粗变绿，直观验证遍历结果。

## 项目结构（Maven 标准布局）

```
rapidkl-t250/
├── pom.xml                              Maven 配置（JavaFX 21 + javafx-maven-plugin）
├── src/main/java/com/rapidkl/t250/
│   ├── GraphADT.java        【接口】图操作合同（顶点/边/查询/遍历）
│   ├── GraphTraversal.java  【抽象类】DFS/BFS 公共骨架（模板方法模式）
│   ├── DepthFirstSearch.java   【具体类】DFS 递归核心
│   ├── BreadthFirstSearch.java 【具体类】BFS 队列核心
│   ├── BusRouteGraph.java   【具体类】邻接表图实现（图操作）
│   ├── Stop.java            顶点类（站名 / 官方站码 / 是否枢纽）
│   ├── RouteSegment.java    边类（目的地 + 距离权重）
│   ├── T250Data.java        真实 T250 种子数据（26 站 / 28 边）
│   ├── Main.java            控制台菜单与输入验证（程序入口）
│   ├── GraphView.java       JavaFX 绘图面板
│   └── NetworkViewer.java   JavaFX 窗口（独立线程，控制台不被阻塞）
├── docs/REPORT-NOTES.md     报告素材：引言 / 伪代码 / Big-O 分析 / 参考文献
├── test-tools/              开发期辅助工具（可选，不参与 Maven 构建）
├── compile.bat / run.bat
```

## Viva 演示建议流程

1. 启动即自动载入 T250 数据；`1` → `5` 显示邻接表确认（26 站 / 28 段 ≈13.1 km）
2. `3` 打开地图：指橙色 = 枢纽站，蓝 = 普通站，线上数字 = 公里
3. `2` Route Search → `1` DFS：起点 `LRT Wangsa Maju`，终点 `Setapak Central` → 念出路径和段数，切地图看黄色高亮
4. `2` → `2` BFS 同样起终点：对比段数（BFS 明显更少 = 最少段数保证）
5. `2` → `2` BFS 终点留空回车：全网遍历模式，看逐层扩散的访问顺序
6. 稳定性演示：加重复站名 → 被拒；自环 → 拒绝；删中间站 → 邻接表证明相连边全清
7. 自由分支演示（老师已批准）：加几条 T250 之外的边，再跑 BFS 看最短路变化

## 数据说明与假设

路线结构来自 Rapid KL 官方接驳线 T250（环线，全程约 13.9 km）。
建模假设（报告中需写明）：

1. 往返同名车站合并为同一个顶点（如 TAR UMT 各大门只算一站）；
2. 每段距离为近似值，全网合计约 13.1 km；
3. 巴士可双向行驶，因此图是无向的；每条边在两个端点的邻接表中各存一次。

> 注意：`test-tools/SnapshotTool.java` 只是开发期截图工具，
> 打包提交源代码时可以不带它。
