package dev.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class DiscoveryServer extends Thread {
    public static final String DEFAULT_MCAST_IP = "224.0.0.3";
    public static final int DEFAULT_SERVER_PORT = 9090;
    public static final int DEFAULT_BASE_PORT = 9093;

    private final InetAddress mcastAddr;
    private final int basePort;
    private final int serverPort;

    public DiscoveryServer(java.net.InetAddress mcastAddr, int basePort, int serverPort)
            throws java.io.IOException, java.net.UnknownHostException {
        /*
          Open two MulticastSockets, one for receiving (basePort)
          and one for replying (serverPort) discovery requests.
        */
        this.mcastAddr = mcastAddr;
        this.basePort = basePort;
        this.serverPort = serverPort;
        this.start();
    }

    public void run() {
        /*
          Listen for discovery requests, receive and reply them.
        */
        byte[] buf = new byte[256];
        try (MulticastSocket clientSocket = new MulticastSocket(DEFAULT_BASE_PORT)) {
            clientSocket.joinGroup(mcastAddr);
            System.out.println("Starting loop");
            while (true) {
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                clientSocket.receive(msgPacket);
                String msg = new String(buf, 0, buf.length);
                System.out.println("Socket 1 received msg: " + msg);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        try {
            InetAddress mcastAddr = InetAddress.getByName(DEFAULT_MCAST_IP);
            new DiscoveryServer(mcastAddr, DEFAULT_BASE_PORT, DEFAULT_SERVER_PORT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
