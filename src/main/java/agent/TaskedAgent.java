package agent;

import agent.tasks.IAgentTask;
import common.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.Vector;

public class TaskedAgent extends BaseAgent {
    private static transient Logger log = LogManager.getLogger(TaskedAgent.class.getName());

    private IAgentTask task;

    public TaskedAgent(Node homeSite) {
        super(homeSite);
    }

    @Override
    public void handshake(String secret) {
        // no-op
    }

    @Override
    public void agentArrived(AgentServer srv, InetAddress srvInetAddr, int serverPort) {
        log.info("Arrived at a server");
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
