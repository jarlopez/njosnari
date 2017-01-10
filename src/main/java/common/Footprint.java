package common;

/*
 A footprint of an agent contains the home address (IP address and home port of the agent client
 that created this agent) of the agent and the IP address and port where the agent has been sent.
 */

import java.io.Serializable;

/**
 * Represents the footprint of an agent which contains the home address
 * (IP address and home port of the agent client that created this agent)
 * of the agent and the IP address and port where the agent has been sent.
 */
public class Footprint implements Serializable {

    /**
     * An agent's original home.
     */
    private Node homeNode;
    /**
     * The node an agent was sent to next.
     */
    private Node sentToNode;

    /**
     * Creates a new Footprint for an agent based on its home and its new destination.
     * @param homeNode the agent's original home
     * @param sentToNode the node it will be sent to next
     */
    public Footprint(Node homeNode, Node sentToNode) {
        this.homeNode = homeNode;
        this.sentToNode = sentToNode;
    }

    /**
     * Gets this Footprint's home node
     * @return this footprint's home node
     */
    public Node getHomeNode() {
        return homeNode;
    }

    /**
     * Sets this Footprint's home node
     * @param homeNode the new home node
     */
    public void setHomeNode(Node homeNode) {
        this.homeNode = homeNode;
    }

    /**
     * Gets this Footprint's next node
     * @return the sent to node
     */
    public Node getSentToNode() {
        return sentToNode;
    }

    /**
     * Sets this Footprint's next node
     * @param sentToNode the new next node
     */
    public void setSentToNode(Node sentToNode) {
        this.sentToNode = sentToNode;
    }
}
