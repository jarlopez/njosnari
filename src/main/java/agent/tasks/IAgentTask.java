package agent.tasks;

import agent.AgentServer;

/**
 * General interface for agent tasks.
 */
public interface IAgentTask {
    /**
     * Executes the assigned task.
     */
    void execute(AgentServer context);

    /**
     * Fetches the results of the executed task.
     * @return task results
     */
    Object getResults();

    /**
     * Generates the results of the executed task as a string.
     * @return task results string
     */
    String getResultsString();

    enum Type {
        FindResidentsTask,
        GatherFootprintTask,
        SleeperTask
    }
}
