package agent;

import discovery.DiscoveryServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.ExportException;

public class AgentServer implements IAgentServer{

    /*
    An agent server accepts TCP socket connections on a server port to receive agents.
    This TCP port has the same port number as the one that is used for sending
    discovery replies (see the figure below).
     */
    public static final String DEFAULT_MULTICAST_ADDRESS = "230.0.0.88";
    public static final int DEFAULT_BASE_PORT = 8082;

    private ServerSocket serverSocket;
    private int serverPort;

    public AgentServer(int serverPort) {
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
            try {
                Socket clientSocket = serverSocket.accept();
                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                Object inputObject = inputStream.readObject();

                if (inputObject instanceof Agent)
                {
                    Agent agent = (Agent)inputObject;
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
        }
    }

    public void agentMigrate(Agent agent, InetAddress dstAddr, int dstPort) {

        System.out.println("Agent: " + agent + " want to migrate home");
    }

    public static void main (String[] args) {
        try {
            int serverPort = 0;
            if(args.length > 0) {
                serverPort = Integer.parseInt(args[0]);
            }
            else {
                throw new Exception("Invalid argument exception");
            }

            new AgentServer(serverPort);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
