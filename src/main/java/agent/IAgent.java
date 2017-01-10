package agent;

import common.Node;

import java.net.InetAddress;
import java.util.Vector;

/**
 * Interface for a mobile agent capable of arriving at agent servers and executing tasks.
 */
public interface IAgent
{
    /**
     * Tells an agents that it has successfully arrived at a new home server
     * and is ready to execute this agent's task.
     * @param srv reference to that server object that received the agent's object
     * @param srvInetAddr the server's ip address
     * @param serverPort the server's server port
     */
    void agentArrived(AgentServer srv, InetAddress srvInetAddr, int serverPort);

    /**
     * @return the list of all servers visited by this agent
     */
    Vector<Node> getVisitedServers();
}