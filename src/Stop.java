/**
 * Stop - represents a VERTEX in the bus route graph.
 *
 * Each Stop object models one physical Rapid KL bus stop along route T250.
 * A stop can be marked as a "hub" (interchange), e.g. an LRT station or a
 * bus terminal, where passengers can change to other services.
 */
public class Stop {

    private final String name;      // unique name of the stop, e.g. "LRT Wangsa Maju"
    private final String code;      // official Rapid KL stop code, e.g. "KL2097"
    private final boolean hub;      // true = interchange / terminal stop

    public Stop(String name, String code, boolean hub) {
        this.name = name;
        this.code = code;
        this.hub = hub;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public boolean isHub() {
        return hub;
    }

    @Override
    public String toString() {
        return name + " [" + code + "]" + (hub ? " (Interchange)" : "");
    }
}
