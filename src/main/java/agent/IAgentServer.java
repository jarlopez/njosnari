package agent;

import common.Footprint;

import java.net.InetAddress;
import java.util.Vector;

public interface IAgentServer
{
    /*
       The agent wants to be sent to inetAddr and port.
       Parameters:
       agent.Agent agent          - reference to an agent object ("this")
       InetAddress dstAddr  - destination address
       int dstPort          - destination port
    */
    void agentMigrate(Agent agent, InetAddress dstAddr, int dstPort);

    /* optional */
    Vector<Footprint> getFootprints();
    Vector<Agent> getResidingAgents();
}
