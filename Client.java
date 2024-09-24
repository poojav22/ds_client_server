import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class Client {

    static Connection conn;
    static String hostAddress;
    static int tcpPort;
    static int udpPort;

    public boolean commandCheck(String cmd) {
        ArrayList<String> validCommandList = new ArrayList<>();
        validCommandList.add("setmode");
        validCommandList.add("purchase");
        validCommandList.add("cancel");
        validCommandList.add("search");
        validCommandList.add("list");

        if (!validCommandList.contains(cmd)) {
            System.err.println("Not a valid command. Try again...");
            return false;
        }
        return true;
    }

    public Connection setmode(String mode, String hostAddress, int tcpPort, int udpPort) throws IOException {
        if (conn != null) {
            conn.closeConn();
        }
        if (mode.equals("T")) {
            //System.out.println("Establishing TCP Connection");
            conn = new ClientTCP(hostAddress, tcpPort);
            conn.connect();
        }  else {
            //System.out.println("Establishing UDP Connection");
            conn = new ClientUDP(hostAddress, udpPort);
            conn.connect();
        }
        return conn;
    }

    public void checkConn() throws UnknownHostException, IOException {
        if (conn == null) {
            //System.out.println("Connection mode is not yet. Setting the default mode to TCP");
            conn = new ClientTCP(hostAddress, tcpPort);
            conn.connect();
        }
    }

    public String sendCommand(String cmd) throws IOException  {
        checkConn();
        conn.sendMessage(cmd);
        return conn.receiveMessage();
    }

    public static void main (String[] args) throws IOException {

        Client client = new Client();

        if (args.length > 0) {
            hostAddress = args[0];
            tcpPort = Integer.parseInt(args[1]);
            udpPort = Integer.parseInt(args[2]);
        } else {

            hostAddress = "127.0.0.1";
            tcpPort = 8000;
            udpPort = 8000;
        }

        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");
          if(!client.commandCheck(tokens[0]))
              continue;
          if (tokens[0].equals("setmode")) {
                conn = client.setmode(tokens[1], hostAddress, tcpPort, udpPort);
          }
          else
            System.out.println(client.sendCommand(cmd));
        }
      }
}

interface Connection {
    void connect() throws UnknownHostException, IOException;
    void sendMessage(String value) throws IOException ;
    String receiveMessage() throws IOException ;
    void closeConn()throws IOException;
}

class ClientTCP implements Connection {
    BufferedReader fromServer;
    PrintStream toServer ;
    int port = 8000;
    String host = "127.0.0.1";
    Socket tcp;
    public ClientTCP(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public void connect() throws UnknownHostException, IOException{
        //System.out.println("Connecting to " + host +":"+ port);
        tcp = new Socket(host, port);
        fromServer = new BufferedReader(new InputStreamReader(tcp.getInputStream()));
        toServer = new PrintStream(tcp.getOutputStream()) ;
    }

    public void sendMessage(String input) {
        //System.out.println("Sending: " +input);
        toServer.println(input);
    }

    public String receiveMessage() throws IOException {
        boolean first = true;
        String retVal="";

        do {
            String newline = "\n";
            if(first) {
                newline = "";
                first = false;
            }
            retVal += newline + fromServer.readLine();

        } while (fromServer.ready());
        return retVal;
    }

    public void closeConn() throws IOException{
        fromServer.close();
        toServer.close();
        tcp.close();
    }
}

class ClientUDP implements  Connection{
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
