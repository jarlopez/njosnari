package agent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

public class AgentHandler implements Runnable {

    private Socket clientSocket;
    private AgentServer agentServer;
    private int serverPort;

    public AgentHandler(Socket clientSocket, AgentServer agentServer, int serverPort) {
        this.clientSocket = clientSocket;
        this.agentServer = agentServer;
        this.serverPort = serverPort;
    }

    public void run() {

        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            Object inputObject = inputStream.readObject();

            if (inputObject instanceof Agent)
            {
                Agent agent = (Agent)inputObject;
                this.agentServer.addResidingAgent(agent);
                agent.agentArrived(this.agentServer, InetAddress.getLocalHost(), 8084);
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
