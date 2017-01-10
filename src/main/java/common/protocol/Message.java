package common.protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Message protocol for the njosnari agent framework.
 * The protocol is byte-based and supports a set of hard-coded op-codes.
 * Sending is done to multicast UDP sockets, and parsing is done on datagram packets.
 */
public class Message implements Serializable {
    /**
     * Delimiter used for separating data fields.
     */
    public static final java.lang.String DELIMITER = ",";
    private transient static Logger log = LogManager.getLogger(Message.class.getName());

    // Operations
    /**
     * Op-code indicating a service discovery request.
     */
    public static final byte OP_DISCOVERY_REQUEST  = 0;
    /**
     * Op-code indicating a service discovery response.
     */
    public static final byte OP_DISCOVERY_RESPONSE = 1;

    /**
     * Op-code for this Message.
     */
    public byte opCode;
    /**
     * Additional data for this Message.
     */
    public byte[] data;
    /**
     * Total length of Message op-code and data.
     */
    public int length;

    /**
     * Creates a new Message based on an op-code.
     * @param opCode the message's operation
     */
    public Message(byte opCode) {
        this.opCode = opCode;
        this.length = 0;
    }

    /**
     * Creates a new Message based on an op-code and additional data.
     * @param opCode the message's operation
     * @param data the additional data to be included
     */
    public Message(byte opCode, byte[] data) {
        this.opCode = opCode;
        this.data = data;
        this.length = data.length;
    }

    /**
     * Encodes this Message's contents and sends it to the given socket
     * using a datagram packet.
     * @param socket the socket to send this Message over
     * @param address the destination address
     * @param port the destination port
     * @throws IOException if the socket cannot send this Message
     */
    public void send(MulticastSocket socket, InetAddress address, int port) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream daos = new DataOutputStream(baos);
        daos.writeByte(opCode);
        daos.writeInt(length);
        if (length > 0) {
            daos.write(data);
        }
        daos.close();
        final byte[] bytes = baos.toByteArray();

        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
        socket.send(packet);
    }

    /**
     * Decodes the byte data from the given packet and constructs a new Message based on it.
     * @param packet the datagram to parse
     * @return a new message with the packet's data
     * @throws IOException if the packet's input stream cannot be read
     */
    public static Message parseMessage(DatagramPacket packet) throws IOException {
        byte[] packetBytes = packet.getData();
        int packetLength = packet.getLength();

        final ByteArrayInputStream bais = new ByteArrayInputStream(packetBytes);
        final DataInputStream dais = new DataInputStream(bais);

        byte packetOpCode = dais.readByte();
        int packetDataLength = dais.readInt();
        if (packetDataLength > 0) {
            byte[] packetData = new byte[packetDataLength];
            int readLength = dais.read(packetData);
            return new Message(packetOpCode, packetData);
        }
        else return new Message(packetOpCode);
    }
}
