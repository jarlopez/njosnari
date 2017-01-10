package agent;

import agent.tasks.IAgentTask;
import common.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.Vector;

/**
 * Agent which is assigned a custom task to execute on a remote agent server,
 * and will then return home to report the task results.
 */
public class TaskedAgent extends BaseAgent {
    private static transient Logger log = LogManager.getLogger(TaskedAgent.class.getName());

    /**
     * Custom task to execute on remote agent server.
     */
    private IAgentTask task;

    /**
     * Creates a new TaskedAgent stationed at a home node.
     * @param homeSite
     */
    public TaskedAgent(Node homeSite) {
        super(homeSite);
    }

    @Override
    public void handshake(String secret) {
        // no-op
    }

    /**
     * Triggers chain of events at remote server, which include executing the custom task
     * and returning home upon completion.
     * @param srv reference to that server object that received the agent's object
     * @param srvInetAddr the server's ip address
     * @param serverPort the server's server port
     */
    @Override
    public void agentArrived(AgentServer srv, InetAddress srvInetAddr, int serverPort) {
        log.info("Arrived at a server");
        addVisitedServer(srvInetAddr, serverPort);
        currentServer = srv;
        executeTask();
        srv.agentMigrate(this, homeSite.getAddress(), homeSite.getPort());
    }

    @Override
    public Vector<Node> getVisitedServers() {
        return null;
    }

    @Override
    public void executeTask() {
        task.execute(currentServer);
    }

    @Override
    public void displayReport() {
        log.info("Results: " + task.getResultsString());
    }

    public void setTask(IAgentTask task) {
        this.task = task;
    }
}
