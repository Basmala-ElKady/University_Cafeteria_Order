
import java.util.List;

public class MenuItem implements IMenuItem {
    private String id;
    private String name;
    private double price;
    private int quantity;

    public MenuItem(String id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }



    @Override
    public String toString() {
        return name + " - $" + price + " x" + quantity;
    }

    @Override
    public IMenuItem addItem(IMenuItem item) {
        throw new UnsupportedOperationException("Unimplemented method 'addItem'");
    }

    @Override
    public IMenuItem removeItem(String itemId) {
        throw new UnsupportedOperationException("Unimplemented method 'removeItem'");
    }

    @Override
    public IMenuItem updateItem(String itemId, IMenuItem updated) {
        throw new UnsupportedOperationException("Unimplemented method 'updateItem'");
    }

    @Override
    public List<IMenuItem> listAllItems() {
        throw new UnsupportedOperationException("Unimplemented method 'listAllItems'");
    }

    @Override
    public MenuItem getItem() {
        return this;
    }

    @Override
    public IMenuItem addItem(MenuItem m1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addItem'");
    }
}
