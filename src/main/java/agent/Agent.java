package agent;

import common.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Vector;

public class Agent extends Thread implements IAgent, Serializable {
    private static transient Logger log = LogManager.getLogger(Agent.class.getName());

    private Node homeSite;
    private Vector<Node> visitedServers;

    public Agent(Node homeSite){
        this.homeSite = homeSite;
        this.visitedServers = new Vector<>();
    }

    public Node getHomeSite() {
        return homeSite;
    }

    public void setHomeSite(Node homeSite) {
        this.homeSite = homeSite;
    }

    public void agentArrived(AgentServer srv, InetAddress srvInetAddr, int serverPort) {
        log.info("Agent has arrived at server");
        Node visitedServer = new Node(srvInetAddr, serverPort);
        this.visitedServers.add(visitedServer);

        // TODO Perform work
        try {
            Thread.sleep(10000);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        // I think I am done with my task and want to be sent back home
        srv.agentMigrate(this, homeSite.getAddress(), homeSite.getPort());
    }

    public Vector<Node> getVisitedServers() {
        return this.visitedServers;
    }

    public void printReport() {
        log.info("Carried out task at server. Task results: "); // TODO Prinnt results
    }
}
