package agent;

import common.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;

/**
 * Represents a mobile agent capable of finding servers,
 * migrating to them, and executing tasks.
 */
public class Agent extends Thread implements IAgent, Serializable {
    private static transient Logger log = LogManager.getLogger(Agent.class.getName());

    /**
     * Original location of this agent.
     */
    private Node homeSite;
    /**
     * Collection of servers visited during the lifetime of this agent.
     */
    private Vector<Node> visitedServers;
    /**
     * Generic container for data collected during tasks.
     */
    private HashMap taskData;
    /**
     * Agent's unique identifier.
     */
    private String id = UUID.randomUUID().toString();

    /**
     * Creates a new Agent with a given home.
     * The home should be populated with address and port.
     * @param homeSite
     */
    public Agent(Node homeSite){
        this.homeSite = homeSite;
        this.visitedServers = new Vector<>();
        this.taskData = new HashMap();
    }

    /**
     * Gets the home site of this Agent.
     * The Agent returns to this home site when done with its tasks.
     * @return this Agent's home.
     */
    public Node getHomeSite() {
        return homeSite;
    }

    /**
     * Updates the home site of this Agent.
     * The Agent returns to this home site when done with its tasks.
     * @param homeSite the new home site
     */
    public void setHomeSite(Node homeSite) {
        this.homeSite = homeSite;
    }

    /**
     * Runs this Agent's arrival sequence, consisting of
     * adding the new server to its visited list, handshaking, executing its task,
     * and returning home.
     * @param srv the server this Agent has arrived at
     * @param srvInetAddr the server's address
     * @param serverPort the server's port
     */
    public void agentArrived(AgentServer srv, InetAddress srvInetAddr, int serverPort) {
        log.info("Agent has arrived at server");
        Node visitedServer = new Node(srvInetAddr, serverPort);
        this.visitedServers.add(visitedServer);

        // TODO Perform work
        // TODO Abstract away into "doWork()" method which can be overridden
        log.info("Shaking hands");
        srv.handshake(this);

        // I think I am done with my task and want to be sent back home
        srv.agentMigrate(this, homeSite.getAddress(), homeSite.getPort());
    }

    /**
     * Get sthe entire list of all servers this Agent has visited so far.
     * @return all visited servers
     */
    public Vector<Node> getVisitedServers() {
        return this.visitedServers;
    }

    /**
     * Logs all task data collected during this Agent's lifetime.
     */
    public void printReport() {
        log.info("Carried out task at server. Task results: " + taskData.toString()); // TODO Print results
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Agent agent = (Agent) o;

        return homeSite.equals(agent.homeSite) && id.equals(agent.id);
    }

    @Override
    public int hashCode() {
        int result = homeSite.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    /**
     * Updates this Agent's handshake knowledge from a server.
     * @param secret the server's secret handshake message
     */
    public void handshake(String secret) {
        taskData.put("handshake", secret);
        log.info("Shook hands: " + secret);
    }
}
