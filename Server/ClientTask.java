import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientTask implements Runnable {

  private final Socket tcpClientSocket;
  private final DatagramPacket dataPacket;
  private final DatagramSocket udpClientSocket;
  ProductTable table;

  ProductService service;
  AtomicInteger orderIdAtomicInteger;
  Orders orderList;

  public ClientTask(Socket clientSocket, ProductTable inventory, Orders orderList,
      AtomicInteger orderIdAtomicInteger) {
    this.tcpClientSocket = clientSocket;
    this.table = inventory;
    this.orderList = orderList;
    this.orderIdAtomicInteger = orderIdAtomicInteger;
    this.dataPacket = null;
    this.udpClientSocket = null;
  }

  public ClientTask(DatagramSocket dataSocket, DatagramPacket packet, ProductTable inventory,
      Orders orderList, AtomicInteger orderIdAtomicInteger) {
    this.udpClientSocket = dataSocket;
    this.dataPacket = packet;
    this.table = inventory;
    this.orderList = orderList;
    this.orderIdAtomicInteger = orderIdAtomicInteger;
    this.tcpClientSocket = null;
  }

  @Override
  public void run() {
    service = new ProductService(table, orderList, orderIdAtomicInteger);
    System.out.println("Got a client !");
    if (tcpClientSocket != null) {
      try {
        System.out.println("running tcp client thread");
        Scanner sc = new Scanner(tcpClientSocket.getInputStream());
        PrintWriter pout = new PrintWriter(tcpClientSocket.getOutputStream());
        while (true) {
          String command = sc.nextLine();
          System.out.println("received:" + command);
          Scanner st = new Scanner(command);
          String tag = st.next();
          String response;
          if (tag.equals("search")) {
            response = service.searchUserOrders(st.next());
            pout.println(response);
          } else if (tag.equals("purchase")) {
            String userName = st.next();
            String productName = st.next();
            int quantity = st.nextInt();
            response = service.purchase(userName, productName, quantity);
            pout.println(response);
          } else if (tag.equals("cancel")) {
            response = service.cancelOrder(st.nextInt());
            pout.println(response);
          } else if (tag.equals("list")) {
            response = service.listProducts();
            pout.println(response);
          } else if (tag.equals("close")) {
            tcpClientSocket.close();
          }
          pout.flush();

        }
      } catch (IOException e) {
        System.err.println(e);
      }
    } else if (udpClientSocket != null) {
      //add udp block here
      try {
        System.out.println("running udp client thread");
        while (true) {

          String command = new String(dataPacket.getData(),0,dataPacket.getLength());
          System.out.println("received:" + command);
          Scanner st = new Scanner(command);
          String tag = st.next();
          String response = null;
          if (tag.equals("search")) {
            response = service.searchUserOrders(st.next());
          } else if (tag.equals("purchase")) {
            String userName = st.next();
            String productName = st.next();
            int quantity = st.nextInt();
            response = service.purchase(userName, productName, quantity);
          } else if (tag.equals("cancel")) {
            response = service.cancelOrder(st.nextInt());
          } else if (tag.equals("list")) {
            response = service.listProducts();
          } else if (tag.equals("close")) {
            udpClientSocket.close();
          }
          byte[] responseData = response.getBytes();
          DatagramPacket sendPacket = new DatagramPacket(responseData, responseData.length,
              dataPacket.getAddress(), dataPacket.getPort());
          udpClientSocket.send(sendPacket);

        }
      } catch (IOException e) {
        System.err.println(e);
      }
    }
  }

}
