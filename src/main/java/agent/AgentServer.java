package agent;

import common.Footprint;
import common.Node;
import discovery.DiscoveryClient;
import discovery.DiscoveryServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class AgentServer implements IAgentServer{
    private static Logger log = LogManager.getLogger(AgentServer.class.getName());

    /*
    An agent server accepts TCP socket connections on a server port to receive agents.
    This TCP port has the same port number as the one that is used for sending
    discovery replies.
     */
    public static final String DEFAULT_MULTICAST_ADDRESS = "230.0.0.88";
    public static final int DEFAULT_BASE_PORT   = 8082;
    public static final int DEFAULT_SERVER_PORT = 8084;

    private ServerSocket serverSocket;
    private final Thread serverThread;
    private int serverPort;
    private Socket sendingSocket;

    /**
    Information about agents that visited the server (home node and what node it was sent to).
     */
    private Vector<Footprint> footprints;
    /**
    Agents that are currently residing at this server
     */
    private Vector<Agent> residingAgents;

    /**
     * List of neighbors
     */
    private Vector<Node> neighbours;

    public AgentServer(int serverPort) {
        this.footprints = new Vector<>();
        this.residingAgents = new Vector<>();
        this.serverPort = serverPort;
        try
        {
            serverSocket = new ServerSocket(this.serverPort);
        }
        catch (IOException ex)
        {
            log.error("Could not listen on port " + serverPort);
            System.exit(1);
        }
        log.info("Running");

        // run the discovery service in new thread
        try {
            serverThread = new DiscoveryServer(InetAddress.getByName(DEFAULT_MULTICAST_ADDRESS), DEFAULT_BASE_PORT, this.serverPort);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        // run neighbour discovery periodically to keep track of active servers in the network
        Timer time = new Timer();
        time.schedule(new NeighbourDiscovery(), 0, TimeUnit.MINUTES.toMillis(1));

        while (true)
        {
            ObjectInputStream inputStream = null;
            try {
                Socket clientSocket = serverSocket.accept();
                inputStream = new ObjectInputStream(clientSocket.getInputStream());
                Object inputObject = inputStream.readObject();

                if (inputObject instanceof Agent)
                {
                    Agent agent = (Agent)inputObject;
                    this.residingAgents.add(agent);
                    agent.agentArrived(this, InetAddress.getLocalHost(), 8084);
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            catch (ClassNotFoundException cEx)
            {
                cEx.printStackTrace();
            }
            finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    }
                    catch (IOException ioEx) {
                        ioEx.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     The agent wants to be sent to inetAddr and port.
     */
    public void agentMigrate(Agent agent, InetAddress dstAddr, int dstPort) {
        log.info("Agent " + agent + " wants to migrate home/to next node");

        Node nextNode = new Node(dstAddr, dstPort);
        Footprint footprint = new Footprint(agent.getHomeSite(), nextNode);
        this.footprints.add(footprint);

        this.residingAgents.remove(agent);

        ObjectOutputStream out = null;

        try {
            sendingSocket = new Socket(dstAddr, dstPort);
            out = new ObjectOutputStream(sendingSocket.getOutputStream());
            out.writeObject(agent);
            out.flush();
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
        finally {
            if(out != null) {
                try {
                    out.close();
                }
                catch (IOException ioEx) {
                    ioEx.printStackTrace();
                }
            }
        }
    }

    /**
     * An agent server keeps track of neighbours, i.e. other agent servers.
     * @return all agent servers neighbours
     */
    public Vector<Node> getNeighbours() {
        return this.neighbours;
    }

    /**
     * @return footprints of agents that visited the server
     */
    public Vector getFootprints() {
        return this.footprints;
    }

    /**
     * @return list of agents currently residing a server
     */
    public Vector getResidingAgents() {
        return this.residingAgents;
    }

    public static void main (String[] args) {
        try {
            int serverPort = DEFAULT_SERVER_PORT;
            if (args.length > 0) {
                serverPort = Integer.parseInt(args[0]);
            }
            new AgentServer(serverPort);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Neighbour discovery process that is periodically repeated in order to keep track of active servers in the network.
     */
    private class NeighbourDiscovery extends TimerTask {

        public NeighbourDiscovery() {
            neighbours = new Vector<>();
        }

        public void run() {
            try {
                log.info("Running neighbour discovery");
                DiscoveryClient discoveryClient = new DiscoveryClient(InetAddress.getByName(DEFAULT_MULTICAST_ADDRESS), DEFAULT_BASE_PORT);
                neighbours = discoveryClient.getDiscoveryResult();

                if (!neighbours.isEmpty()) {
                    // remove our self from the neighbour list
                    neighbours.removeIf(n -> n.getPort() == serverPort);
                    log.info("Neighbours:" + neighbours.toString());
                }

            } catch (Exception ex) {
                System.out.println("error running thread " + ex.getMessage());
            }
        }
    }
}
