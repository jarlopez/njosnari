package agent;

import common.Node;
import discovery.DiscoveryClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import static agent.AgentServer.DEFAULT_MULTICAST_ADDRESS;
import static agent.AgentServer.DEFAULT_BASE_PORT;

public class AgentClient {

    Agent agent;
    Socket sendingSocket;

    int listeningPort;
    ServerSocket listeningSocket;

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
            Agent agent = new Agent(new Node(InetAddress.getLocalHost(), this.listeningPort));
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

            if (inputObject instanceof Agent)
            {
                Agent agent = (Agent)inputObject;
                agent.printReport();
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
            int clientPort = 0;
            if(args.length > 0) {
                clientPort = Integer.parseInt(args[0]);
            }
            else {
                throw new Exception("Invalid argument exception");
            }

            new AgentClient(clientPort);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
