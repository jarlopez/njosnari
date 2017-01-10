package common;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Represents a device in a network, identified by an address and port.
 */
public class Node implements Serializable {

    /**
     * Address of this Node.
     */
    private InetAddress address;
    /**
     * Available port on this node.
     */
    private int port;

    /**
     * Creates a new Node with an address and a port.
     * @param address the node's address
     * @param port the node's available port
     */
    public Node(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    /**
     * @return this node's address
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * @param address the address for this node
     */
    public void setAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * @return this node's port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port this node's available port
     */
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Node{" +
                "address=" + address +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (port != node.port) return false;
        return address != null ? address.equals(node.address) : node.address == null;
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    public String displayName() {
        return address.toString() + ":" + port;
    }
}
