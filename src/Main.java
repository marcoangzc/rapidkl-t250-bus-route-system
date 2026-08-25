import java.util.List;
import java.util.Scanner;

/**
 * Main - console driver of the Rapid KL Bus Route System (Route T250).
 *
 * The program follows the classic three-part menu flow of the assignment
 * sample output:
 *
 *      1. Create Graph        -> graph operations  (add/remove vertices/edges)
 *      2. Search a Bus Stop   -> DFS / BFS traversal
 *      3. View the Network    -> JavaFX graphical map window
 *
 * The network starts EMPTY so that the examiner can watch every graph
 * operation being performed; the complete real T250 route can be loaded in
 * one step through Create Graph -> option 6.
 */
public class Main {

    private static final Scanner input = new Scanner(System.in);
    private static final BusRouteGraph network = new BusRouteGraph();

    /** Last DFS/BFS result, kept so the map can highlight it again later. */
    private static List<String> lastTraversalOrder = List.of();
    private static String lastTraversalType = "";

    // =====================================================================
    //  Program entry point and main menu
    // =====================================================================

    public static void main(String[] args) {
        printBanner();

        while (true) {
            printMainMenu();
            int choice = readInt("Enter your selection (0 - 3): ");
            System.out.println();

            switch (choice) {
                case 1:
                    createGraphMenu();
                    break;
                case 2:
                    searchMenu();
                    break;
                case 3:
                    viewNetwork();
                    break;
                case 0:
                    System.out.println("Thank you for using the Rapid KL Bus Route "
                            + "System. Goodbye!");
                    return;
                default:
                    System.out.println("[!] Invalid selection. Please enter 0 - 3 only.");
            }
            System.out.println();
        }
    }

    private static void printBanner() {
        System.out.println("============================================================================");
        System.out.println("          RAPID KL BUS ROUTE SYSTEM  --  FEEDER SERVICE ROUTE T250");
        System.out.println("              LRT Wangsa Maju  <->  Setapak Sentral, Kuala Lumpur");
        System.out.println("============================================================================");
        System.out.println("  Graph data structure : Undirected weighted graph (adjacency list)");
        System.out.println("  Traversal algorithms : Depth First Search & Breadth First Search");
        System.out.println("============================================================================");
        System.out.println("[TIP] The network starts EMPTY. Choose 1 (Create Graph), then option 6");
        System.out.println("      to load the complete real T250 route in one step.");
        System.out.println();
    }

    private static void printMainMenu() {
        System.out.println("Main Menu (Press '0' to exit)");
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("     1. Create Graph       (add / remove bus stops & route segments)");
        System.out.println("     2. Search for a Bus Stop    (details / DFS / BFS traversal)");
        System.out.println("     3. View the Rapid KL Bus Route Network   (JavaFX map window)");
        System.out.println("     0. Exit");
        System.out.println("---------------------------------------------------------------------------");
    }

    // =====================================================================
    //  Menu 1 - Create Graph (graph operations)
    // =====================================================================

    private static void createGraphMenu() {
        while (true) {
            System.out.println("# CREATE GRAPH - GRAPH OPERATIONS");
            System.out.println("---------------------------------------------------------------------------");
            System.out.println("     1. Add a bus stop (vertex)");
            System.out.println("     2. Remove a bus stop (vertex)");
            System.out.println("     3. Add a route segment (edge)");
            System.out.println("     4. Remove a route segment (edge)");
            System.out.println("     5. Display the current network (adjacency list)");
            System.out.println("     6. Load the complete T250 default route data");
            System.out.println("     7. Clear the whole network");
            System.out.println("     0. Back to Main Menu");
            System.out.println("---------------------------------------------------------------------------");

            int choice = readInt("Enter your selection (0 - 7): ");

            if (choice == 0) {
                System.out.println();
                return;
            }

            switch (choice) {
                case 1: addStopFlow();        break;
                case 2: removeStopFlow();     break;
                case 3: addSegmentFlow();     break;
                case 4: removeSegmentFlow();  break;
                case 5: displayNetworkFlow(); break;
                case 6: loadDefaultFlow();    break;
                case 7: clearNetworkFlow();   break;
                default:
                    System.out.println("[!] Invalid selection. Please enter 0 - 7 only.");
            }
            System.out.println();
        }
    }

    /** Option 1 - add vertices, repeatable with "Continue? Y/N". */
    private static void addStopFlow() {
        int count = 1;
        boolean again = true;
        while (again) {
            System.out.println();
            System.out.println("#(" + count + ") Add a Bus Stop");
            System.out.println("------------------------------------------------------------------");

            String name = readNonEmptyLine("      Enter the name of the bus stop: ");
            String existing = network.resolveStopName(name);
            if (existing != null) {
                System.out.println("      [!] The bus stop \"" + existing + "\" already exists "
                        + "in the network.");
            } else {
                String code = readLine("      Enter the official stop code (press Enter for '-'): ");
                if (code.isEmpty()) {
                    code = "-";
                }
                boolean hub = readYesNo("      Is this an interchange / hub stop? Y/N: ");
                int result = network.addStop(name, code, hub);
                if (result == BusRouteGraph.RESULT_OK) {
                    System.out.println("      The bus stop \"" + name + "\" has been added "
                            + "to the network.");
                }
            }

            invalidateTraversal();
            count++;
            again = readYesNo("\nContinue? Y/N:");
        }
    }

    /** Option 2 - remove one vertex together with all its edges. */
    private static void removeStopFlow() {
        System.out.println();
        System.out.println("# Remove a Bus Stop");
        System.out.println("------------------------------------------------------------------");

        String name = askExistingStop("Enter the name of the bus stop to remove");
        List<RouteSegment> neighbours = network.getNeighbours(name);

        System.out.println("      \"" + name + "\" currently has " + neighbours.size()
                + " direct route(s). Removing it also removes those segments.");
        if (!readYesNo("      Are you sure you want to remove it? Y/N: ")) {
            System.out.println("      Removal cancelled. Nothing was changed.");
            return;
        }

        int removedSegments = network.removeStop(name);
        if (removedSegments >= 0) {
            System.out.println("      The bus stop \"" + name + "\" and " + removedSegments
                    + " route segment(s) were removed.");
        } else {
            System.out.println("      [!] The bus stop was not found.");  // defensive check
        }
        invalidateTraversal();
    }

    /** Option 3 - add an undirected weighted edge between two existing stops. */
    private static void addSegmentFlow() {
        int count = 1;
        boolean again = true;
        while (again) {
            System.out.println();
            System.out.println("#(" + count + ") Add a Route Segment (Edge)");
            System.out.println("------------------------------------------------------------------");

            String stopA = askExistingStop("Enter the 1st bus stop");
            String stopB = askExistingStop("Enter the 2nd bus stop");
            double km = readPositiveDouble("      Enter the distance between them (km): ");

            int result = network.addSegment(stopA, stopB, km);
            if (result == BusRouteGraph.RESULT_OK) {
                System.out.println("      There is now a direct route between \"" + stopA
                        + "\" and \"" + stopB + "\" (" + String.format("%.1f", km) + " km).");
            } else {
                System.out.println("      [!] " + network.describeResult(result, stopA, stopB));
            }

            invalidateTraversal();
            count++;
            again = readYesNo("\nContinue? Y/N:");
        }
    }

    /** Option 4 - remove an edge from both endpoints. */
    private static void removeSegmentFlow() {
        System.out.println();
        System.out.println("# Remove a Route Segment (Edge)");
        System.out.println("------------------------------------------------------------------");

        String stopA = askExistingStop("Enter the 1st bus stop");
        String stopB = askExistingStop("Enter the 2nd bus stop");

        int result = network.removeSegment(stopA, stopB);
        if (result == BusRouteGraph.RESULT_OK) {
            System.out.println("      The direct route between \"" + stopA + "\" and \""
                    + stopB + "\" has been removed.");
        } else {
            System.out.println("      [!] " + network.describeResult(result, stopA, stopB));
        }
        invalidateTraversal();
    }

    /** Option 5 - text view of the whole structure. */
    private static void displayNetworkFlow() {
        System.out.println();
        if (network.isEmpty()) {
            System.out.println("[!] The route network is currently empty.");
            System.out.println("    Add bus stops first, or load option 6 for the full T250 data.");
            return;
        }
        System.out.println("================= CURRENT ROUTE NETWORK (ADJACENCY LIST) =================");
        System.out.printf ("  Total stops: %-4d | Route segments: %-4d | Total length: %.1f km%n",
                network.getStopCount(), network.getSegmentCount(), network.getTotalDistanceKm());
        System.out.println("---------------------------------------------------------------------------");
        System.out.print(network.formatAdjacencyList());
        System.out.println("===========================================================================");
    }

    /** Option 6 - reset and reload the complete real T250 route. */
    private static void loadDefaultFlow() {
        System.out.println();
        System.out.println("# Load the Complete T250 Default Route Data");
        System.out.println("------------------------------------------------------------------");

        if (!network.isEmpty()
                && !readYesNo("      This will REPLACE the whole current network. Continue? Y/N: ")) {
            System.out.println("      Loading cancelled. Nothing was changed.");
            return;
        }

        network.clear();
        T250Data.loadDefaultNetwork(network);
        invalidateTraversal();

        System.out.printf("      Loaded %d bus stops and %d route segments (%.1f km total).%n",
                network.getStopCount(), network.getSegmentCount(), network.getTotalDistanceKm());
        System.out.println("      Tip: use Main Menu option 3 to see the network as a map,");
        System.out.println("           or Search option 2 / 3 to run DFS / BFS on it.");
    }

    /** Option 7 - wipe everything after double confirmation. */
    private static void clearNetworkFlow() {
        System.out.println();
        System.out.println("# Clear the Whole Network");
        System.out.println("------------------------------------------------------------------");
        if (readYesNo("      Remove ALL stops and route segments permanently? Y/N: ")) {
            network.clear();
            invalidateTraversal();
            System.out.println("      The network is now empty (" + network.getStopCount()
                    + " stops).");
        } else {
            System.out.println("      Clearing cancelled. Nothing was changed.");
        }
    }

    // =====================================================================
    //  Menu 2 - Search & traversal
    // =====================================================================

    private static void searchMenu() {
        while (true) {
            System.out.println("# SEARCH FOR A BUS STOP - TRAVERSAL OPERATIONS");
            System.out.println("---------------------------------------------------------------------------");
            System.out.println("     1. Search for a bus stop (details & direct connections)");
            System.out.println("     2. DFS traversal (Depth First Search) from a starting stop");
            System.out.println("     3. BFS traversal (Breadth First Search) from a starting stop");
            System.out.println("     0. Back to Main Menu");
            System.out.println("---------------------------------------------------------------------------");

            int choice = readInt("Enter your selection (0 - 3): ");

            if (choice == 0) {
                System.out.println();
                return;
            }

            switch (choice) {
                case 1: searchStopFlow();               break;
                case 2: traversalFlow(true);            break;
                case 3: traversalFlow(false);           break;
                default:
                    System.out.println("[!] Invalid selection. Please enter 0 - 3 only.");
            }
            System.out.println();
        }
    }

    /** Search option 1 - full details of one stop. */
    private static void searchStopFlow() {
        System.out.println();
        System.out.println("# Search for a Bus Stop");
        System.out.println("------------------------------------------------------------------");
        if (guardEmptyNetwork()) {
            return;
        }

        String query = readNonEmptyLine("      Enter the name of the bus stop to search: ");
        String resolved = network.resolveStopName(query);

        if (resolved == null) {
            System.out.println("      The bus stop \"" + query + "\" does NOT exist in the network.");
            printSuggestions(query);
            return;
        }

        Stop stop = network.getStop(resolved);
        List<RouteSegment> neighbours = network.getNeighbours(resolved);

        System.out.println("      Bus stop found!");
        System.out.println("         Name         : " + stop.getName());
        System.out.println("         Code         : " + stop.getCode());
        System.out.println("         Type         : " + (stop.isHub()
                ? "Interchange / hub" : "Normal bus stop"));
        System.out.println("         Direct routes: " + neighbours.size());
        for (RouteSegment seg : neighbours) {
            System.out.printf("            --> %-32s %.1f km%n",
                    seg.getDestination(), seg.getDistanceKm());
        }
    }

    /**
     * Search options 2 & 3 - run DFS or BFS from a chosen start stop,
     * print the numbered visit order and refresh the map highlight.
     */
    private static void traversalFlow(boolean isDfs) {
        String title = isDfs ? "# DFS Traversal (Depth First Search)"
                             : "# BFS Traversal (Breadth First Search)";
        System.out.println();
        System.out.println(title);
        System.out.println("------------------------------------------------------------------");
        if (guardEmptyNetwork()) {
            return;
        }

        String start = askExistingStop("Enter the starting bus stop");
        List<String> order = isDfs ? network.depthFirstSearch(start)
                                   : network.breadthFirstSearch(start);

        System.out.println();
        System.out.println((isDfs ? "DFS" : "BFS") + " visit order starting from \""
                + start + "\":");
        System.out.println("------------------------------------------------------------------");
        System.out.println("   No.  Bus Stop");
        System.out.println("------------------------------------------------------------------");
        for (int i = 0; i < order.size(); i++) {
            System.out.printf("   %-4d %s%n", i + 1, order.get(i));
        }
        System.out.println("------------------------------------------------------------------");
        System.out.printf("Visited %d of %d bus stops.%n", order.size(), network.getStopCount());

        if (order.size() < network.getStopCount()) {
            System.out.println("      [i] " + (network.getStopCount() - order.size())
                    + " stop(s) are UNREACHABLE from here - the network is disconnected.");
        }
        System.out.println(isDfs
                ? "      (DFS explores one branch as deep as possible before backtracking.)"
                : "      (BFS explores nearest stops first, level by level.)");

        lastTraversalOrder = order;
        lastTraversalType = isDfs ? "DFS" : "BFS";
        refreshViewerIfOpen();
        System.out.println("      (The map window highlights these stops in yellow with numbers"
                + " when open.)");
    }

    // =====================================================================
    //  Menu 3 - graphical view
    // =====================================================================

    private static void viewNetwork() {
        System.out.println("# VIEW THE RAPID KL BUS ROUTE NETWORK");
        System.out.println("------------------------------------------------------------------");
        if (guardEmptyNetwork()) {
            return;
        }
        NetworkViewer.showNetwork(network, lastTraversalOrder, lastTraversalType);
        System.out.println("      The route network window has been opened / refreshed.");
        System.out.println("      You may keep using this console while the map stays open.");
    }

    // =====================================================================
    //  Shared helpers - input validation
    // =====================================================================

    /** Reads any line; exits gracefully instead of looping forever at EOF. */
    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!input.hasNextLine()) {
            printInputEndedAndExit();
        }
        return input.nextLine().trim();
    }

    private static String readNonEmptyLine(String prompt) {
        while (true) {
            String line = readLine(prompt);
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("      [!] The input cannot be empty. Please try again.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("      [!] \"" + line + "\" is not a valid number. Try again.");
            }
        }
    }

    /** Reads a positive distance in km (0 < d <= 100 keeps the data sane). */
    private static double readPositiveDouble(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                double value = Double.parseDouble(line);
                if (value > 0 && value <= 100) {
                    return value;
                }
                System.out.println("      [!] The distance must be greater than 0 km.");
            } catch (NumberFormatException e) {
                System.out.println("      [!] \"" + line + "\" is not a valid distance. Try again.");
            }
        }
    }

    private static boolean readYesNo(String prompt) {
        while (true) {
            String line = readLine(prompt);
            String answer = line.toUpperCase();
            if (answer.equals("Y")) {
                return true;
            }
            if (answer.equals("N")) {
                return false;
            }
            System.out.println("      [!] Please answer Y or N only.");
        }
    }

    /**
     * Prompts until the user types a stop that EXISTS (case-insensitive).
     * Unknown names get "did you mean..." suggestions instead of failing.
     */
    private static String askExistingStop(String prompt) {
        while (true) {
            String raw = readNonEmptyLine("      " + prompt + ": ");
            String resolved = network.resolveStopName(raw);
            if (resolved != null) {
                return resolved;
            }
            System.out.println("      [!] The bus stop \"" + raw + "\" does not exist in the network.");
            printSuggestions(raw);
        }
    }

    private static void printSuggestions(String query) {
        List<String> matches = network.suggestStops(query);
        if (matches.isEmpty()) {
            System.out.println("          No similar stop name found. Add new stops under"
                    + " Create Graph first.");
        } else {
            System.out.println("          Did you mean one of these?");
            int shown = Math.min(5, matches.size());
            for (int i = 0; i < shown; i++) {
                System.out.println("             - " + matches.get(i));
            }
        }
    }

    // =====================================================================
    //  Small utilities
    // =====================================================================

    /** @return true if the network is empty (and prints a friendly hint). */
    private static boolean guardEmptyNetwork() {
        if (network.isEmpty()) {
            System.out.println("      [!] The route network is currently empty.");
            System.out.println("          Use Create Graph first, or load the T250 default data.");
            return true;
        }
        return false;
    }

    /** Any change to the graph invalidates a previous DFS/BFS highlight. */
    private static void invalidateTraversal() {
        lastTraversalOrder = List.of();
        lastTraversalType = "";
        refreshViewerIfOpen();
    }

    private static void refreshViewerIfOpen() {
        if (NetworkViewer.isWindowOpen()) {
            NetworkViewer.showNetwork(network, lastTraversalOrder, lastTraversalType);
        }
    }

    private static void printInputEndedAndExit() {
        System.out.println();
        System.out.println("[!] Input stream ended. Closing the system... Goodbye!");
        System.exit(0);
    }
}
