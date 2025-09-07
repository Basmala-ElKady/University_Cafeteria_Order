

import java.time.LocalDateTime;
import java.util.List;



public interface IOrder {
    double calculateTotal();
    OrderStatus getStatus();
    String toString();
    double getTotalPrice();
    String getOrderId();
    LocalDateTime getCreatedAt(); 
    List<IMenuItem> getItems();
    void setStatus(OrderStatus status);
    default void nextState() {
        switch (getStatus()) {
            case PENDING:
                setStatus(OrderStatus.PREPARING);
                break;
            case PREPARING:
                setStatus(OrderStatus.READY_FOR_PICKUP);
                break;
            case READY_FOR_PICKUP:
                break;
        }

}
}
