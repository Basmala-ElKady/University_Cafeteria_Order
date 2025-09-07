

import java.util.List;



public interface IMenuItem {
    IMenuItem addItem(IMenuItem item);
    IMenuItem removeItem(String itemId);
    IMenuItem updateItem(String itemId, IMenuItem updated);
    List<IMenuItem> listAllItems();
    MenuItem getItem();
    int getQuantity();
    double getPrice();
    String getId();
    String getName();
    IMenuItem addItem(MenuItem m1);
}
