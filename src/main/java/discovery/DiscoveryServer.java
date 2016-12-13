package discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class DiscoveryServer extends Thread {

    InetAddress mcastAddr;
    int basePort;
    MulticastSocket receiveSocket;
    MulticastSocket replySocket;

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

    public void run () {
        /*
          Listen for discovery requests, receive and reply them.
        */
        try {

            while (true)
            {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                receiveSocket.receive(packet);
                String data = new String(buffer, 0, packet.getLength());
                System.out.println("Server: we got a response: " + data);

                if(data.equals("discovery"))
                {
                    // reply
                    String reply = "discovery-reply";
                    DatagramPacket hi = new DatagramPacket(reply.getBytes(), reply.length(), mcastAddr, basePort);
                    replySocket.send(hi);
                }
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main (String[] args) {
        try {

            InetAddress mcastAddr = InetAddress.getByName("230.0.0.88");
            int basePort = 8082;
            int serverPort = 0;

            if(args.length > 0) {
                serverPort = Integer.parseInt(args[0]);
            }
            else {
                throw new Exception("Invalid argument exception");
            }

            new DiscoveryServer(mcastAddr, basePort, serverPort);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}