package common.protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Message protocol for the njosnari agent framework.
 * Messages are sent in the following format:
 */
public class Message implements Serializable {
    public static final java.lang.String DELIMITER = ",";
    private transient static Logger log = LogManager.getLogger(Message.class.getName());

    // Operations
    public static final byte OP_DISCOVERY_REQUEST  = 0;
    public static final byte OP_DISCOVERY_RESPONSE = 1;

    public byte opCode;
    public byte[] data;
    public int length;

    public Message(byte opCode) {
        this.opCode = opCode;
        this.length = 0;
    }

    public Message(byte opCode, byte[] data) {
        this.opCode = opCode;
        this.data = data;
        this.length = data.length;
    }

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
