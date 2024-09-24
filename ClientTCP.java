
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class ClientTCP implements Connection {
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
