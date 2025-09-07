// NEW CODE: OrderLineItem class to handle quantity
public class OrderLineItem {
    private final IMenuItem item;
    private final int quantity;

    public OrderLineItem(IMenuItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public IMenuItem getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotal() {
        return item.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return quantity + "x " + item.getName() + " ($" + item.getPrice() + " each)";
    }
}
