package Server;

import java.util.ArrayList;
import java.util.List;

public class Orders {
   class Order {
    public String userName;
    public Integer orderId;
    public String productName;
    public Integer quantity;

    public Order(String userName, Integer orderId, String productName, Integer quantity ) {
      this.userName = userName;
      this.orderId = orderId;
      this.productName = productName;
      this.quantity= quantity;
    }

  }

  List<Order> orderList = new ArrayList<>();
  public synchronized List<Order> search(String userName) {
    System.out.println("Searching " + userName);
    List<Order> searchResult = new ArrayList<>();
    for (Order order: orderList)
      if (userName.equals(order.userName)) searchResult.add(order);
    return searchResult;
  }

  public synchronized boolean insert(int orderId, String userName, String productName, Integer quantity) {
    System.out.println("Inserting " + orderId);
    orderList.add(new Order(userName, orderId, productName, quantity));
    notifyAll();
    return true;
  }
  public synchronized Order cancel(int orderId) {
    System.out.println("Finding " + orderId);
    for (Order order: orderList) {
      if (orderId == order.orderId) {
        Order returnOrder = order;
        orderList.remove(order);
        return returnOrder;
      }
    }
    System.out.println(orderId + " not found, no such order");
    return null;
  }

  public synchronized void clear() {
    orderList.clear();
  }

}

