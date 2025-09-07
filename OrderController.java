import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderController implements IOrder {
    private String orderId;
    private List<OrderLineItem> items; // Store line items
    private double totalPrice;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public OrderController(String orderId, List<OrderLineItem> items) {
        this.orderId = orderId;
        this.items = items;
        this.status = OrderStatus.PENDING;
        this.totalPrice = calculateTotal();
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public double calculateTotal() {
        return items.stream()
                .mapToDouble(OrderLineItem::getTotal)
                .sum();
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getOrderId() {
        return orderId;
    }

    // Fix: implement IOrder requirement
    @Override
    public List<IMenuItem> getItems() {
        return items.stream()
                .map(OrderLineItem::getItem)
                .collect(Collectors.toList());
    }

    // Extra method to access line items with quantity
    public List<OrderLineItem> getLineItems() {
        return items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Order #" + orderId +
                " Items=" + items +
                " Total=$" + totalPrice +
                " Status=" + status;
    }

    public Object getEmail() {
        throw new UnsupportedOperationException("Unimplemented method 'getEmail'");
    }

    public String getName() {
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    public String getLoyaltyPoints() {
        throw new UnsupportedOperationException("Unimplemented method 'getLoyaltyPoints'");
    }
}
