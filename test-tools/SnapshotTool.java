import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * SnapshotTool - DEVELOPMENT HELPER ONLY (not part of the assignment).
 *
 * Renders the seeded T250 network headlessly (Monocle) and saves a PNG so
 * the map layout can be checked without opening a real window.
 *
 * Run with:
 *   java --module-path lib --add-modules javafx.controls,javafx.swing \
 *        -Dglass.platform=Monocle -Dmonocle.platform=Headless -Dprism.order=sw \
 *        -cp out-test SnapshotTool
 */
public class SnapshotTool {

    public static void main(String[] args) throws Exception {
        BusRouteGraph graph = new BusRouteGraph();
        T250Data.loadDefaultNetwork(graph);

        CountDownLatch done = new CountDownLatch(1);
        Platform.startup(() -> { });
        Platform.runLater(() -> {
            try {
                GraphView view = new GraphView(graph);
                view.setTraversal(graph.depthFirstSearch("LRT Wangsa Maju"), "DFS");
                view.redraw();   // repaint with the DFS highlight applied
                Scene scene = new Scene(view);
                view.applyCss();
                view.layout();
                ImageIO.write(SwingFXUtils.fromFXImage(scene.snapshot(null), null),
                        "png", new File("snapshot.png"));
                System.out.println("SNAPSHOT_OK");
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                done.countDown();
            }
        });
        done.await();
        Platform.exit();
    }
}
