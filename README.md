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
| 1 → 1 | 添加巴士站（顶点），查重、可设 hub | Graph Operations (20%) |
| 1 → 2 | 删除巴士站（顶点），自动清理所有相连边（数据完整性） | 同上 |
| 1 → 3 | 添加路线段（边，带 km 权重），禁止自环/重复边 | 同上 |
| 1 → 4 | 删除路线段（双边同步删除） | 同上 |
| 1 → 5 | 邻接表文本显示全网络 | Program Output (5%) |
| 1 → 6 | 一键载入真实 T250 全部 26 站 28 段数据 | Completeness (15%) |
| 1 → 7 | 清空网络（二次确认） | Graph Operations |
| 2 → 1 | 站点搜索（详情 + 直连列表 + “did you mean” 建议） | Program Output |
| 2 → 2 | **DFS 遍历**（递归实现，打印访问顺序表） | Traversal (10%) |
| 2 → 3 | **BFS 遍历**（队列实现，逐层访问） | Traversal (10%) |
| 3 | JavaFX 地图窗口：站点圆圈 + 边 + 距离标注 + 图例 | Program Output |

遍历后若地图窗口开着，会自动刷新：被访问站点变黄并带序号徽章，
走过的路段加粗变绿，直观验证遍历结果。

## 项目结构（Maven 标准布局）

```
rapidkl-t250/
├── pom.xml                              Maven 配置（JavaFX 21 + javafx-maven-plugin）
├── src/main/java/com/rapidkl/t250/
│   ├── Main.java            控制台菜单与输入验证（程序入口）
│   ├── BusRouteGraph.java   图 ADT：邻接表 + 图操作 + DFS/BFS
│   ├── Stop.java            顶点类（站名 / 官方站码 / 是否枢纽）
│   ├── RouteSegment.java    边类（目的地 + 距离权重）
│   ├── T250Data.java        真实 T250 种子数据（26 站 / 28 边）
│   ├── GraphView.java       JavaFX 绘图面板
│   └── NetworkViewer.java   JavaFX 窗口（独立线程，控制台不被阻塞）
├── docs/REPORT-NOTES.md     报告素材：引言 / 伪代码 / Big-O 分析 / 参考文献
├── test-tools/              开发期辅助工具（可选，不参与 Maven 构建）
├── compile.bat / run.bat
```

## Viva 演示建议流程

1. `1` Create Graph → `6` 载入 T250 数据（26 站 / 28 段 ≈13.1 km）
2. `3` 打开地图窗口，指认枢纽站（橙色）与普通站
3. 回控制台 `2` Search → `2` DFS from `LRT Wangsa Maju` → 切回地图看黄色高亮路径
4. `3` BFS 对比同一站的访问顺序差异（DFS 深入优先 vs BFS 近邻优先）
5. 演示图操作健壮性：
   - 添加重复站名 → 报“已存在”
   - 添加已存在的边 → 报“已有直达路线”；自环 → 拒绝
   - 删除一个中间站（如 TAR UMT Gate 4）→ 再显示邻接表，证明相关边全部同步删除、无悬挂边
6. `2` Search → `1` 搜索不存在的站 → 展示模糊建议功能

## 数据说明与假设

路线结构来自 Rapid KL 官方接驳线 T250（环线，全程约 13.9 km）。
建模假设（报告中需写明）：

1. 往返同名车站合并为同一个顶点（如 TAR UMT 各大门只算一站）；
2. 每段距离为近似值，全网合计约 13.1 km；
3. 巴士可双向行驶，因此图是无向的；每条边在两个端点的邻接表中各存一次。

> 注意：`test-tools/SnapshotTool.java` 只是开发期截图工具，
> 打包提交源代码时可以不带它。
