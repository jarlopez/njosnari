package agent.tasks;

import agent.AgentServer;
import agent.BaseAgent;
import common.Node;

import java.io.Serializable;
import java.util.Vector;

/**
 * Task whose purpose is to get all other agent residents on a remote agent server.
 */
public class FindResidentsTask implements IAgentTask, Serializable {
    private Vector<String> results = new Vector<>();

    /**
     * Asks the agent server context for all residing agents and stores them.
     * @param context the remote agent server
     */
    @Override
    public void execute(AgentServer context) {
        Vector<BaseAgent> residents = context.getResidingAgents();
        for (BaseAgent it : residents) {
            Node home = it.getHomeSite();
            results.add(home.displayName());
        }
    }

    @Override
    public Object getResults() {
        return results;
    }

    @Override
    public String getResultsString() {
        return results.toString();
    }
}
