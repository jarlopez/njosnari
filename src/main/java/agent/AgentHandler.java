package agent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Socket handler for managing incoming Agents.
 */
public class AgentHandler implements Runnable {

    /**
     * The socket the incoming Agent is connected to.
     */
    private Socket clientSocket;
    /**
     * The agent server which created this AgentHandler
     */
    private AgentServer agentServer;
    /**
     * TODO
     */
    private int serverPort;

    /**
     * Creates a new AgentHandler and sets critical fields.
     * @param clientSocket
     * @param agentServer
     * @param serverPort
     */
    public AgentHandler(Socket clientSocket, AgentServer agentServer, int serverPort) {
        this.clientSocket = clientSocket;
        this.agentServer = agentServer;
        this.serverPort = serverPort;
    }

    /**
     * Accepts an incoming Agent by extracting it, adding it to our current residing Agents,
     * and triggering its work function.
     */
    public void run() {
        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            Object inputObject = inputStream.readObject();

            if (inputObject instanceof BaseAgent)
            {
                BaseAgent agent = (BaseAgent)inputObject;
                this.agentServer.addResidingAgent(agent);
                agent.agentArrived(this.agentServer, InetAddress.getLocalHost(), 8084);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        catch (ClassNotFoundException cEx) {
            cEx.printStackTrace();
        } finally {
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
