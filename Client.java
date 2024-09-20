import java.io.IOException;
import java.util.*;

public class Client {

    static Connection conn;

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

    public Connection setmode() throws IOException {
        if (conn != null) {
            conn.closeConn();
        }
        System.out.println("setmode options:\ntcp|udp\nInput:");
        Scanner setmode = new Scanner(System.in);
        String mode = setmode.nextLine();
        System.out.println("Connection mode: "+mode);
        if (mode.equals("tcp")) {
            System.out.println("Establishing TCP Connection");
            conn = new ClientTCP();
            conn.connect();
        }  else {
            System.out.println("Establishing UDP Connection");
            conn = new ClientUDP();
            conn.connect();
        }
        return conn;
    }

    public void purchase() throws IOException  {
        System.out.println("purchase options: <username> <product> <quantity>\nInput:");
        Scanner purchase = new Scanner(System.in);
        String purchaseDetails = purchase.nextLine();
        conn.sendMessage("purchase " + purchaseDetails);
        String retVal = conn.receiveMessage();
        System.out.println(retVal);

    }

    public void cancel() throws IOException {
        System.out.println("provide the orderid for cancelling:");
        Scanner scancel = new Scanner(System.in);
        String cancelid = scancel.nextLine();
        conn.sendMessage("cancel " + cancelid);
   //     conn.sendMessage(cancelid);
        String retVal = conn.receiveMessage();
        System.out.println(retVal);
    }

    public String search() throws IOException {
        System.out.println("provide username for search:");
        Scanner ssearch = new Scanner(System.in);
        String searchid = ssearch.nextLine();
        conn.sendMessage("search " + searchid);
     //   conn.sendMessage(searchid);
        String retVal = conn.receiveMessage();
        System.out.println(retVal);
        return searchid;
    }

    public String list() throws IOException {
        System.out.println("listing all items in inventory");
        conn.sendMessage("list");
        String retVal = conn.receiveMessage();
        System.out.println(retVal);
        return "list";
    }

    public static void main(String args[]) throws IOException {
        Client client = new Client();
        String mode = "udp";
        String value = "";

        String retVal;

        while(true) {
            System.out.println("Choose one of the following commands:\nsetmode\npurchase\ncancel\nsearch\nlist");
            Scanner input = new Scanner(System.in);
            String cmd = input.next();
            System.out.println("Entered command is:"+cmd);
            client.commandCheck(cmd);
            if (cmd.equals("setmode")) {
                conn = client.setmode();
            }
            if (conn == null) {
                System.out.println("Connection mode is not yet. Setinng the default mode to TCP");
                conn = new ClientTCP();
                conn.connect();
            }
            System.out.println(conn);
            if (cmd.equals("purchase")) {
                client.purchase();

            }
            if (cmd.equals("cancel")) {
                client.cancel();
            }
            if (cmd.equals("search")) {
                value = client.search();
            }
            if (cmd.equals("list")) {
                value = client.list();

            }
    }
    }
}
