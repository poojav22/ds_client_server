
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientUDP implements  Connection{
    DatagramSocket udp;
    public void connect() throws SocketException, UnknownHostException{
       udp = new DatagramSocket(7999, InetAddress.getLocalHost());

    }

    public String sendMessage(String value) throws IOException {
        byte[] buf = value.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        udp.send(packet);
        DatagramPacket rpacket = new DatagramPacket(buf, buf.length);
        udp.receive(rpacket);
        String retVal = new String(rpacket.getData(), 0, rpacket.getLength());
        return retVal;
    }

    public void closeConn() throws IOException{
        udp.close();

    }
}
