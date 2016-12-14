package discovery;

import common.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Vector;

//multicast socket: http://download.java.net/jdk7/archive/b123/docs/api/java/net/MulticastSocket.html
//multicast wireless fix:
//http://stackoverflow.com/questions/18747134/getting-cant-assign-requested-address-java-net-socketexception-using-ehcache

public class DiscoveryClient {

    private MulticastSocket mcastSocket;
    private InetAddress mcastAddr;
    private int basePort;
    private Vector discoveryResults;


    public DiscoveryClient(InetAddress mcastAddr, int basePort) throws IOException, UnknownHostException {
        /*
          Open a MulticastSocket for sending and receiving discovery
          requests - and join the group.
        */
        this.discoveryResults = new Vector();
        this.mcastAddr = mcastAddr;
        this.basePort = basePort;
        this.mcastSocket = new MulticastSocket(basePort);
        this.mcastSocket.joinGroup(mcastAddr);

        discover(mcastSocket, basePort);
    }

    private void discover(MulticastSocket mcastSocket, int basePort) {
        /*
          Send a discovery packet to the multicast group. The destination
          port of the datagram packet to be sent as discovery packet must
          be the basePort. Receive (work with timeout) discovery
          replies, if any. Extract the source IP address and port from each
          received discovery reply packet. Store this information in a Vector.
        */

        try {
            String discoverMsg = "discovery";

            DatagramPacket hi = new DatagramPacket(discoverMsg.getBytes(), discoverMsg.length(), mcastAddr, basePort);
            mcastSocket.setSoTimeout(5000);
            mcastSocket.send(hi);

            while (true)
            {
                // get group responses
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                mcastSocket.receive(packet);
                String data = new String(buffer, 0, packet.getLength());
                System.out.println("Client: we got a response: " + data);

                if(data.equals("discovery-reply")) {
                    this.discoveryResults.add(new Node(packet.getAddress(), packet.getPort()));
                }
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();

            System.out.println("We got a socket timeout, let's print out all found servers");
            for (Object res : this.discoveryResults) {
                System.out.println(res.toString());
            }
        }
    }

    public Vector getDiscoveryResult() {
        /* Return the Vector that contains (IP address, port)-pairs */
        return this.discoveryResults;
    }

    public static void main (String[] args) {
        /*
          Create a DiscoveryClient and print out IP address-port-pairs under
          which a server was found (discovery reply received).
        */

        try {

            InetAddress mcastAddr = InetAddress.getByName("230.0.0.88");
            int basePort = 8082;
            new DiscoveryClient(mcastAddr, basePort);
        }
        catch (UnknownHostException uEx) {
            uEx.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
