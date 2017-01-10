package agent;

import common.Node;
import gui.AgentClientListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Represents a mobile agent capable of finding servers,
 * migrating to them, and executing tasks.
 */
public class Agent extends BaseAgent implements Serializable {
    private static transient Logger log = LogManager.getLogger(Agent.class.getName());

    /**
     * Creates a new agent and sets up structures.
     * @param homeSite this agent's home
     */
    public Agent(Node homeSite){
        super(homeSite);
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
        addVisitedServer(srvInetAddr, serverPort);
        this.currentServer = srv;

        this.executeTask();

        // I think I am done with my task and want to be sent back home
        srv.agentMigrate(this, homeSite.getAddress(), homeSite.getPort());
    }

    @Override
    public void executeTask() {
        log.info("Shaking hands");
        currentServer.handshake(this);
    }

    @Override
    public void displayReport(AgentClientListener uiContext) {
        String msg = "Carried out task at server. Task results: " + taskData.toString();
        log.info(msg);
        if (uiContext != null) {
            uiContext.onMessage(msg);
        }
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
