package common;

import java.io.Serializable;
import java.net.InetAddress;

public class Node implements Serializable {

    private InetAddress address;
    private int port;

    public Node(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

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
}
