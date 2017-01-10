package discovery;

import common.Node;
import common.protocol.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Vector;

//multicast socket: http://download.java.net/jdk7/archive/b123/docs/api/java/net/MulticastSocket.html
//multicast wireless fix:
//http://stackoverflow.com/questions/18747134/getting-cant-assign-requested-address-java-net-socketexception-using-ehcache

/**
 * Runs a timeout-driven discovery mechanism over UDP for finding
 * agent service providers on a multicast address.
 */
public class DiscoveryClient {
    private static Logger log = LogManager.getLogger(DiscoveryClient.class.getName());

    /**
     * Socket used for sending and receiving discovery messages on.
     */
    private MulticastSocket mcastSocket;
    /**
     * Multicast address for sending discovery messages to.
     */
    private InetAddress mcastAddr;
    /**
     * Port used for sending discovery messages to.
     */
    private int basePort;
    /**
     * Collection of discovered nodes.
     */
    private Vector<Node> discoveryResults;


    /**
     * Creates a  new discovery client and opens a multicast socket for
     * sending and receiving discovery requests.
     * @param mcastAddr multicast address for sending discovery messages to
     * @param basePort port used for sending discovery messages to
     * @throws IOException if this client cannot join the multicast group or if the port is occupied
     */
    public DiscoveryClient(InetAddress mcastAddr, int basePort) throws IOException {
        /*
          Open a MulticastSocket for sending and receiving discovery
          requests - and join the group.
        */
        this.discoveryResults = new Vector<>();
        this.mcastAddr = mcastAddr;
        this.basePort = basePort;
        this.mcastSocket = new MulticastSocket(basePort);
        this.mcastSocket.joinGroup(mcastAddr);

        discover(mcastSocket, basePort);
    }

    /**
     * Sends a discovery message to the multicast group and collects their responses,
     * reporting the results after a timeout.
     * @param mcastSocket
     * @param basePort
     */
    private void discover(MulticastSocket mcastSocket, int basePort) {
        /*
          The destination port of the datagram packet to be sent as discovery packet must
          be the basePort. Receive (work with timeout) discovery
          replies, if any. Extract the source IP address and port from each
          received discovery reply packet. Store this information in a Vector.
        */

        try {
            mcastSocket.setSoTimeout(5000);
            Message discoveryMessage = new Message(Message.OP_DISCOVERY_REQUEST);
            /* NOTE: We could append data to our discovery message to explicitly ask for specific services:
                    String[] requestedServices = {DiscoveryServer.AGENT_SERVER_SERVICE, "some-other-service"};
                    Message msg = new Message(Message.OP_DISCOVERY_REQUEST, String.join(Message.DELIMITER, requestedServices).getBytes());
            */

            discoveryMessage.send(mcastSocket, mcastAddr, basePort);

            while (true)  {
                // get group responses
                byte[] buffer = new byte[1024];
                DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
                mcastSocket.receive(recvPacket);

                Message message = Message.parseMessage(recvPacket);
                switch (message.opCode) {
                    case Message.OP_DISCOVERY_RESPONSE:
                        log.info("Received response!");
                        if (message.length > 0) {
                            String servicesStr = new String(message.data);
                            String[] services = servicesStr.split(Message.DELIMITER);
                            log.info("With data: " + Arrays.toString(services));
                            this.discoveryResults.add(new Node(recvPacket.getAddress(), recvPacket.getPort()));
                        }
                }
            }
        } catch (SocketTimeoutException e) {
            StringBuilder sb = new StringBuilder("Received socket timeout; discovered servers:");
            for (Node res : this.discoveryResults) {
                sb.append("\n\t").append(res.toString());
            }
            log.warn(sb.toString());
        } catch (IOException e) {
            log.error("Received IO exception: ");
            e.printStackTrace();
        }
    }

    /**
     * Gets the (IP-address, port) pairs of discovered nodes.
     * @return all discovered nodes
     */
    public Vector<Node> getDiscoveryResult() {
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
