
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientUDP implements  Connection{
    DatagramSocket udp;
    int port = 8000;
    String hostname = "localhost";
    InetAddress host;

    public ClientUDP(String host, int port) {
        this.hostname = host;
        this.port = port;
    }

    public void connect() throws SocketException, UnknownHostException{
        host = InetAddress.getByName(hostname);
        //System.out.println("Connecting to " + host +":"+ port);
       udp = new DatagramSocket();

    }

    public void sendMessage(String value) throws IOException {
        //System.out.println("Sending: " +value);
        byte[] buf = value.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, host, port);
        udp.send(packet);

    }

    public String receiveMessage() throws IOException{
        int len = 1024;
        byte[] buf = new byte[len];
        DatagramPacket rpacket = new DatagramPacket(buf, buf.length);
        udp.receive(rpacket);
        String retVal = new String(rpacket.getData(), 0, rpacket.getLength());
        return retVal;
    }
    public void closeConn() throws IOException{
        udp.close();

    }
}
