package discovery;


import common.protocol.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class DiscoveryServer extends Thread {
    private static Logger log = LogManager.getLogger(DiscoveryServer.class.getName());

    private static final int DEFAULT_SERVER_PORT = 8081;
    private static final int DEFAULT_BASE_PORT = 8082;
    public static final String AGENT_SERVER_SERVICE = "agent-server";

    private InetAddress mcastAddr;
    private int basePort;
    private MulticastSocket receiveSocket;
    private MulticastSocket replySocket;

    private ArrayList<String> services = new ArrayList<>();

    public DiscoveryServer(InetAddress mcastAddr, int basePort, int serverPort)
            throws java.io.IOException, UnknownHostException {
        /*
          Open two MulticastSockets, one for receiving (basePort)
          and one for replying (serverPort) discovery requests.
        */
        this.services.add(AGENT_SERVER_SERVICE);
        this.mcastAddr = mcastAddr;
        this.basePort = basePort;
        this.receiveSocket = new MulticastSocket(basePort);
        this.receiveSocket.joinGroup(mcastAddr);
        this.replySocket = new MulticastSocket(serverPort);
        this.replySocket.joinGroup(mcastAddr);

        this.start();
    }

    public void run() {
        /*
          Listen for discovery requests, receive and reply them.
        */
        log.info("Running server on port " + basePort);
        try {

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                receiveSocket.receive(packet);

                Message message = Message.parseMessage(packet);
                switch (message.opCode) {
                    case Message.OP_DISCOVERY_REQUEST:
                        log.info("Received request!");
                        if (message.length > 0) {
                            String servicesStr = new String(message.data);
                            String[] services = servicesStr.split(Message.DELIMITER);
                            log.info("With data: " + Arrays.toString(services));
                            // TODO Could do server-side filtering to determine if OUR services intersect with requested
                        }
                        Message reply = new Message(Message.OP_DISCOVERY_RESPONSE, String.join(Message.DELIMITER, services).getBytes());
                        reply.send(replySocket, mcastAddr, basePort);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            InetAddress mcastAddr = InetAddress.getByName("230.0.0.88");
            int basePort = DEFAULT_BASE_PORT;
            int serverPort = DEFAULT_SERVER_PORT;

            if (args.length > 0) {
                serverPort = Integer.parseInt(args[0]);
            }
            new DiscoveryServer(mcastAddr, basePort, serverPort);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}