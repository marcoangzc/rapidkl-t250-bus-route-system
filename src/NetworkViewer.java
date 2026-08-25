import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

/**
 * NetworkViewer - the JavaFX window that displays the bus route network.
 *
 * The window is opened from the console menu (option 3). It runs on its own
 * JavaFX thread so that the console menu keeps working while the map is on
 * screen. When a DFS/BFS traversal is executed afterwards, the SAME window
 * is refreshed automatically: visited stops turn yellow with numbered badges
 * and the travelled segments are highlighted in green.
 */
public class NetworkViewer extends Application {

    private static BusRouteGraph sharedGraph;                 // graph being displayed
    private static List<String> sharedTraversal = List.of();  // last DFS/BFS order
    private static String sharedTraversalType = "";

    private static boolean launched = false;
    private static Stage stage;
    private static GraphView graphView;

    /** Called by the console UI whenever the map should be (re)displayed. */
    public static void showNetwork(BusRouteGraph graph, List<String> order, String type) {
        sharedGraph = graph;
        sharedTraversal = (order == null) ? List.of() : order;
        sharedTraversalType = (type == null) ? "" : type;

        if (!launched) {
            launched = true;
            Thread fxThread = new Thread(() -> Application.launch(NetworkViewer.class));
            fxThread.setDaemon(true);   // lets the JVM exit when the console closes
            fxThread.start();
        } else {
            Platform.runLater(NetworkViewer::refreshOnFxThread);
        }
    }

    /** True when the map window is currently visible (used for auto-refresh). */
    public static boolean isWindowOpen() {
        return launched && stage != null && stage.isShowing();
    }

    /** Must run on the JavaFX thread: repaint and bring the window forward. */
    private static void refreshOnFxThread() {
        if (!stage.isShowing()) {
            stage.show();               // user closed it before - open again
        }
        graphView.setTraversal(sharedTraversal, sharedTraversalType);
        graphView.redraw();
        stage.setIconified(false);
        stage.toFront();
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        graphView = new GraphView(sharedGraph);
        graphView.setTraversal(sharedTraversal, sharedTraversalType);
        graphView.redraw();   // repaint WITH the traversal highlight (the
                              // constructor already drew the plain network)

        BorderPane root = new BorderPane();

        Label header = new Label("Rapid KL Bus Route Network - Route T250"
                + " (LRT Wangsa Maju <-> Setapak Sentral)");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        header.setTextFill(Color.web("#263238"));
        BorderPane.setMargin(header, new Insets(12, 12, 4, 12));

        ScrollPane scrollPane = new ScrollPane(graphView);
        scrollPane.setPadding(new Insets(0, 8, 0, 8));

        root.setTop(header);
        root.setCenter(scrollPane);
        root.setBottom(buildLegend());

        Scene scene = new Scene(root, 1200, 810);
        primaryStage.setTitle("Rapid KL T250 Bus Route System - Route Network View");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /** Small colour legend explaining every symbol used on the map. */
    private HBox buildLegend() {
        HBox legend = new HBox(18);
        legend.setPadding(new Insets(6, 14, 10, 14));

        legend.getChildren().add(makeCircleLegend(Color.web("#3498db"), "Bus stop"));
        legend.getChildren().add(makeCircleLegend(Color.web("#e67e22"), "Interchange / hub"));
        legend.getChildren().add(makeCircleLegend(Color.web("#f1c40f"), "Visited by DFS / BFS"));
        legend.getChildren().add(makeLineLegend(Color.web("#90a4ae"), 2.0, "Route segment"));
        legend.getChildren().add(makeLineLegend(Color.web("#27ae60"), 4.0,
                "Travelled path of last traversal"));

        Label note = new Label("Numbers beside stops show the visit order.");
        note.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 12));
        note.setTextFill(Color.web("#607d8b"));
        legend.getChildren().add(note);

        return legend;
    }

    private HBox makeCircleLegend(Color color, String text) {
        Circle dot = new Circle(9, color);
        dot.setStroke(Color.web("#263238"));
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", 13));
        HBox box = new HBox(6, dot, label);
        return box;
    }

    private HBox makeLineLegend(Color color, double width, String text) {
        Line line = new Line(0, 0, 26, 0);
        line.setStroke(color);
        line.setStrokeWidth(width);
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", 13));
        HBox box = new HBox(6, line, label);
        return box;
    }
}
