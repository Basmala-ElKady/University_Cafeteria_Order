

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Logger;

public class OrderProcessorController
        implements IOrderPlacement, IOrderCancellation, IOrderDiscount, IOrderNotification, IOrderPrioritization {

    private static final Logger logger = Logger.getLogger(OrderProcessorController.class.getName());
    private final IOrderRepository orderRepository;
    private final INotificationService notifier;
    private final ILoyaltyRule loyaltyProgram;
    private final IQLearning qLearning;

    // NEW CODE: order ID generator
    private static int orderCounter = 1;

    private static synchronized String generateOrderId() {
        return "O" + (orderCounter++);
    }

    public OrderProcessorController(IOrderRepository orderRepository,
            BasicLoyaltyRuleController loyaltyRule,
            INotificationService notifier,
            IQLearning qLearning) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.loyaltyProgram = Objects.requireNonNull(loyaltyRule);
        this.notifier = Objects.requireNonNull(notifier);
        this.qLearning = Objects.requireNonNull(qLearning);
    }

    // Place an order and calculate loyalty points
    @Override
    public IOrder placeOrder(IStudentAuthentication student, List<IMenuItem> items, StudentManagement studentCtrl) {
        // NEW CODE: convert to OrderLineItems with quantity=1 for now
        List<OrderLineItem> lineItems = new ArrayList<>();
        for (IMenuItem i : items) {
            lineItems.add(new OrderLineItem(i, 1));
        }

        String orderId = generateOrderId(); // NEW CODE
        IOrder order = new OrderController(orderId, lineItems);

        // Show summary before saving
        System.out.println("Your Order:");
        for (OrderLineItem li : lineItems) {
            System.out.println(li.toString() + " = $" + li.getTotal());
        }
        System.out.println("Total: $" + order.getTotalPrice());
        System.out.print("Confirm order? (y/n): ");

        Scanner sc = new Scanner(System.in);
        String confirm = sc.nextLine();
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Order cancelled.");
            return null;
        }

        // Save and apply loyalty
        orderRepository.save(order);
        double points = loyaltyProgram.calculatePoints(order.getTotalPrice());
        studentCtrl.addPoints(student.getId(), (int) points);

        int studentId = student.getId();
        String studentName = studentCtrl.getStudentName(studentId);

        logger.info("Placed order " + order.getOrderId() + " for student "
                + studentName + ", awarded " + points + " points.");

        System.out.println("Order placed successfully! Order ID: " + order.getOrderId());
        System.out.println("Points earned: " + (int) points);
        return order;
    }

    // Cancel an order before preparation
    @Override
    public boolean cancelOrder(String orderId) {
        Optional<IOrder> opt = orderRepository.findById(orderId);
        if (opt.isEmpty())
            return false;
        IOrder order = opt.get();
        if (!"PENDING".equals(order.getStatus().name()))
            return false;
        orderRepository.deleteById(orderId);
        logger.info("Cancelled order " + orderId);
        return true;
    }

    // Apply a discount to an order
    @Override
    public void applyDiscount(IOrder order, double egp) {
        if (egp <= 0)
            return;
        double newTotal = Math.max(0.0, order.getTotalPrice() - egp);
        try {
            var totalField = OrderController.class.getDeclaredField("totalPrice"); // Field name must match
            totalField.setAccessible(true);
            totalField.set(order, newTotal);
            logger.info("Applied discount " + egp + " EGP on order " + order.getOrderId());
        } catch (ReflectiveOperationException e) {
            logger.warning("Failed to apply discount: " + e.getMessage());
        }
    }

    // Notify when an order is ready
    @Override
    public void notifyReady(IOrder order) {
        notifier.notifyOrderReady(order);
        logger.info("Notification sent for order " + order.getOrderId());
    }

    // Prioritize orders using Q-learning
    @Override
    public List<String> prioritizeOrders(List<IOrder> orders) {
        String state = qLearning.encodeState(orders);
        Action action = qLearning.chooseAction(state);
        logger.info("Chosen action: " + action);
        return qLearning.prioritize(orders, action);
    }

    @Override
    public IOrder placeOrder(IStudentAuthentication student, List<IMenuItem> items, IPointsAdder pointsAdder) {
        // TODO: Implement this method if needed
        throw new UnsupportedOperationException("Unimplemented method 'placeOrder'");
    }
}