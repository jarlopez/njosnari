package agent.tasks;

import agent.AgentServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

public class SleeperTask implements IAgentTask, Serializable {
    private static transient Logger log = LogManager.getLogger(SleeperTask.class.getName());

    private static final long SLEEPER_MILLIS = 60 * 1000;

    String results = "INCOMPLETE";

    @Override
    public void execute(AgentServer context) {
        try {
            Thread.sleep(SLEEPER_MILLIS);
            results = "Slept at server for " + SLEEPER_MILLIS + "ms";
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
