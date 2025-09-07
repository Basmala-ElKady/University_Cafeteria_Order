

public interface IOrderCancellationHandler {
    void handleCancelledOrder(int orderId);

    int calculatePoints(double totalPrice);
}
