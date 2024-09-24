import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Server {

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
    new Server().startServer(inventory, orders, orderIdIterator);
  }

  public void startServer(ProductTable inventory, Orders orders, AtomicInteger orderIdIterator) {
    final ExecutorService clientProcessingPool = Executors.newCachedThreadPool();

    Runnable tcpServerTask = new Runnable() {
      @Override
      public void run() {
        try {
          ServerSocket serverSocket = new ServerSocket(8000);
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
          int port = 8000;
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

class ClientTask implements Runnable {

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
          while (tcpClientSocket != null) {
            String command = sc.nextLine();
            System.out.println("received:" + command);
            Scanner st = new Scanner(command);
            String tag = st.next();
            System.out.println("tag:" + tag);
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
            }
            pout.flush();
          }
        } catch (IOException e) {
          System.err.println(e);
        }
      } else if (udpClientSocket != null) {

        try {
          if (tcpClientSocket != null) {
            tcpClientSocket.close();
          }
          System.out.println("running udp client thread");
          String command = new String(dataPacket.getData(), 0, dataPacket.getLength());
          System.out.println("received:" + command);
          Scanner st = new Scanner(command);
          String tag = st.next();
          System.out.println("tag:" + tag);
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
          }
          byte[] responseData = response.getBytes();
          DatagramPacket sendPacket = new DatagramPacket(responseData, responseData.length,
              dataPacket.getAddress(), dataPacket.getPort());
          udpClientSocket.send(sendPacket);
        } catch (IOException e) {
          System.err.println(e);
        }
      }
    }

  }

  class ProductService {

    final String INVALID_ITEM_COUNT = "Invalid item quantity provided. cannot order negative items.";
    final String ITEMS_UNAVAILABLE = "Not Available - Not enough items";
    final String INVALID_PRODUCT = "Not Available - We do not sell this product";
    final String PURCHASE_SUCCESS = "Your order has been placed, %d %s %s %d";
    final String CANCEL_SUCCESS = "Order %d is canceled";
    final String SEARCH_EMPTY = "No order found for %s";
    final String ORDER_NOT_EXISTS = "%d not found, no such order";
    ProductTable table;
    AtomicInteger orderIdAtomicInteger;
    Orders orderList;


    public ProductService(ProductTable table, Orders orderList, AtomicInteger orderIdInteger) {
      this.table = table;
      this.orderList = orderList;
      this.orderIdAtomicInteger = orderIdInteger;
    }

    public String purchase(String userName, String productName, int quantity) {
      if (quantity < 0) {
        return INVALID_ITEM_COUNT;
      }
      String action = "purchase";
      int updateProductTable = table.update(productName, quantity, action);
      switch (updateProductTable) {
        case -1:
          return INVALID_PRODUCT;
        case 0:
          return ITEMS_UNAVAILABLE;
        case 1: {
          int orderId = orderIdAtomicInteger.incrementAndGet();
          if (orderList.insert(orderId, userName, productName, quantity)) {
            return String.format(PURCHASE_SUCCESS, orderId, userName, productName, quantity);
          }
          return "Order Insert Failed. Order Id exists";
        }
        default:
          return "Result Unknown";
      }
    }

    public String cancelOrder(int orderId) {
      System.out.println("Initiating Order cancel");
      Orders.Order cancelledOrder = orderList.cancel(orderId);
      if (cancelledOrder == null) {
        return String.format(ORDER_NOT_EXISTS, orderId);
      }
      String action = "cancel";
      int updateProductTable = table.update(cancelledOrder.productName, cancelledOrder.quantity,
          action);
      switch (updateProductTable) {
        case -1:
          return INVALID_PRODUCT;
        case 1:
          return String.format(CANCEL_SUCCESS, orderId);
        default:
          return "Table update Failed";
      }
    }

    public String searchUserOrders(String userName) {
      List<Orders.Order> searchOrders = orderList.search(userName);
      if (searchOrders.isEmpty()) {
        return String.format(SEARCH_EMPTY, userName);
      } else {
        return searchOrders.stream().map(o -> mapOrder(o))
            .collect(Collectors.joining(System.getProperty("line.separator")));
      }
    }

    private String mapOrder(Orders.Order order) {
      return String.format("%d, %s, %d", order.orderId, order.productName, order.quantity);
    }

    public String listProducts() {
      return table.list();

    }

  }

  class ProductTable {

    public String productName;
    public Integer quantity;

    public HashMap<String, Integer> productInventoryMap;

    public ProductTable(File inputFile) throws IOException {

      productInventoryMap = new HashMap<String, Integer>();
      Files.lines(inputFile.toPath())
          .map(l -> l.split(" "))
          .forEach(a -> productInventoryMap.put(a[0], Integer.valueOf(a[1])));

    }

    public synchronized Integer get(String productName) {
      System.out.println("Getting quantity for " + productName);
      return productInventoryMap.get(productName);
    }

    // returns 0 if old value replaced, otherwise 1
    public synchronized int update(String productName, int quantity, String action) {
      Integer current_inventory_value = productInventoryMap.get(productName);
      if (current_inventory_value == null) //product does not exist
        return -1;
      Integer new_quantity = null;
      if (action.equals("purchase")) {
        if (quantity > current_inventory_value) //items not available
          return 0;
        else
          new_quantity = current_inventory_value - quantity;
      } else if (action.equals("cancel"))
        new_quantity = current_inventory_value + quantity;
      System.out.println("updating " + productName + " inventory");
      productInventoryMap.replace(productName, new_quantity);
      notifyAll();
      return 1;
    }

    public synchronized String list() {
      String mapAsString = productInventoryMap.entrySet().stream()
          .map(entry -> toString(entry))
          .collect(Collectors.joining(System.getProperty("line.separator")));
      System.out.println(mapAsString);
      return mapAsString;
    }

    public final String toString(Entry entry) {
      return entry.getKey() + " " + entry.getValue();
    }

    public synchronized void clear() {
      productInventoryMap.clear();
    }
  }

  class Orders {

    class Order {

      public String userName;
      public Integer orderId;
      public String productName;
      public Integer quantity;

      public Order(String userName, Integer orderId, String productName, Integer quantity) {
        this.userName = userName;
        this.orderId = orderId;
        this.productName = productName;
        this.quantity = quantity;
      }

    }

    List<Order> orderList = new ArrayList<>();

    public synchronized List<Order> search(String userName) {
      System.out.println("Searching " + userName);
      List<Order> searchResult = new ArrayList<>();
      for (Order order : orderList)
        if (userName.equalsIgnoreCase(order.userName))
          searchResult.add(order);
      return searchResult;
    }

    public synchronized boolean insert(int orderId, String userName, String productName,
        Integer quantity) {
      System.out.println("Inserting " + orderId);
      orderList.add(new Order(userName, orderId, productName, quantity));
      notifyAll();
      return true;
    }

    public synchronized Order cancel(int orderId) {
      System.out.println("Finding " + orderId);
      for (Order order : orderList) {
        if (orderId == order.orderId) {
          Order returnOrder = order;
          orderList.remove(order);
          return returnOrder;
        }
      }
      System.out.println(orderId + " not found, no such order");
      return null;
    }

}

