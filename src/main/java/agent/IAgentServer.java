package agent;

import java.net.InetAddress;

public interface IAgentServer
{
    /*
       The agent wants to be sent to inetAddr and port.
       Parameters:
       agent.Agent agent          - reference to an agent object ("this")
       InetAddress dstAddr  - destination address
       int dstPort          - destination port
    */
    public void agentMigrate(Agent agent, InetAddress dstAddr, int dstPort);
}
