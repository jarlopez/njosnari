package agent;

import common.Node;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;

public abstract class BaseAgent extends Thread implements IAgent, Serializable {
    /**
     * Collection of servers visited during the lifetime of this agent.
     */
    protected Vector<Node> visitedServers;

    /**
     * Original location of this agent.
     */
    protected Node homeSite;

    /**
     * Generic container for data collected during tasks.
     */
    protected HashMap taskData;
    /**
     * Agent's unique identifier.
     */
    protected String id = UUID.randomUUID().toString();

    /**
     * The server this Agent is currently at.
     */
    protected transient AgentServer currentServer;

    /**
     * Creates a new Agent with a given home.
     * The home should be populated with address and port.
     * @param homeSite
     */
    public BaseAgent(Node homeSite){
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
     * Gets the entire list of all servers this Agent has visited so far.
     * @return all visited servers
     */
    public Vector<Node> getVisitedServers() {
        return this.visitedServers;
    }

    /**
     * Updates the list of visited servers with the given one.
     * @param srvInetAddr the new server's address
     * @param serverPort the new server's port
     */
    public void addVisitedServer(InetAddress srvInetAddr, int serverPort) {
        Node visitedServer = new Node(srvInetAddr, serverPort);
        this.visitedServers.add(visitedServer);
    }

    /**
     * Updates the home site of this Agent.
     * The Agent returns to this home site when done with its tasks.
     * @param homeSite the new home site
     */
    public void setHomeSite(Node homeSite) {
        this.homeSite = homeSite;
    }

    public abstract void handshake(String secret);
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseAgent agent = (BaseAgent) o;

        return homeSite.equals(agent.homeSite) && id.equals(agent.id);
    }

    @Override
    public int hashCode() {
        int result = homeSite.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }
}
