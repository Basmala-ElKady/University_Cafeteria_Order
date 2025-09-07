
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class StaffViewController implements IStaffView {
    private List<OrderController> orders;

    public StaffViewController(List<OrderController> orders) {
        this.orders = orders;
    }

    @Override
    public void showMenu() {
        System.out.println("=== Staff Menu ===");
        System.out.println("1. View Pending Orders");
        System.out.println("2. Update Order Status");
    }

    @Override
    public void viewPendingOrders() {
        for (OrderController order : orders) {
            if (order.getStatus() == OrderStatus.PENDING) {
                System.out.println(order);
            }
        }
    }

    @Override
    public void updateOrderStatus(Scanner sc) {
        System.out.print("Enter Order ID to update: ");
        int id = sc.nextInt();
        for (OrderController order : orders) {
            if (order.toString().contains("orderId=" + id)) {
                System.out.println("Choose new status (1=PREPARING, 2=READY_FOR_PICKUP): ");
                int choice = sc.nextInt();
                switch (choice) {
                    case 1 -> order.setStatus(OrderStatus.PREPARING);
                    case 2 -> order.setStatus(OrderStatus.READY_FOR_PICKUP);
                    default -> System.out.println("Invalid choice");
                }
                System.out.println("Updated: " + order);
                return;
            }
        }
        System.out.println("Order not found.");
    }

    public void addStudent(Student s2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addStudent'");
    }

    public boolean login(String email, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    public Collection<OrderController> getAllStudents() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllStudents'");
    }
}
