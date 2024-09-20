import java.io.IOException;
import java.net.UnknownHostException;

public interface Connection {
     void connect() throws UnknownHostException, IOException;
     void sendMessage(String value) throws IOException ;
     String receiveMessage() throws IOException ;
     void closeConn()throws IOException;
}
