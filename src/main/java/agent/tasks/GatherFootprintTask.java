package agent.tasks;

import agent.AgentServer;
import common.Footprint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Vector;

/**
 * Task which extracts all agent footprints at a remote agent server.
 */
public class GatherFootprintTask implements IAgentTask, Serializable {
    private static transient Logger log = LogManager.getLogger(GatherFootprintTask.class.getName());

    /**
     * Footprint data gathered from the target server.
     */
    private Vector<Footprint> results;

    /**
     * Stores the agent server footprints.
     * @param context the remote agent server
     */
    @Override
    public void execute(AgentServer context) {
        log.info("Gathering footprints from context");
        results = context.getFootprints();
    }

    @Override
    public Object getResults() {
        return results;
    }

    @Override
    public String getResultsString() {
        StringBuilder sb = new StringBuilder();
        for (Footprint it : results) {
            sb
                    .append("(")
                    .append(it.getHomeNode().displayName())
                    .append(", ")
                    .append(it.getSentToNode().displayName())
                    .append(")");
        }
        return sb.toString();
    }
}
