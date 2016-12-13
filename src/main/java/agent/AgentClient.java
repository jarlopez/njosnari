package agent;

import com.sun.tools.javac.util.Pair;
import common.Node;
import discovery.DiscoveryClient;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;

import static agent.AgentServer.DEFAULT_MULTICAST_ADDRESS;
import static agent.AgentServer.DEFAULT_BASE_PORT;

public class AgentClient {

    Agent agent;
    Socket clientSocket;

    public AgentClient() {

        try
        {
            //Discover agent servers
            DiscoveryClient discoveryClient = new DiscoveryClient(InetAddress.getByName(DEFAULT_MULTICAST_ADDRESS), DEFAULT_BASE_PORT);
            Vector agentServers = discoveryClient.getDiscoveryResult();

            if(!agentServers.isEmpty()) {

                Node agentServer = (Node) agentServers.get(0);
                Agent agent = new Agent();
                clientSocket = new Socket(agentServer.getAddress(), agentServer.getPort());
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.writeObject(agent);
                out.flush();
            }
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    public static void main (String[] args) {
        new AgentClient();
    }
}
