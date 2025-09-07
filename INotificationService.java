

public interface INotificationService {
    void sendNotification(String message);
    void notifyOrderReady(IOrder order);
    void notifyPointsUpdated(int studentId, double points);
    void notifyOrderCancelled(int orderId);
    void notifyOrderReady(int orderId);
}
