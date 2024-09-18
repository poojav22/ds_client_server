import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ProductService {

  final String INVALID_ITEM_COUNT = "Invalid item quantity provided. cannot order negative items.";
  final String ITEMS_UNAVAILABLE = "Not Available - Not enough items";
  final String INVALID_PRODUCT = "Not Available - We do not sell this product";
  final String PURCHASE_SUCCESS = "You order has been placed, %d %s %s %d";
  final String CANCEL_SUCCESS = "Order %d is canceled";
  final String SEARCH_EMPTY = "No order found for %s";
  final String ORDER_NOT_EXISTS = "Order Id not found. Order does not exist.";
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
      return ORDER_NOT_EXISTS;
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
    StringBuffer orderResults = new StringBuffer();
    List<Orders.Order> searchOrders = orderList.search(userName);
    if (searchOrders.isEmpty()) {
      return String.format(SEARCH_EMPTY, userName);
    } else {
      return searchOrders.stream().map(o -> mapOrder(o))
          .collect(Collectors.joining(System.getProperty("line.separator")));
    }
  }

  private String mapOrder(Orders.Order order) {
    return String.format("%d %s %d", order.orderId, order.productName, order.quantity);
  }

  public String listProducts() {
    return table.list();

  }

}
