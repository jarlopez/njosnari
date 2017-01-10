package agent;

import common.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.Vector;

public class SleeperAgent extends BaseAgent {
    private static final long SLEEPER_MILLIS = 60 * 1000;

    private static transient Logger log = LogManager.getLogger(SleeperAgent.class.getName());

    public SleeperAgent(Node homeSite) {
        super(homeSite);
    }

    @Override
    public void handshake(String secret) {
        // no-op
    }

    @Override
    public void agentArrived(AgentServer srv, InetAddress srvInetAddr, int serverPort) {
        log.info("Arrived at a server");
        executeTask();
        srv.agentMigrate(this, homeSite.getAddress(), homeSite.getPort());
    }

    @Override
    public Vector<Node> getVisitedServers() {
        return null;
    }

    @Override
    public void executeTask() {
        log.info("Sleeping :)");
        try {
            Thread.sleep(SLEEPER_MILLIS);
        } catch (InterruptedException e) {
            log.warn("Interrupted during task!");
        }
    }

    @Override
    public void displayReport() {

    }
}
