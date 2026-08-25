import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GraphView - the JavaFX panel that DRAWS the bus route network as a graph.
 *
 * Every Stop is drawn as a coloured circle (vertex) and every RouteSegment as
 * a line with its distance written beside it (weighted edge). This gives the
 * examiner an instant picture of the network structure, and after a DFS/BFS
 * traversal the visited stops are repainted yellow with a numbered badge so
 * the visit order can be verified visually.
 *
 * Positions of the default T250 stops are hand-placed in a schematic layout;
 * any user-created stop that has no stored position is arranged automatically
 * on a large circle around the centre.
 */
public final class GraphView extends Pane {

    // ------------------------------------------------------------------ //
    //  Visual constants                                                   //
    // ------------------------------------------------------------------ //
    private static final double MAP_WIDTH  = 1150;
    private static final double MAP_HEIGHT = 740;

    private static final Color BG_COLOR         = Color.web("#fbfdff");
    private static final Color EDGE_COLOR       = Color.web("#90a4ae");
    private static final Color WEIGHT_COLOR     = Color.web("#546e7a");
    private static final Color LABEL_COLOR      = Color.web("#37474f");
    private static final Color STOP_NORMAL      = Color.web("#3498db");
    private static final Color STOP_HUB         = Color.web("#e67e22");
    private static final Color STOP_VISITED     = Color.web("#f1c40f");
    private static final Color BADGE_COLOR      = Color.web("#27ae60");

    private static final double R_NORMAL  = 13;
    private static final double R_HUB     = 16;

    /** Schematic (hand-tuned) positions of the default T250 stops. */
    private static final Map<String, double[]> COORDINATES = new HashMap<>();
    /** Shorter display names so labels never overlap on the map. */
    private static final Map<String, String> SHORT_LABELS = new HashMap<>();

    static {
        COORDINATES.put("LRT Wangsa Maju",              new double[]{300, 645});
        COORDINATES.put("Tar Villa Setapak",            new double[]{380, 600});
        COORDINATES.put("PULAPOT",                      new double[]{450, 555});
        COORDINATES.put("Surau Taman Bunga Raya",       new double[]{520, 510});
        COORDINATES.put("TAR UMT Gate 4",               new double[]{585, 465});
        COORDINATES.put("TAR UMT Main Gate",            new double[]{650, 435});
        COORDINATES.put("TAR UMT Gate 2",               new double[]{720, 405});
        COORDINATES.put("Surau Al-Amin",                new double[]{785, 370});
        COORDINATES.put("Indah Apartments",             new double[]{830, 335});
        COORDINATES.put("SK Danau Kota",                new double[]{875, 295});
        COORDINATES.put("PV 12 Platinum Lake",          new double[]{920, 245});
        COORDINATES.put("PV 10 Platinum Lake",          new double[]{955, 190});
        COORDINATES.put("PV 16 Platinum Lake",          new double[]{990, 135});
        COORDINATES.put("Columbia Hospital Danau Kota", new double[]{1050, 165});
        COORDINATES.put("Setapak Central",              new double[]{1075, 235});
        COORDINATES.put("Vista Wirajaya",               new double[]{1000, 345});
        COORDINATES.put("AEON Alpha Angle",             new double[]{180, 588});
        COORDINATES.put("Wangsa Metroview",             new double[]{130, 510});
        COORDINATES.put("Desa Setapak",                 new double[]{145, 425});
        COORDINATES.put("Flat WM Sec 2 (Timur)",        new double[]{240, 345});
        COORDINATES.put("Pasar & Penjaja WM Sec 2",     new double[]{295, 300});
        COORDINATES.put("Flat WM Sec 2 (Utara)",        new double[]{370, 285});
        COORDINATES.put("Flat WM Sec 2 (Barat)",        new double[]{448, 298});
        COORDINATES.put("Hospital Tentera (Utara)",     new double[]{505, 340});
        COORDINATES.put("Hospital Tentera (Selatan)",   new double[]{525, 400});
        COORDINATES.put("Flat WM Sec 2 (Selatan)",      new double[]{480, 450});

        SHORT_LABELS.put("Tar Villa Setapak",            "Tar Villa");
        SHORT_LABELS.put("Surau Taman Bunga Raya",       "Surau TBR");
        SHORT_LABELS.put("Columbia Hospital Danau Kota", "Columbia Hospital");
        SHORT_LABELS.put("Pasar & Penjaja WM Sec 2",     "Pasar & Penjaja S2");
        SHORT_LABELS.put("Flat WM Sec 2 (Timur)",        "Flat S2 Timur");
        SHORT_LABELS.put("Flat WM Sec 2 (Utara)",        "Flat S2 Utara");
        SHORT_LABELS.put("Flat WM Sec 2 (Barat)",        "Flat S2 Barat");
        SHORT_LABELS.put("Flat WM Sec 2 (Selatan)",      "Flat S2 Selatan");
        SHORT_LABELS.put("Hospital Tentera (Utara)",     "Hosp. Tentera (U)");
        SHORT_LABELS.put("Hospital Tentera (Selatan)",   "Hosp. Tentera (S)");
        SHORT_LABELS.put("PV 12 Platinum Lake",          "PV 12");
        SHORT_LABELS.put("PV 10 Platinum Lake",          "PV 10");
        SHORT_LABELS.put("PV 16 Platinum Lake",          "PV 16");
    }

    private final BusRouteGraph graph;

    /** Latest DFS/BFS result to overlay on the map (empty = no highlight). */
    private List<String> traversalOrder = List.of();
    private String traversalType = "";

    public GraphView(BusRouteGraph graph) {
        this.graph = graph;
        setPrefSize(MAP_WIDTH, MAP_HEIGHT);
        setStyle("-fx-background-color: " + toCss(BG_COLOR) + ";");
        redraw();
    }

    /** Stores a traversal result so it can be painted onto the network. */
    public void setTraversal(List<String> order, String type) {
        this.traversalOrder = (order == null) ? List.of() : order;
        this.traversalType = (type == null) ? "" : type;
    }

    /** Clears everything and repaints the whole network from current data. */
    public void redraw() {
        getChildren().clear();

        if (graph.isEmpty()) {
            Text hint = new Text("The route network is empty.\n"
                    + "Use Main Menu -> Create Graph to add bus stops.");
            hint.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
            hint.setFill(Color.GRAY);
            hint.setTextAlignment(TextAlignment.CENTER);
            hint.setX(MAP_WIDTH / 2 - 170);
            hint.setY(MAP_HEIGHT / 2);
            getChildren().add(hint);
            return;
        }

        drawSegments();
        drawStops();
    }

    // ------------------------------------------------------------------ //
    //  Edges                                                              //
    // ------------------------------------------------------------------ //

    private void drawSegments() {
        // Pairs already painted (each undirected edge must be drawn ONCE).
        Set<String> painted = new HashSet<>();

        // Consecutive stop pairs of the last traversal -> highlighted edges.
        Set<String> traversedPairs = collectTraversedPairs();

        for (String stopName : graph.getStopNames()) {
            for (RouteSegment seg : graph.getNeighbours(stopName)) {
                String other = seg.getDestination();
                String pairKey = pairKey(stopName, other);
                if (!painted.add(pairKey)) {
                    continue;                        // opposite direction already drawn
                }

                double[] p1 = locate(stopName);
                double[] p2 = locate(other);

                boolean highlight = traversedPairs.contains(pairKey);
                Line line = new Line(p1[0], p1[1], p2[0], p2[1]);
                line.setStroke(highlight ? BADGE_COLOR : EDGE_COLOR);
                line.setStrokeWidth(highlight ? 4.0 : 2.0);
                line.setOpacity(highlight ? 1.0 : 0.85);
                getChildren().add(line);

                // Distance label beside the middle of the segment: steep edges
                // get it on the right, horizontal edges above the midpoint.
                double midX = (p1[0] + p2[0]) / 2;
                double midY = (p1[1] + p2[1]) / 2;
                boolean steep = Math.abs(p2[1] - p1[1]) > Math.abs(p2[0] - p1[0]);
                double weightX = steep ? midX + 6 : midX - 20;
                double weightY = steep ? midY + 4 : midY - 7;
                addWeightLabel(String.format("%.1f km", seg.getDistanceKm()),
                        weightX, weightY, highlight);
            }
        }
    }

    /** "A|B" keys of every consecutive pair in the last DFS/BFS visit order. */
    private Set<String> collectTraversedPairs() {
        Set<String> pairs = new HashSet<>();
        for (int i = 0; i + 1 < traversalOrder.size(); i++) {
            String a = traversalOrder.get(i);
            String b = traversalOrder.get(i + 1);
            if (graph.hasDirectSegment(a, b)) {      // skip backtracking jumps
                pairs.add(pairKey(a, b));
            }
        }
        return pairs;
    }

    private static String pairKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
    }

    /**
     * Draws one distance label with a translucent white plate behind it, so
     * the km value stays readable even where edges and labels cross - the
     * same trick used for road labels on real maps.
     *
     * @param x left edge of the text, @param y baseline of the text
     */
    private void addWeightLabel(String text, double x, double y, boolean highlight) {
        Text weight = new Text(text);
        weight.setFont(Font.font("Consolas", FontWeight.NORMAL, 11));
        weight.setFill(highlight ? BADGE_COLOR : WEIGHT_COLOR);

        double w = weight.getBoundsInLocal().getWidth();
        double h = weight.getBoundsInLocal().getHeight();

        Rectangle plate = new Rectangle(x - 3, y - h + 3, w + 6, h - 1);
        plate.setFill(Color.rgb(255, 255, 255, 0.85));
        plate.setArcWidth(4);
        plate.setArcHeight(4);
        getChildren().addAll(plate, weight);

        weight.setX(x);
        weight.setY(y);
    }

    // ------------------------------------------------------------------ //
    //  Vertices                                                           //
    // ------------------------------------------------------------------ //

    private void drawStops() {
        // Order number of every visited stop, e.g. "LRT Wangsa Maju" -> 1.
        Map<String, Integer> visitIndex = new HashMap<>();
        for (int i = 0; i < traversalOrder.size(); i++) {
            visitIndex.put(traversalOrder.get(i), i + 1);
        }

        for (String name : graph.getStopNames()) {
            Stop stop = graph.getStop(name);
            double[] pos = locate(name);
            boolean visited = visitIndex.containsKey(name);

            double radius = stop.isHub() ? R_HUB : R_NORMAL;
            Color fill = visited ? STOP_VISITED
                    : (stop.isHub() ? STOP_HUB : STOP_NORMAL);

            Circle circle = new Circle(pos[0], pos[1], radius);
            circle.setFill(fill);
            circle.setStroke(Color.web("#263238"));
            circle.setStrokeWidth(1.6);
            DropShadow shadow = new DropShadow();
            shadow.setRadius(6);
            shadow.setOffsetY(2);
            shadow.setColor(Color.rgb(0, 0, 0, 0.25));
            circle.setEffect(shadow);
            getChildren().add(circle);

            // Numbered badge showing the position in the DFS/BFS visit order.
            if (visited) {
                int orderNo = visitIndex.get(name);
                Circle badge = new Circle(pos[0] + radius + 4, pos[1] - radius - 4, 10);
                badge.setFill(BADGE_COLOR);
                badge.setStroke(Color.WHITE);
                badge.setStrokeWidth(1.5);
                getChildren().add(badge);

                Text num = new Text(String.valueOf(orderNo));
                num.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                num.setFill(Color.WHITE);
                num.setX(pos[0] + radius + 4 - num.getBoundsInLocal().getWidth() / 2);
                num.setY(pos[1] - radius - 4 + 4);
                getChildren().add(num);
            }

            // Name label centred under the circle.
            String label = SHORT_LABELS.getOrDefault(name, name);
            Text text = new Text(label);
            text.setFont(Font.font("Segoe UI",
                    stop.isHub() ? FontWeight.BOLD : FontWeight.NORMAL, 12));
            text.setFill(LABEL_COLOR);
            text.setX(pos[0] - text.getBoundsInLocal().getWidth() / 2);
            text.setY(pos[1] + radius + 17);
            getChildren().add(text);
        }
    }

    /**
     * Returns the drawing position of one stop: the hand-tuned coordinate if
     * defined, otherwise an automatic spot on a big circle around the centre
     * (so user-added stops still appear neatly instead of stacking up).
     */
    private double[] locate(String stopName) {
        double[] fixed = COORDINATES.get(stopName);
        if (fixed != null) {
            return fixed;
        }
        // Automatic placement: sunflower arrangement around the map centre.
        List<String> names = graph.getStopNames();
        int extra = 0;
        for (String n : names) {
            if (!COORDINATES.containsKey(n)) {
                if (n.equals(stopName)) {
                    break;
                }
                extra++;
            }
        }
        double angle = Math.toRadians(extra * 137.5 - 90);
        double radius = 330;
        return new double[]{
                MAP_WIDTH / 2 + radius * Math.cos(angle),
                MAP_HEIGHT / 2 + radius * Math.sin(angle) * 0.85
        };
    }

    private static String toCss(Color color) {
        return "#" + color.toString().substring(2, 8);
    }
}
