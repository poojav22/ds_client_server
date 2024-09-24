import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

public class Client {

    static Connection conn;
    static String hostAddress;
    static int tcpPort;
    static int udpPort;

    public void commandCheck(String cmd) {
        ArrayList<String> validCommandList = new ArrayList<>();
        validCommandList.add("setmode");
        validCommandList.add("purchase");
        validCommandList.add("cancel");
        validCommandList.add("search");
        validCommandList.add("list");

        if (validCommandList.contains(cmd))
            System.out.println(cmd +" exists in the set of valid commands");
        else
            System.err.println("Not a valid command. Try again...");
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

    public void purchase(String cmd) throws IOException  {
        //System.out.println("purchase options: <username> <product> <quantity>\nInput:");
        //Scanner purchase = new Scanner(System.in);
        //String purchaseDetails = purchase.nextLine();
        checkConn();
        conn.sendMessage(cmd);
        String retVal = conn.receiveMessage();
        System.out.println(retVal);
    }

    public void cancel(String cmd) throws IOException {
        //System.out.println("provide the orderid for cancelling:");
        //Scanner scancel = new Scanner(System.in);
        //String cancelid = scancel.nextLine();
        checkConn();
        conn.sendMessage(cmd);
   //     conn.sendMessage(cancelid);
        String retVal = conn.receiveMessage();
        System.out.println(retVal);
    }

    public void search(String cmd) throws IOException {
        //System.out.println("provide username for search:");
        //Scanner ssearch = new Scanner(System.in);
        //String searchid = ssearch.nextLine();
        checkConn();
        conn.sendMessage(cmd);
     //   conn.sendMessage(searchid);
        String retVal = conn.receiveMessage();
        System.out.println(retVal);
        //return searchid;
    }

    public String list() throws IOException {
        //System.out.println("listing all items in inventory");
        checkConn();
        conn.sendMessage("list");
        String retVal = conn.receiveMessage();
        System.out.println(retVal);
        return "list";
    }

    public static void main (String[] args) throws IOException {

        Client client = new Client();

        if (args.length != 3) {
          System.out.println("ERROR: Provide 3 arguments");
          System.out.println("\t(1) <hostAddress>: the address of the server");
          System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
          System.out.println("\t(3) <udpPort>: the port number for UDP connection");
          System.exit(-1);
        }

        hostAddress = args[0];
        tcpPort = Integer.parseInt(args[1]);
        udpPort = Integer.parseInt(args[2]);

        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode")) {
                conn = client.setmode(tokens[1], hostAddress, tcpPort, udpPort);
          }
          else if (tokens[0].equals("purchase")) {
            client.purchase(cmd);
          } else if (tokens[0].equals("cancel")) {
            client.cancel(cmd);
          } else if (tokens[0].equals("search")) {
            client.search(cmd);
          } else if (tokens[0].equals("list")) {
            client.list();
          } else {
            System.out.println("ERROR: No such command" + tokens[0]);
          }

        }
      }
}
