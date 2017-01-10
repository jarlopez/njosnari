package agent;

import common.Node;

import java.net.InetAddress;

/**
 * Responsible for generating agent tasks based on a predefined type.
 */
public class AgentFactory {
    /**
     * Creates a new agent based on a type (if it exists)
     * @param type
     * @return a new agent of the specified type
     * @throws ClassNotFoundException if the given type is not supported
     */
    public static BaseAgent createAgent(AgentType type, InetAddress address, int port) throws ClassNotFoundException {
        Node home = new Node(address, port);
        switch (type) {
            case TaskedAgent:
                return new TaskedAgent(home);
            case TestAgent:
                return new Agent(home);
            default:
                throw new ClassNotFoundException("Invalid task type: " + type);
        }
    }
}
