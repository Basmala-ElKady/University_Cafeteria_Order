

import java.util.List;

public interface IOrderFulfillment {
 List<IOrder> getPendingOrders();
 boolean updateOrderStatus(String orderId);
}
