

import java.util.List;
import java.util.stream.Collectors;


public class OrderFulfillmentController extends OrderHandleController implements IOrderFulfillment {
    private final INotificationService notifier;

    public OrderFulfillmentController(IOrderRepository orderRepository, INotificationService notifier) {
        super(orderRepository);
        this.notifier = notifier;
    }

    @Override

    public List<IOrder> getPendingOrders() {
        return getAllOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .collect(Collectors.toList());
    }

    @Override

    public boolean updateOrderStatus(String orderId) {
        return findOrderById(orderId).map(order -> {
            OrderStatus oldStatus = order.getStatus();

            // تحديث الحالة
            order.nextState();
            orderRepository.save(order);

            // إذا الحالة بعد التحديث أصبحت READY_FOR_PICKUP
            if (oldStatus != OrderStatus.READY_FOR_PICKUP && order.getStatus() == OrderStatus.READY_FOR_PICKUP) {
                notifier.notifyOrderReady(order);
                System.out.println("Your order is ready.");
            } else {
                System.out.println("Your order is not ready. Wait please for a few seconds/minutes.");
            }

            return true;
        }).orElseGet(() -> {
            // لو الأوردر مش موجود
            System.out.println("Your order is not ready. Wait please for a few seconds/minutes.");
            return false;
        });
    }
}