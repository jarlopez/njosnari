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

public class DiscoveryClient {
    private static Logger log = LogManager.getLogger(DiscoveryClient.class.getName());

    private MulticastSocket mcastSocket;
    private InetAddress mcastAddr;
    private int basePort;
    private Vector<Node> discoveryResults;


    public DiscoveryClient(InetAddress mcastAddr, int basePort) throws IOException, UnknownHostException {
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

    private void discover(MulticastSocket mcastSocket, int basePort) {
        /*
          Send a discovery packet to the multicast group. The destination
          port of the datagram packet to be sent as discovery packet must
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

    public Vector<Node> getDiscoveryResult() {
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
