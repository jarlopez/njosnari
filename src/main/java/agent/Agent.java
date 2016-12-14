package agent;

import common.Node;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Vector;

public class Agent extends Thread implements IAgent, Serializable{

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
        System.out.println("HELLO! I Am an agent. I have arrived at an agent server!");
        Node visitedServer = new Node(srvInetAddr, serverPort);
        this.visitedServers.add(visitedServer);

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
        System.out.println("I, Agent, carried out this task at this server and found this information..");
    }
}
