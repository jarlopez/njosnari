package dev.discovery;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import static dev.discovery.DiscoveryServer.DEFAULT_BASE_PORT;
import static dev.discovery.DiscoveryServer.DEFAULT_MCAST_IP;

public class DiscoveryClient {
    public DiscoveryClient(java.net.InetAddress mcastAddr, int basePort)
            throws java.io.IOException, java.net.UnknownHostException {
        /*
          Open a MulticastSocket for sending and receiving discovery
          requests.
        */
        SocketAddress socketAddress = new InetSocketAddress(mcastAddr, basePort);
        MulticastSocket mcastSocket = new MulticastSocket(socketAddress);
        discover(mcastSocket, basePort);
    }

    private void discover(java.net.MulticastSocket mSocket, int basePort) throws IOException {
        /*
          Send a discovery packet to the multicast group. The destination
          port of the datagram packet to be sent as discovery packet must
          be the basePort. Receive (work with timeout) discovery
          replies, if any. Extract the source IP address and port from each
          received discovery reply packet. Store this information in a Vector.
        */
        byte[] buf = new byte[256];
        InetAddress group = InetAddress.getByName(DEFAULT_MCAST_IP);
        DatagramPacket packet;
        System.out.println("Sending MCAST");
        buf = "Hello!".getBytes("UTF-8");
        packet = new DatagramPacket(buf, buf.length, group, basePort);
        mSocket.send(packet);
    }

    public List getDiscoveryResult() {
        /* Return the Vector that contains (IP address, port)-pairs */
        return new ArrayList();
    }

    public static void main(String[] args) throws IOException {
        /*
          Create a DiscoveryClient and print out IP address-port-pairs under
          which a server was found (discovery reply received).
        */
        InetAddress mcastAddr = InetAddress.getByName(DEFAULT_MCAST_IP);
        DiscoveryClient client = new DiscoveryClient(mcastAddr, DEFAULT_BASE_PORT);
    }
}