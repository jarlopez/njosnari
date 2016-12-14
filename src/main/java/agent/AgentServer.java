package agent;

import common.Footprint;
import common.Node;
import discovery.DiscoveryServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.ExportException;
import java.util.Vector;

public class AgentServer implements IAgentServer{

    /*
    An agent server accepts TCP socket connections on a server port to receive agents.
    This TCP port has the same port number as the one that is used for sending
    discovery replies.
     */
    public static final String DEFAULT_MULTICAST_ADDRESS = "230.0.0.88";
    public static final int DEFAULT_BASE_PORT = 8082;

    private ServerSocket serverSocket;
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
            System.err.println("Could not listen on port: " + this.serverPort);
            System.exit(1);
        }

        System.out.println("The AgentServer is running...");

        // run the discovery service in new thread
        try {
            new DiscoveryServer(InetAddress.getByName(DEFAULT_MULTICAST_ADDRESS), DEFAULT_BASE_PORT, this.serverPort);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

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

        System.out.println("Agent: " + agent + " wants to migrate home/to next node");

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
            if(args.length > 0) {
                int serverPort = Integer.parseInt(args[0]);
                new AgentServer(serverPort);
            }
            else {
                throw new Exception("Invalid argument exception");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
