package agent;

import common.Node;
import discovery.DiscoveryClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import static agent.AgentServer.DEFAULT_BASE_PORT;
import static agent.AgentServer.DEFAULT_MULTICAST_ADDRESS;

public class AgentClient {
    private static Logger log = LogManager.getLogger(AgentClient.class.getName());

    private Agent agent;
    private Socket sendingSocket;

    private int listeningPort;
    private ServerSocket listeningSocket;

    public AgentClient(int listeningPort) {
        try {
            this.listeningPort = listeningPort;
            //Discover agent servers
            DiscoveryClient discoveryClient = new DiscoveryClient(InetAddress.getByName(DEFAULT_MULTICAST_ADDRESS), DEFAULT_BASE_PORT);
            Vector agentServers = discoveryClient.getDiscoveryResult();

            if(!agentServers.isEmpty()) {
                migrateAgentToServer((Node) agentServers.get(0));
                waitForAgentToReturn();
            }
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    /**
     * Send agent to agentServer
     * @param agentServer
     * @throws IOException
     */
    private void migrateAgentToServer(Node agentServer) throws IOException{
        ObjectOutputStream out = null;

        try {
            agent = new Agent(new Node(InetAddress.getLocalHost(), this.listeningPort));
            sendingSocket = new Socket(agentServer.getAddress(), agentServer.getPort());
            out = new ObjectOutputStream(sendingSocket.getOutputStream());
            out.writeObject(agent);
            out.flush();
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
        finally {
            if(out != null) {
                out.close();
            }
        }
    }

    /**
     * Opens up a listening socket and waits for an incoming agent to be received
     * @throws IOException
     */
    private void waitForAgentToReturn() throws IOException{
        ObjectInputStream in = null;

        try {
            listeningSocket = new ServerSocket(listeningPort);
            Socket acceptSocket = listeningSocket.accept();
            in = new ObjectInputStream(acceptSocket.getInputStream());
            Object inputObject = in.readObject();

            if (inputObject instanceof Agent) {
                Agent receivedAgent = (Agent)inputObject;
                if (agent.equals(receivedAgent)) {
                    agent.printReport();
                } else {
                    log.warn("Received suspicious agent when waiting for agent to return!");
                }
            }

        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
        catch (ClassNotFoundException cEx) {
            cEx.printStackTrace();
        }
        finally {
            if(in != null) {
                in.close();
            }
        }
    }

    public static void main (String[] args) {
        try {
            int clientPort = DEFAULT_BASE_PORT;
            if (args.length > 0) {
                clientPort = Integer.parseInt(args[0]);
            }
            new AgentClient(clientPort);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
