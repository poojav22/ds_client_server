package Server;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ProductTable
{

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
    if(current_inventory_value == null) //product does not exist
      return -1;
    Integer new_quantity = null;
    if(action.equals("purchase")) {
      if (quantity > current_inventory_value) //items not available
        return 0;
      else
        new_quantity = current_inventory_value - quantity;
    } else if(action.equals("cancel"))
      new_quantity = current_inventory_value + quantity;
    System.out.println("updating " + productName +" inventory");
    productInventoryMap.replace(productName, new_quantity);
    notifyAll();
    return 1;
  }

  public synchronized String list(){
    String mapAsString = productInventoryMap.entrySet().stream()
        .map(Entry::toString)
    .collect(Collectors.joining(System.getProperty("line.separator")));
    System.out.println(mapAsString);
    return mapAsString;
  }

  public synchronized void clear() {
    productInventoryMap.clear();
  }
}

