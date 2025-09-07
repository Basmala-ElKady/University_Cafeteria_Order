

import java.util.List;
import java.util.Optional;

public interface IOrderHandle {
    Optional<IOrder> findOrderById(String id);
    List<IOrder> getAllOrders();
    void addOrder(IOrder order);
    boolean removeOrderById(String id);
}
