package com.rapidkl.t250;

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
 * The complete real T250 route (26 stops, 28 segments) is loaded
 * automatically at startup, so a realistic network is on screen immediately;
 * Create Graph can then modify it, or clear it (option 6) to build a
 * network from scratch.
 */
public class Main {

    private static final Scanner input = new Scanner(System.in);
    private static final GraphADT network = new BusRouteGraph();

    /** Last DFS/BFS result, kept so the map can highlight it again later. */
    private static List<String> lastTraversalOrder = List.of();
    private static String lastTraversalType = "";

    // =====================================================================
    //  Program entry point and main menu
    // =====================================================================

    public static void main(String[] args) {
        T250Data.loadDefaultNetwork(network);   // pre-load the real T250 route
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
        System.out.println("[TIP] The T250 route is pre-loaded. Choose 3 to see the network map,");
        System.out.println("      or 1 (Create Graph) to add / remove stops and segments.");
        System.out.println();
    }

    private static void printMainMenu() {
        System.out.println("Main Menu (Press '0' to exit)");
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("     1. Create Graph       (add / remove bus stops & route segments)");
        System.out.println("     2. Route Search    (DFS / BFS with goal state)");
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
            System.out.println("     6. Clear the whole network");
            System.out.println("     0. Back to Main Menu");
            System.out.println("---------------------------------------------------------------------------");

            int choice = readInt("Enter your selection (0 - 6): ");

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
                case 6: clearNetworkFlow();   break;
                default:
                    System.out.println("[!] Invalid selection. Please enter 0 - 6 only.");
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
                if (result == GraphADT.RESULT_OK) {
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
            if (result == GraphADT.RESULT_OK) {
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
        if (result == GraphADT.RESULT_OK) {
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

    /** Option 6 - wipe everything after double confirmation. */
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
            System.out.println("# ROUTE SEARCH - GRAPH TRAVERSAL ALGORITHMS");
            System.out.println("---------------------------------------------------------------------------");
            System.out.println("     1. DFS (Depth First Search): start stop -> goal stop");
            System.out.println("     2. BFS (Breadth First Search): start stop -> goal stop");
            System.out.println("     0. Back to Main Menu");
            System.out.println("---------------------------------------------------------------------------");

            int choice = readInt("Enter your selection (0 - 2): ");

            if (choice == 0) {
                System.out.println();
                return;
            }

            switch (choice) {
                case 1: traversalFlow(true);  break;
                case 2: traversalFlow(false); break;
                default:
                    System.out.println("[!] Invalid selection. Please enter 0 - 2 only.");
            }
            System.out.println();
        }
    }

    /**
     * Search options 1 & 2 - run DFS or BFS with an optional GOAL STATE.
     *
     * The user enters a start stop and (optionally) a destination stop.
     * With a goal, the traversal stops as soon as the destination is
     * reached and the discovered route is printed with its segment count
     * and total distance. Without a goal (press Enter), the whole
     * connected component is traversed and shown as a numbered list.
     */
    private static void traversalFlow(boolean isDfs) {
        String title = isDfs ? "# DFS Search (Depth First Search)"
                             : "# BFS Search (Breadth First Search)";
        System.out.println();
        System.out.println(title);
        System.out.println("------------------------------------------------------------------");
        if (guardEmptyNetwork()) {
            return;
        }

        String start = askExistingStop("Enter the START bus stop");
        String goal = readOptionalExistingStop(
                "Enter the DESTINATION stop (goal state), or press Enter to traverse ALL stops");

        GraphTraversal algorithm = isDfs ? new DepthFirstSearch(network)
                                         : new BreadthFirstSearch(network);
        List<String> order = algorithm.traverse(start, goal);

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

        if (!goal.isEmpty()) {
            reportGoalSearch(algorithm, isDfs, start, goal);
        } else {
            System.out.printf("Visited %d of %d bus stops.%n",
                    order.size(), network.getStopCount());
            if (order.size() < network.getStopCount()) {
                System.out.println("      [i] " + (network.getStopCount() - order.size())
                        + " stop(s) are UNREACHABLE from here - the network is disconnected.");
            }
            System.out.println(isDfs
                    ? "      (DFS explores one branch as deep as possible before backtracking.)"
                    : "      (BFS explores nearest stops first, level by level.)");
        }

        lastTraversalOrder = order;
        lastTraversalType = isDfs ? "DFS" : "BFS";
        refreshViewerIfOpen();
        System.out.println("      (The map window highlights these stops in yellow with numbers"
                + " when open.)");
    }

    /**
     * Prints the outcome of a goal-directed search: the found route with
     * segment count and total km, or a clear unreachable message.
     */
    private static void reportGoalSearch(GraphTraversal algorithm,
                                         boolean isDfs, String start, String goal) {
        System.out.println("Stops explored before termination: "
                + algorithm.getVisitCount() + " of " + network.getStopCount() + ".");
        System.out.println();

        if (!algorithm.isGoalReached()) {
            System.out.println("      >>> RESULT: The destination \"" + goal
                    + "\" is UNREACHABLE from \"" + start + "\".");
            System.out.println("          The search exhausted every reachable stop without"
                    + " finding it.");
            return;
        }

        List<String> path = algorithm.getGoalPath();
        double totalKm = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            totalKm += segmentDistance(path.get(i), path.get(i + 1));
        }

        System.out.println("      >>> GOAL FOUND! Route from \"" + start + "\" to \""
                + goal + "\":");
        System.out.println("      ------------------------------------------------------------------");
        printPath(path);
        System.out.println("      ------------------------------------------------------------------");
        System.out.printf("      Segments travelled : %d   |   Total distance: %.1f km%n",
                path.size() - 1, totalKm);

        if (!isDfs) {
            System.out.println("      (BFS guarantee: this route uses the FEWEST segments possible.)");
        } else {
            System.out.println("      (DFS finds A valid route quickly, but it is not always the"
                    + " fewest-segment one - compare with BFS!)");
        }
    }

    /** Distance of the direct segment between two stops (0 if none). */
    private static double segmentDistance(String stopA, String stopB) {
        for (RouteSegment seg : network.getNeighbours(stopA)) {
            if (seg.getDestination().equals(stopB)) {
                return seg.getDistanceKm();
            }
        }
        return 0;                                // defensive: should not happen
    }

    /** Prints a route as one wrapped line of "Stop -> Stop -> ...". */
    private static void printPath(List<String> path) {
        StringBuilder line = new StringBuilder("       ");
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) {
                line.append("  ->  ");
            }
            line.append(path.get(i));
            if (line.length() > 62 && i + 1 < path.size()) {
                System.out.println(line);
                line = new StringBuilder("         ");
            }
        }
        System.out.println(line);
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

    /**
     * Like askExistingStop, but pressing ENTER returns "" (no goal state:
     * the traversal then covers the whole connected component).
     */
    private static String readOptionalExistingStop(String prompt) {
        while (true) {
            String raw = readLine("      " + prompt + ": ");
            if (raw.isEmpty()) {
                return "";
            }
            String resolved = network.resolveStopName(raw);
            if (resolved != null) {
                return resolved;
            }
            System.out.println("      [!] The bus stop \"" + raw + "\" does not exist in the network.");
            printSuggestions(raw);
            System.out.println("          (Press Enter without typing a name to skip the goal.)");
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
