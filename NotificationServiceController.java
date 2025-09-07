
public class NotificationServiceController implements INotificationService,
        IOrderReadyNotifier, IPointsUpdateNotifier, IOrderCancellationNotifier {

    private IQLearning qLearningEngine;

    public NotificationServiceController(IQLearning qLearningEngine) {
        this.qLearningEngine = qLearningEngine;
    }

    @Override
    public void sendNotification(String message) {
        System.out.println("Notification: " + message);
    }

    @Override
    public void notifyOrderReady(IOrder order) {
        String state = "ORDER_READY_" + order.getOrderId();
        Action action = qLearningEngine.chooseAction(state);
        if (action == Action.PRIORITIZE_PREPARING_FIRST) {
            sendNotification("Order " + order.getOrderId() + " is ready for pickup.");
        }
        qLearningEngine.update(state, action, 1.0, state);
    }

    @Override
    public void notifyPointsUpdated(int studentId, int points) {
        String state = "STUDENT_POINTS_" + studentId;
        Action action = qLearningEngine.chooseAction(state);
        if (action == Action.PRIORITIZE_LARGEST_TOTAL) {
            sendNotification("Student " + studentId + " now has " + points + " points.");
        }
        qLearningEngine.update(state, action, points, state);
    }

    @Override
    public void notifyOrderCancelled(int orderId) {
        String state = "ORDER_CANCELLED_" + orderId;
        Action action = qLearningEngine.chooseAction(state);
        if (action == Action.PRIORITIZE_OLDEST_PENDING) {
            sendNotification("Order " + orderId + " has been cancelled.");
        }
        qLearningEngine.update(state, action, 0.0, state);
    }

    @Override
    public void notifyOrderReady(int orderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifyOrderReady'");
    }

    public void notifyPointsUpdated(OrderController currentStudent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifyPointsUpdated'");
    }

    public void notifyOrderReady(OrderController currentStudent, OrderController order) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifyOrderReady'");
    }

    @Override
    public void notifyPointsUpdated(int studentId, double points) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifyPointsUpdated'");
    }
}