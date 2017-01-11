package agent.tasks;

import agent.AgentServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

/**
 * Task which sleeps for {@link #SLEEPER_MILLIS} ms at a remote agent server.
 */
public class SleeperTask implements IAgentTask, Serializable {
    private static transient Logger log = LogManager.getLogger(SleeperTask.class.getName());

    private static final long SLEEPER_MILLIS = 60 * 1000;

    String results = "INCOMPLETE";

    /**
     * Sleeps for {@link #SLEEPER_MILLIS} ms at a remote server agent.
     * If interrupted, it updates the task results accordingly.
     * @param context the remote agent server
     */
    @Override
    public void execute(AgentServer context) {
        try {
            Thread.sleep(SLEEPER_MILLIS);
            results = "Slept at " + context.getServerName() + " for " + SLEEPER_MILLIS + "ms";
        } catch (InterruptedException e) {
            results = "Interrupted during task execution";
            log.warn(results);
        }
    }

    @Override
    public Object getResults() {
        return getResultsString();
    }

    @Override
    public String getResultsString() {
        return results;
    }
}
