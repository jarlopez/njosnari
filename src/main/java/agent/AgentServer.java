package agent;

import common.Footprint;
import common.Node;
import discovery.DiscoveryClient;
import discovery.DiscoveryServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * An agent server accepts TCP socket connections on a server port to receive agents.
 */
public class AgentServer implements IAgentServer {
    private static Logger log = LogManager.getLogger(AgentServer.class.getName());

    /**
     * The default UDP multicast group used for server discovery.
     */
    public static final String DEFAULT_MULTICAST_ADDRESS = "230.0.0.88";
    /**
     * The default port used for receiving UDP discovery messages.
     */
    public static final int DEFAULT_BASE_PORT   = 8082;
    /**
     * The default port used for listening for incoming TCP messages
     */
    public static final int DEFAULT_SERVER_PORT = 8084;

    /**
     * Used for accepting incoming migrating Agents.
     */
    private ServerSocket serverSocket;

    /**
     * Port used for accepting incoming migrating Agents.
     */
    private int serverPort;

    /**
     * Main thread running the server discovery service.
     */
    private final Thread serverDiscoveryThread;

    /**
     * Socket used for sending away Agents who have completed their tasks.
     */
    private Socket sendingSocket;

    /**
     * Threadpool for accepting incoming Agents.
     */
    private ExecutorService executorService;

    /**
     * Server-specific secret message used to handshake with Agents.
     */
    private static final String secret = "shhh";

    /**
     * Specifies how often to run neigbhour discovery.
     */
    private static final int NEIGHBOUR_DISCOVERY_INTERVAL_MIN = 1;

    /**
     * Information about agents that visited the server (home node and what node it was sent to).
     */
    private Vector<Footprint> footprints;
    /**
     * Agents that are currently residing at this server.
     */
    private Vector<BaseAgent> residingAgents;

    /**
     * List of neighbour servers discovered so far.
     */
    private Vector<Node> neighbours;

    /**
     * Creates a new AgentServer by setting up the threadpool,
     * scheduling the server discovery service, and running the main server loop.
     * // TODO Split up run into own function?
     * @param serverPort the port used for accepting incoming migrating Agents
     */
    public AgentServer(int serverPort) {
        this.footprints = new Vector<>();
        this.residingAgents = new Vector<BaseAgent>();
        this.serverPort = serverPort;
        this.executorService = Executors.newFixedThreadPool(8);
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
            serverDiscoveryThread = new DiscoveryServer(InetAddress.getByName(DEFAULT_MULTICAST_ADDRESS), DEFAULT_BASE_PORT, this.serverPort);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        // run neighbour discovery periodically to keep track of active servers in the network
        Timer time = new Timer();
        time.schedule(new NeighbourDiscovery(), 0, TimeUnit.MINUTES.toMillis(NEIGHBOUR_DISCOVERY_INTERVAL_MIN));

        while (true)
        {
            try {
                Socket clientSocket = serverSocket.accept();
                this.executorService.execute(new AgentHandler(clientSocket, this, DEFAULT_SERVER_PORT));
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Sends an Agent to its next desired server.
     * @param agent the agent wishing to be transported
     * @param dstAddr the destination address for the Agent's new home
     * @param dstPort the destination port for the Agent's new home
     */
    public void agentMigrate(BaseAgent agent, InetAddress dstAddr, int dstPort) {
        log.info("Agent " + agent.toString() + " wants to migrate home/to next node");

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
     * @return all AgentServer neighbours
     */
    public Vector<Node> getNeighbours() {
        return this.neighbours;
    }

    /**
     * Gets all Agent footprints that have ever resided on this server.
     * @return footprints of Agents that visited this server
     */
    public Vector getFootprints() {
        return this.footprints;
    }

    /**
     * Adds an agent to the list of residing agents
     * @param agent the new Agent wishing to be added
     */
    public void addResidingAgent(BaseAgent agent) {
        this.residingAgents.add(agent);
    }

    /**
     * @return list of agents <i>currently</i> residing a server
     */
    public Vector<BaseAgent> getResidingAgents() {
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
     * Performs a handshake with an Agent using this server's secret.
     * @param agent the agent to handshake with
     */
    public void handshake(BaseAgent agent) {
        agent.handshake(secret);
    }

    /**
     * Neighbour discovery process that is periodically repeated in order to
     * keep track of active servers in the network.
     */
    private class NeighbourDiscovery extends TimerTask {

        /**
         * Creates a new task and initializes its neighbors.
         */
        public NeighbourDiscovery() {
            neighbours = new Vector<>();
        }

        /**
         * Periodically dispatches a neighbour discovery service and adds the results to
         * the list of known neighbours.
         */
        public void run() {
            try {
                log.info("Running neighbour discovery");
                DiscoveryClient discoveryClient = new DiscoveryClient(InetAddress.getByName(DEFAULT_MULTICAST_ADDRESS), DEFAULT_BASE_PORT);
                neighbours = discoveryClient.getDiscoveryResult();

                if (!neighbours.isEmpty()) {
                    // remove our self from the neighbour list
                    InetAddress localAddress = InetAddress.getLocalHost();
                    neighbours.removeIf(n -> n.getAddress().getHostAddress().equals(localAddress.getHostAddress()) && n.getPort() == serverPort);
                    log.info("Neighbours:" + neighbours.toString());
                }

            } catch (Exception ex) {
                System.out.println("error running thread " + ex.getMessage());
            }
        }
    }
}
