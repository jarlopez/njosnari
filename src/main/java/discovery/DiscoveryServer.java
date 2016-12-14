package discovery;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class DiscoveryServer extends Thread {
    private static Logger log = LogManager.getLogger(DiscoveryServer.class.getName());

    private static final int DEFAULT_SERVER_PORT = 8081;
    private static final int DEFAULT_BASE_PORT = 8082;

    private InetAddress mcastAddr;
    private int basePort;
    private MulticastSocket receiveSocket;
    private MulticastSocket replySocket;

    public DiscoveryServer(InetAddress mcastAddr, int basePort, int serverPort)
            throws java.io.IOException, UnknownHostException {
        /*
          Open two MulticastSockets, one for receiving (basePort)
          and one for replying (serverPort) discovery requests.
        */
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
                String data = new String(buffer, 0, packet.getLength());
                log.debug("Received response: " + data);

                if (data.equals("discovery")) {
                    log.debug("Replying to DISCOVERY message");
                    String reply = "discovery-reply";
                    DatagramPacket hi = new DatagramPacket(reply.getBytes(), reply.length(), mcastAddr, basePort);
                    replySocket.send(hi);
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