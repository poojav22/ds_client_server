import java.io.IOException;
import java.net.UnknownHostException;

public interface Connection {
     void connect() throws UnknownHostException, IOException;
     String sendMessage(String value) throws IOException ;
     void closeConn()throws IOException;
}
