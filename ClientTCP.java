
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientTCP implements Connection {
    Scanner fromServer ;
    PrintStream toServer ;
    int port = 6666;
    String host = "127.0.0.1";
    Socket tcp;
    public void connect() throws UnknownHostException, IOException{
        System.out.println("Connecting to " + host +":"+ port);
        tcp = new Socket(host, port);
        fromServer = new Scanner(tcp.getInputStream());
        toServer = new PrintStream(tcp.getOutputStream()) ;
    }

    public void sendMessage(String input) {
        System.out.println("Sending: " +input);
        toServer.println(input);
    }

    public String receiveMessage() {
        String retVal = "";
        String val;
        while (fromServer.hasNext()) {
            val = fromServer.next();
            retVal += val;
            System.out.println("Response from server:" + retVal);
            //if (val.isEmpty())) {
            //    break;
            //}
        }
        System.out.println("Response from server:" + retVal);
        return retVal;
    }

    public void closeConn() throws IOException{
        fromServer.close();
        toServer.close();
        tcp.close();
    }
}
