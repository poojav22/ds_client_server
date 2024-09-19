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
        System.out.println("setmode options:\nTCP\nUDP\nInput:");
        Scanner setmode = new Scanner(System.in);
        String mode = setmode.nextLine();
        System.out.println("Connection mode:"+mode);
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

    public String purchase()  {
        System.out.println("purchase options: <username> <product> <quantity>\nInput:");
        Scanner purchase = new Scanner(System.in);
        String purchaseDetails = purchase.nextLine();
        return purchaseDetails;

    }

    public String cancel() {
        System.out.println("provide the orderid for cancelling:");
        Scanner scancel = new Scanner(System.in);
        String cancel = scancel.nextLine();
        return cancel;
    }

    public String search() {
        System.out.println("provide username for search:");
        Scanner ssearch = new Scanner(System.in);
        String search = ssearch.nextLine();
        return search;
    }

    public String list() {
        System.out.println("listing all items in inventory");
        return "list";
    }

    public static void main(String args[]) throws IOException {
        Client client = new Client();
        String mode = "udp";
        String value = "";

        String retVal;

        while(true) {
            System.out.println("Choose one of the following commands:\n1. setmode\n2. purchase\n3. cancel\n4. search\n5. list");
            Scanner input = new Scanner(System.in);
            String cmd = input.nextLine();
            System.out.println("Entered command is:"+cmd);
            client.commandCheck(cmd);
            if (cmd.equals("setmode")) {
                conn = client.setmode();
            }
            System.out.println(conn);
            if (cmd.equals("purchase")) {
                value = client.purchase();
                retVal = conn.sendMessage(value);
                System.out.println(retVal);
            }
            if (cmd.equals("cancel")) {
                value = client.cancel();
                retVal = conn.sendMessage(value);
                System.out.println(retVal);
            }
            if (cmd.equals("search")) {
                value = client.search();
                retVal = conn.sendMessage(value);
                System.out.println(retVal);
            }
            if (cmd.equals("list")) {
                value = client.list();
                retVal = conn.sendMessage(value);
                System.out.println(retVal);
            }
            conn = new ClientUDP();
            conn.connect();
    }
    }
}
