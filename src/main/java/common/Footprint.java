package common;

/*
 A footprint of an agent contains the home address (IP address and home port of the agent client
 that created this agent) of the agent and the IP address and port where the agent has been sent.
 */

public class Footprint {

    private Node homeNode;
    private Node sentToNode;

    public Footprint(Node homeNode, Node sentToNode) {
        this.homeNode = homeNode;
        this.sentToNode = sentToNode;
    }

    public Node getHomeNode() {
        return homeNode;
    }

    public void setHomeNode(Node homeNode) {
        this.homeNode = homeNode;
    }

    public Node getSentToNode() {
        return sentToNode;
    }

    public void setSentToNode(Node sentToNode) {
        this.sentToNode = sentToNode;
    }
}
