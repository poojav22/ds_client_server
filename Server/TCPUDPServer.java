import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TCPUDPServer {

//
//1. Use multi-threading - threads/ executor service.newCachedThreadPool or threadpools
//2. Use synchronized keyword for file access blocks
//3. Use Atomic integer for increment operation(order id)
//
//  Just create a new ServerSocket(port) for TCP and new DatagramSocket(port) for UDP
//  and have a thread listening to each. The TCP thread should loop calling accept() and
//  spawning a new thread per accepted Socket;the UDP thread can just loop on DatagramSocket.receive().

  public static void main(String[] args) throws IOException {
    //read input file and write to hashmap
    String filePath = "products.txt";
    File file = new File(filePath);
    ProductTable inventory = new ProductTable(file);
    Orders orders = new Orders();
    AtomicInteger orderIdIterator = new AtomicInteger(0);
    new TCPUDPServer().startServer(inventory, orders, orderIdIterator);
  }

  public void startServer(ProductTable inventory, Orders orders, AtomicInteger orderIdIterator) {
    final ExecutorService clientProcessingPool = Executors.newCachedThreadPool();

    Runnable tcpServerTask = new Runnable() {
      @Override
      public void run() {
        try {
          ServerSocket serverSocket = new ServerSocket(6666);
          System.out.println("TCP Server is running and Waiting for clients to connect...");
          while (true) {
            Socket clientSocket = serverSocket.accept();
            clientProcessingPool.submit(
                new ClientTask(clientSocket, inventory, orders, orderIdIterator));
          }
        } catch (IOException e) {
          System.err.println("Unable to process client request");
          e.printStackTrace();
        }
      }
    };

    Runnable udpServerTask = new Runnable() {
      @Override
      public void run() {
        try {
          DatagramPacket recievePacket;
          int port = 7000;
          int len = 1024;
          DatagramSocket dataSocket = new DatagramSocket(port);
          System.out.println("UDP Server is running and Waiting for clients to connect...");
          byte[] buf = new byte[len];
          while (true) {
            recievePacket = new DatagramPacket(buf, buf.length);
            dataSocket.receive(recievePacket);
            clientProcessingPool.submit(
                new ClientTask(dataSocket, recievePacket, inventory, orders, orderIdIterator));
          }
        } catch (SocketException e) {
          System.err.println("Unable to process client request");
          e.printStackTrace();
        } catch (IOException e) {
          System.err.println("Unable to process client request");
          e.printStackTrace();
        }

      }
    };

    Thread tcpServerThread = new Thread(tcpServerTask);
    tcpServerThread.start();
    Thread udpServerThread = new Thread(udpServerTask);
    udpServerThread.start();
  }

}
