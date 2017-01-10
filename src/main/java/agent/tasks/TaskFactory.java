package agent.tasks;

/**
 * Responsible for generating agent tasks based on a predefined type.
 */
public class TaskFactory {
    /**
     * Creates a new task based on a type (if it exists)
     * @param type
     * @return a new task of the specified type
     * @throws ClassNotFoundException if the given type is not supported
     */
    public static IAgentTask createTask(IAgentTask.Type type) throws ClassNotFoundException {
        switch (type) {
            case FindResidentsTask:
                return new FindResidentsTask();
            case GatherFootprintTask:
                return new GatherFootprintTask();
            case SleeperTask:
                return new SleeperTask();
            default:
                throw new ClassNotFoundException("Invalid task type: " + type);
        }
    }
}
