
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
        tcp = new Socket(host, port);
        fromServer = new Scanner(tcp.getInputStream());
        toServer = new PrintStream(tcp.getOutputStream()) ;
    }

    public String sendMessage(String input) {
        toServer.println(input);
        String ret_value = fromServer.nextLine();
        return ret_value;
    }

    public void closeConn() throws IOException{
        fromServer.close();
        toServer.close();
        tcp.close();
    }
}
