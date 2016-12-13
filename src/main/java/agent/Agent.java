package agent;

import java.io.Serializable;
import java.net.InetAddress;

public class Agent extends Thread implements IAgent, Serializable{

    public Agent(){

    }

    public void agentArrived(AgentServer srv, InetAddress srvInetAddr, int serverPort) {

        System.out.println("HELLO! I Am an agent. I have arrived at an agent server!");
    }
}
