package agent;

import java.net.InetAddress;

public interface IAgent
{
    /*
       An agent server invokes this method. It is used to tell an agent
       that he is successfully arrived at a server and is ready to run.
       This method should invoke the method start of the object that
       represents the agent.
       Parameters:
       agent.AgentServer srv          - reference to that server object that received
                                  the agent object
       InetAddress srvInetAddr  - the server's IP address
       int serverPort           - the server's server port
    */
    public void agentArrived(AgentServer srv, InetAddress srvInetAddr, int serverPort);
}