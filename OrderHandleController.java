

import java.util.*;


public class OrderHandleController implements IOrderHandle {
     protected final IOrderRepository orderRepository;

    public OrderHandleController(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Optional<IOrder> findOrderById(String id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<IOrder> getAllOrders() {
        return orderRepository.getAll();
    }

    @Override
    public void addOrder(IOrder order) {
        orderRepository.save(order);
    }

    @Override
    public boolean removeOrderById(String id) {
        Optional<IOrder> order = orderRepository.findById(id);
        if (order.isPresent()) {
            orderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<IOrder> getPendingOrders() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPendingOrders'");
    }

    public boolean updateOrderStatus(String orderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateOrderStatus'");
    }
}
