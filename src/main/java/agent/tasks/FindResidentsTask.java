package agent.tasks;

import agent.AgentServer;
import agent.BaseAgent;
import common.Node;

import java.io.Serializable;
import java.util.Vector;

public class FindResidentsTask implements IAgentTask, Serializable {
    private Vector<String> results;

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
