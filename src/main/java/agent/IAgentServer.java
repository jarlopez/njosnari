package agent;

import common.Footprint;
import common.Node;

import java.net.InetAddress;
import java.util.Vector;

/**
 * Interface for an agent server which can order agents to migrate
 * and track visited agent history.
 */
public interface IAgentServer
{
    /**
       The agent wants to be sent to inetAddr and port.
       Parameters:
       agent.Agent agent          - reference to an agent object ("this")
       InetAddress dstAddr  - destination address
       int dstPort          - destination port
    */
    /**
     * Sends an agent to a new destination.
     * @param agent the agent that wants to be relocated
     * @param dstAddr the relocation destination address
     * @param dstPort the relocation destination port
     */
    void agentMigrate(Agent agent, InetAddress dstAddr, int dstPort);

    /**
     * Gets all known agent servers.
     * @return the other agent servers known to this server
     */
    Vector<Node> getNeighbours();

    /**
     * Gets the footprints of all visited agents.
     * @return the footprints of all agents that have visited this server
     */
    Vector<Footprint> getFootprints();

    /**
     *  Gets a list of agents currently residing at this server.
     * @return all agents currently residing on this server.
     */
    Vector<Agent> getResidingAgents();
}
