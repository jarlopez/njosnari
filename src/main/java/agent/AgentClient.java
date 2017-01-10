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

/**
 * Represents a complete mobile, networked agent
 * capable of sending the agent to servers and waiting for it to return.
 */
public class AgentClient {
    private static Logger log = LogManager.getLogger(AgentClient.class.getName());

    /**
     * This Client's agent.
     */
    private BaseAgent agent;
    /**
     * The socket used for transporting this Client's Agent
     * to other servers.
     */
    private Socket sendingSocket;

    /**
     * Port used for listening for an Agent returning home.
     */
    private int listeningPort;
    /**
     * The socket used for listening for an Agent returning home.
     */
    private ServerSocket listeningSocket;

    /**
     * Creates a new Client and sets up the discovery mechanism.
     * @param listeningPort the port used for waiting for Agents returning home
     */
    public AgentClient(int listeningPort) {
        try {
            this.listeningPort = listeningPort;
            //Discover agent servers
            DiscoveryClient discoveryClient = new DiscoveryClient(InetAddress.getByName(DEFAULT_MULTICAST_ADDRESS), DEFAULT_BASE_PORT);
            Vector agentServers = discoveryClient.getDiscoveryResult();

            if (!agentServers.isEmpty()) {
                // TODO? Determine _which_ server to join based on some criteria
                migrateAgentToServer((Node) agentServers.get(0));
                waitForAgentToReturn();
            }
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    /**
     * Sends agent to a remote server
     * @param agentServer the remote server to transport the agent to
     * @throws IOException
     */
    private void migrateAgentToServer(Node agentServer) throws IOException {
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
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Opens up a listening socket and waits for an incoming agent to be received.
     * @throws IOException
     */
    private void waitForAgentToReturn() throws IOException {
        ObjectInputStream in = null;
        try {
            listeningSocket = new ServerSocket(listeningPort);
            Socket acceptSocket = listeningSocket.accept();
            in = new ObjectInputStream(acceptSocket.getInputStream());
            Object inputObject = in.readObject();

            // TODO Determine if BaseAgent
            if (BaseAgent.class.isAssignableFrom(inputObject.getClass())) {
                BaseAgent receivedAgent = (BaseAgent)inputObject;
                if (receivedAgent.equals(agent)) {
                    receivedAgent.displayReport();
                } else {
                    log.warn("Received suspicious agent when waiting for agent to return! " + receivedAgent.toString());
                }
            } else {
                log.error("Unknown object type received: " + inputObject.toString());
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
