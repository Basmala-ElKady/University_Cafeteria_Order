import java.sql.*;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

enum UserType {
    STUDENT,
    STAFF
}

public class Main {
    private static StudentManagement studentManagement;
    private static StaffManagement staffManagement;
    private static MenuManagerController menuManager;
    @SuppressWarnings("unused")
    private static OrderProcessorController orderProcessor;
    @SuppressWarnings("unused")
    private static OrderFulfillmentController orderFulfillment;
    @SuppressWarnings("unused")
    private static LoyaltyProgramController loyaltyProgram;
    private static NotificationServiceController notificationService;
    private static QLearningController qLearning;
    private static Connection connection;
    private static final DecimalFormat df = new DecimalFormat("#.##");
    public static CafeteriaUserType currentUserType;
    public static String currentUserEmail;

    public static void main(String[] args) {
        initializeSystem();
        listAllUsers();
        runConsoleApp();
    }

    public static void initializeSystem() {
        try {
            connection = DatabaseConnection.getConnection();

            // Verify database and tables
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs = meta.getTables(null, null, "Staff", new String[]{"TABLE"});
            if (!rs.next()) {
                System.err.println("Staff table not found. Please run the cafeteria_system.sql script.");
                System.exit(1);
            }
            rs.close();

            studentManagement = new StudentManagement();
            staffManagement = new StaffManagement();
            menuManager = new MenuManagerController();
            qLearning = new QLearningController(0.1, 0.9, 0.1);
            notificationService = new NotificationServiceController(qLearning);
            BasicLoyaltyRuleController loyaltyRule = new BasicLoyaltyRuleController(0.1);
            loyaltyProgram = new LoyaltyProgramController(0, loyaltyRule, notificationService, qLearning, studentManagement);
            OrderRepositoryController orderRepository = new OrderRepositoryController();
            orderProcessor = new OrderProcessorController(orderRepository, loyaltyRule, notificationService, qLearning);
            orderFulfillment = new OrderFulfillmentController(orderRepository, notificationService);

            loadUsersFromDB();
            loadMenuItemsFromDB();

        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void loadUsersFromDB() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            // Load Students
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Students");
            if (rs.next() && rs.getInt(1) == 0) {
                String[] names = { "Alice Johnson", "Bob Smith", "Charlie Brown" };
                String[] emails = { "alice@example.com", "bob@example.com", "charlie@example.com" };
                String[] passwords = { "alicepass", "bobpass", "charliepass" };
                int[] points = { 50, 30, 20 };

                for (int i = 0; i < names.length; i++) {
                    PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM Students WHERE email = ?");
                    checkStmt.setString(1, emails[i]);
                    ResultSet checkRs = checkStmt.executeQuery();

                    if (checkRs.next() && checkRs.getInt(1) == 0) {
                        Student s = new Student(names[i], emails[i], passwords[i], points[i]);
                        studentManagement.addStudent(s);
                    }

                    checkRs.close();
                    checkStmt.close();
                }
            }

            // Load Staff
            rs = stmt.executeQuery("SELECT COUNT(*) FROM Staff");
            if (rs.next() && rs.getInt(1) == 0) {
                String[] staffNames = { "Cafeteria Staff" };
                String[] staffEmails = { "staff@cafeteria.com" };
                String[] staffPasswords = { "staffpass" };

                for (int i = 0; i < staffNames.length; i++) {
                    PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM Staff WHERE email = ?");
                    checkStmt.setString(1, staffEmails[i]);
                    ResultSet checkRs = checkStmt.executeQuery();

                    if (checkRs.next() && checkRs.getInt(1) == 0) {
                        Staff s = new Staff(staffNames[i], staffEmails[i], staffPasswords[i]);
                        staffManagement.addStaff(s);
                    }

                    checkRs.close();
                    checkStmt.close();
                }
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Load users error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadMenuItemsFromDB() throws SQLException {
        String query = "SELECT * FROM menu_items";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                MenuItem item = new MenuItem(String.valueOf(rs.getInt("item_id")), rs.getString("name"), rs.getDouble("price"), 1);
                menuManager.addItem(item);
            }
        }
    }

    public static void listAllUsers() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            // Check if tables exist
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rsTables = meta.getTables(null, null, "Students", new String[]{"TABLE"});
            if (!rsTables.next()) {
                System.err.println("Students table not found.");
                rsTables.close();
                conn.close();
                return;
            }
            rsTables.close();
            rsTables = meta.getTables(null, null, "Staff", new String[]{"TABLE"});
            if (!rsTables.next()) {
                System.err.println("Staff table not found.");
                rsTables.close();
                conn.close();
                return;
            }
            rsTables.close();

            // List Students
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT email, name, loyalty_points FROM Students");
            System.out.println("\n=== All Students in Database ===");
            while (rs.next()) {
                System.out.println("Email: " + rs.getString("email") + " | Name: " + rs.getString("name") + " | Points: " + rs.getInt("loyalty_points"));
            }
            rs.close();

            // List Staff
            rs = stmt.executeQuery("SELECT email, name FROM Staff");
            System.out.println("\n=== All Staff in Database ===");
            while (rs.next()) {
                System.out.println("Email: " + rs.getString("email") + " | Name: " + rs.getString("name"));
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error listing users: " + e.getMessage());
        }
    }

    private static void runConsoleApp() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== University Cafeteria System ===");
            if (currentUserType == null) {
                System.out.println("1. Student Login");
                System.out.println("2. Staff Login");
                System.out.println("3. Register Student or Staff");
                System.out.println("4. Exit");
            } else if (currentUserType == CafeteriaUserType.STUDENT) {
                System.out.println("1. Log Out");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order");
                System.out.println("5. View Loyalty Points");
                System.out.println("6. Redeem Points");
                System.out.println("10. Exit");
            } else if (currentUserType == CafeteriaUserType.STAFF) {
                System.out.println("1. Log Out");
                System.out.println("7. View Pending Orders");
                System.out.println("8. Update Order Status");
                System.out.println("9. View Daily/Weekly Sales Report");
                System.out.println("10. Exit");
            }
            System.out.print("Choose an option: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                if (currentUserType == null) {
                    switch (choice) {
                        case 1 -> login(scanner, CafeteriaUserType.STUDENT);
                        case 2 -> login(scanner, CafeteriaUserType.STAFF);
                        case 3 -> registerUser(scanner);
                        case 4 -> {
                            try {
                                connection.close();
                            } catch (SQLException e) {
                                System.err.println("Error closing database: " + e.getMessage());
                            }
                            System.out.println("Exiting...");
                            scanner.close();
                            System.exit(0);
                        }
                        default -> System.out.println("Invalid option. Please try again.");
                    }
                } else if (currentUserType == CafeteriaUserType.STUDENT) {
                    switch (choice) {
                        case 1 -> logOut();
                        case 3 -> viewMenu();
                        case 4 -> placeOrder(scanner);
                        case 5 -> viewLoyaltyPoints();
                        case 6 -> redeemPoints(scanner);
                        case 10 -> {
                            try {
                                connection.close();
                            } catch (SQLException e) {
                                System.err.println("Error closing database: " + e.getMessage());
                            }
                            System.out.println("Exiting...");
                            scanner.close();
                            System.exit(0);
                        }
                        default -> System.out.println("Invalid option. Please try again.");
                    }
                } else if (currentUserType == CafeteriaUserType.STAFF) {
                    switch (choice) {
                        case 1 -> logOut();
                        case 7 -> viewPendingOrders();
                        case 8 -> updateOrderStatus(scanner);
                        case 9 -> generateSalesReport(scanner);
                        case 10 -> {
                            try {
                                connection.close();
                            } catch (SQLException e) {
                                System.err.println("Error closing database: " + e.getMessage());
                            }
                            System.out.println("Exiting...");
                            scanner.close();
                            System.exit(0);
                        }
                        default -> System.out.println("Invalid option. Please try again.");
                    }
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }
    }

    public static void login(Scanner scanner, CafeteriaUserType userType) {
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (userType == CafeteriaUserType.STUDENT) {
            Student student = studentManagement.login(email, password);
            if (student != null) {
                if (email.endsWith("@cafeteria.com")) {
                    System.out.println("Invalid credentials or this is not a student account.");
                    return;
                }
                currentUserEmail = email;
                currentUserType = CafeteriaUserType.STUDENT;
                System.out.println("Login successful! Welcome, " + student.getName() + " (User Type: STUDENT)");
                loyaltyProgram = new LoyaltyProgramController(student.getId(), new BasicLoyaltyRuleController(0.1), notificationService, qLearning, studentManagement);
            } else {
                System.out.println("Invalid credentials.");
            }
        } else {
            Staff staff = staffManagement.login(email, password);
            if (staff != null) {
                if (!email.endsWith("@cafeteria.com")) {
                    System.out.println("Invalid credentials or this is not a staff account.");
                    return;
                }
                currentUserEmail = email;
                currentUserType = CafeteriaUserType.STAFF;
                System.out.println("Login successful! Welcome, " + staff.getName() + " (User Type: STAFF)");
            } else {
                System.out.println("Invalid credentials.");
            }
        }
    }

    public static void logOut() {
        if (currentUserType == CafeteriaUserType.STUDENT) {
            studentManagement.logOut();
        } else if (currentUserType == CafeteriaUserType.STAFF) {
            staffManagement.logout();
        }
        currentUserType = null;
        currentUserEmail = null;
        System.out.println("Logged out successfully.");
    }

    public static void registerUser(Scanner scanner) {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Register as (1. Student, 2. Staff): ");
        int userTypeChoice;
        try {
            userTypeChoice = scanner.nextInt();
            scanner.nextLine();
            if (userTypeChoice != 1 && userTypeChoice != 2) {
                System.out.println("Invalid input. Please enter 1 for Student or 2 for Staff.");
                return;
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter 1 for Student or 2 for Staff.");
            scanner.nextLine();
            return;
        }

        if (userTypeChoice == 2 && !email.endsWith("@cafeteria.com")) {
            System.out.println("Staff email must end with '@cafeteria.com'.");
            return;
        } else if (userTypeChoice == 1 && email.endsWith("@cafeteria.com")) {
            System.out.println("Student email cannot end with '@cafeteria.com'.");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            String table = userTypeChoice == 1 ? "Students" : "Staff";
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM " + table + " WHERE email = ?");
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Email already exists. Please use a different email.");
                rs.close();
                checkStmt.close();
                conn.close();
                return;
            }
            rs.close();
            checkStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error checking email: " + e.getMessage());
            return;
        }

        boolean success;
        if (userTypeChoice == 1) {
            Student student = new Student(name, email, password, 0);
            success = studentManagement.addStudent(student);
        } else {
            Staff staff = new Staff(name, email, password);
            success = staffManagement.addStaff(staff);
        }

        if (success) {
            System.out.println((userTypeChoice == 1 ? "Student" : "Staff") + " registered successfully!");
        } else {
            System.out.println("Registration failed due to an error. Please try again.");
        }
    }

    public static void viewMenu() {
        System.out.println("\n=== Menu ===");
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT item_id, name, price FROM menu_items WHERE available = TRUE")) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("item_id") + " | " + rs.getString("name") + " | $" + df.format(rs.getDouble("price")));
            }
        } catch (SQLException e) {
            System.err.println("Error displaying menu: " + e.getMessage());
        }
    }

    public static void placeOrder(Scanner scanner) {
        if (currentUserType != CafeteriaUserType.STUDENT || studentManagement.getId() == 0) {
            System.out.println("Please login as a student first.");
            return;
        }

        List<MenuItem> items = new ArrayList<>();
        boolean ordering = true;

        while (ordering) {
            viewMenu();

            System.out.print("Enter item ID (0 to finish): ");
            try {
                int itemId = scanner.nextInt();
                scanner.nextLine();

                if (itemId == 0)
                    break;

                try (PreparedStatement ps = connection.prepareStatement("SELECT name, price FROM menu_items WHERE item_id = ? AND available = TRUE")) {
                    ps.setInt(1, itemId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        String name = rs.getString("name");
                        double price = rs.getDouble("price");

                        System.out.print("Enter quantity: ");
                        int quantity = scanner.nextInt();
                        scanner.nextLine();

                        if (quantity <= 0) {
                            System.out.println("Quantity must be positive.");
                            continue;
                        }

                        items.add(new MenuItem(String.valueOf(itemId), name, price, quantity));
                    } else {
                        System.out.println("Item not found or unavailable.");
                    }
                } catch (SQLException e) {
                    System.err.println("Error fetching item: " + e.getMessage());
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid item ID.");
                scanner.nextLine();
            }

            while (true) {
                System.out.print("Do you want to add another item? (y/n): ");
                String more = scanner.nextLine().trim().toLowerCase();
                if (more.equals("y")) {
                    break;
                } else if (more.equals("n")) {
                    ordering = false;
                    break;
                } else {
                    System.out.println("Invalid input. Please enter 'y' or 'n'.");
                }
            }
        }

        if (items.isEmpty()) {
            System.out.println("No items selected.");
            return;
        }

        double total = 0;
        for (MenuItem m : items) {
            total += m.getPrice() * m.getQuantity();
        }

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO Orders(student_id, total_amount) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, studentManagement.getId());
            ps.setDouble(2, total);
            ps.executeUpdate();

            ResultSet rsKeys = ps.getGeneratedKeys();
            int orderId = 0;
            if (rsKeys.next()) {
                orderId = rsKeys.getInt(1);
            }

            for (MenuItem m : items) {
                try (PreparedStatement psItem = connection.prepareStatement("INSERT INTO OrderDetails(order_id, item_id, quantity) VALUES (?, ?, ?)")) {
                    psItem.setInt(1, orderId);
                    psItem.setInt(2, Integer.parseInt(m.getId()));
                    psItem.setInt(3, m.getQuantity());
                    psItem.executeUpdate();
                }
            }

            int pointsEarned = (int) (total / 10);
            studentManagement.addLoyaltyPoints(studentManagement.getId(), pointsEarned);

            System.out.println("Notification: Student " + studentManagement.getLoggedInStudent().getName() + " earned " + pointsEarned + " points.");
            System.out.println("Order placed successfully! Order ID: " + orderId);
            System.out.println("Total Price: $" + df.format(total));
            System.out.println("You earned " + pointsEarned + " points.");
            System.out.println("Your total loyalty points: " + (int) studentManagement.getLoggedInStudent().getLoyaltyPoints());

        } catch (SQLException e) {
            System.err.println("Error placing order: " + e.getMessage());
        }
    }

    public static void viewLoyaltyPoints() {
        if (currentUserType != CafeteriaUserType.STUDENT || studentManagement.getId() == 0) {
            System.out.println("Please login as a student first.");
            return;
        }
        Student student = studentManagement.getLoggedInStudent();
        if (student != null) {
            int points = (int) student.getLoyaltyPoints();
            System.out.println("Your current loyalty points: " + points);
        } else {
            System.out.println("Your current loyalty points: 0");
        }
    }

    public static void redeemPoints(Scanner scanner) {
        if (currentUserType != CafeteriaUserType.STUDENT || studentManagement.getId() == 0) {
            System.out.println("Please login as a student first.");
            return;
        }

        Student student = studentManagement.getLoggedInStudent();
        int currentPoints = (int) student.getLoyaltyPoints();
        System.out.println("Your current loyalty points: " + currentPoints);

        if (currentPoints < 10) {
            System.out.println("You need at least 10 points to redeem. You have " + currentPoints + " points.");
            return;
        }

        System.out.print("Enter points to redeem (minimum 10): ");
        try {
            int pointsToRedeem = scanner.nextInt();
            scanner.nextLine();

            if (pointsToRedeem < 10) {
                System.out.println("Minimum redemption is 10 points.");
                return;
            }

            if (currentPoints < pointsToRedeem) {
                System.out.println("Not enough points! You have " + currentPoints + " points.");
                return;
            }

            studentManagement.redeemLoyaltyPoints(studentManagement.getId(), pointsToRedeem);
            System.out.println("You redeemed " + pointsToRedeem + " points.");
            System.out.println("Notification: Student " + student.getName() + " redeemed " + pointsToRedeem + " points.");
            System.out.println("Deducted " + pointsToRedeem + " points. Remaining points: " + (int) student.getLoyaltyPoints() + ".");

            double dollarValue = pointsToRedeem * 0.2; // 1 point = $0.2

            System.out.println("\n=== Menu (Available for $" + df.format(dollarValue) + ") ===");
            List<MenuItem> availableItems = new ArrayList<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT item_id, name, price FROM menu_items WHERE available = TRUE")) {
                while (rs.next()) {
                    MenuItem item = new MenuItem(
                        String.valueOf(rs.getInt("item_id")),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        1
                    );
                    availableItems.add(item);
                    System.out.println("ID: " + rs.getInt("item_id") + " | " + rs.getString("name") + " | $" + df.format(rs.getDouble("price")));
                }
            } catch (SQLException e) {
                System.err.println("Error displaying menu: " + e.getMessage());
                studentManagement.addLoyaltyPoints(studentManagement.getId(), pointsToRedeem);
                System.out.println("Returned " + pointsToRedeem + " points due to error.");
                return;
            }

            List<MenuItem> selectedItems = new ArrayList<>();
            double totalCost = 0.0;
            boolean ordering = true;

            while (ordering) {
                System.out.print("Enter item ID (0 to finish): ");
                try {
                    int itemId = scanner.nextInt();
                    scanner.nextLine();

                    if (itemId == 0) {
                        break;
                    }

                    MenuItem selectedItem = null;
                    for (MenuItem item : availableItems) {
                        if (Integer.parseInt(item.getId()) == itemId) {
                            selectedItem = item;
                            break;
                        }
                    }

                    if (selectedItem == null) {
                        System.out.println("Item not found or unavailable.");
                        continue;
                    }

                    System.out.print("Enter quantity: ");
                    int quantity = scanner.nextInt();
                    scanner.nextLine();

                    if (quantity <= 0) {
                        System.out.println("Quantity must be positive.");
                        continue;
                    }

                    double itemTotal = selectedItem.getPrice() * quantity;
                    selectedItems.add(new MenuItem(selectedItem.getId(), selectedItem.getName(), selectedItem.getPrice(), quantity));
                    totalCost += itemTotal;

                    while (true) {
                        System.out.print("Do you want to add another item? (y/n): ");
                        String more = scanner.nextLine().trim().toLowerCase();
                        if (more.equals("y")) {
                            break;
                        } else if (more.equals("n")) {
                            ordering = false;
                            break;
                        } else {
                            System.out.println("Invalid input. Please enter 'y' or 'n'.");
                        }
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a valid item ID or quantity.");
                    scanner.nextLine();
                }
            }

            if (selectedItems.isEmpty()) {
                System.out.println("No items selected. Redemption cancelled.");
                studentManagement.addLoyaltyPoints(studentManagement.getId(), pointsToRedeem);
                System.out.println("Returned " + pointsToRedeem + " points to your account.");
                return;
            }

            if (totalCost < 10.0) {
                System.out.println("You can't buy anything less than $10.");
                studentManagement.addLoyaltyPoints(studentManagement.getId(), pointsToRedeem);
                System.out.println("Returned " + pointsToRedeem + " points to your account.");
                return;
            }

            int pointsUsed = Math.min(pointsToRedeem, (int) Math.ceil(totalCost / 0.2));
            double cashPaid = totalCost - (pointsUsed * 0.2);
            int pointsToReturn = pointsToRedeem - pointsUsed;

            if (pointsToReturn > 0) {
                studentManagement.addLoyaltyPoints(studentManagement.getId(), pointsToReturn);
                System.out.println("Returned " + pointsToReturn + " unused points to your account.");
            }

            double totalPointsValue = currentPoints * 0.2;
            if (totalCost > totalPointsValue) {
                System.out.println("Order total: $" + df.format(totalCost) + ". Your redeemed points cover $" + df.format(pointsUsed * 0.2) + ". You need to pay $" + df.format(cashPaid) + " extra in cash.");
                System.out.println("Please choose an option:");
                System.out.println("1. Proceed with redemption and pay $" + df.format(cashPaid) + " in cash.");
                System.out.println("2. Cancel redemption and pay the full $" + df.format(totalCost) + " in cash.");
                System.out.println("3. Adjust the order to fit within $" + df.format(dollarValue) + ".");
                System.out.println("4. Cancel the order");
                System.out.print("Enter option (1-4): ");

                try {
                    int option = scanner.nextInt();
                    scanner.nextLine();
                    switch (option) {
                        case 1:
                            System.out.println("Proceeding with redemption. You will pay $" + df.format(cashPaid) + " in cash.");
                            break;
                        case 2:
                            System.out.println("Redemption cancelled. Placing order with full payment in cash.");
                            studentManagement.addLoyaltyPoints(studentManagement.getId(), pointsToRedeem);
                            System.out.println("Returned " + pointsToRedeem + " points to your account.");
                            placeOrderWithCash(scanner, selectedItems, totalCost);
                            return;
                        case 3:
                            System.out.println("Please adjust the order to fit within $" + df.format(dollarValue) + ".");
                            studentManagement.addLoyaltyPoints(studentManagement.getId(), pointsToRedeem);
                            System.out.println("Returned " + pointsToRedeem + " points to your account.");
                            return;
                        case 4:
                            System.out.println("Order cancelled.");
                            studentManagement.addLoyaltyPoints(studentManagement.getId(), pointsToRedeem);
                            System.out.println("Returned " + pointsToRedeem + " points to your account.");
                            return;
                        default:
                            System.out.println("Invalid option. Redemption cancelled.");
                            studentManagement.addLoyaltyPoints(studentManagement.getId(), pointsToRedeem);
                            System.out.println("Returned " + pointsToRedeem + " points to your account.");
                            return;
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Redemption cancelled.");
                    studentManagement.addLoyaltyPoints(studentManagement.getId(), pointsToRedeem);
                    System.out.println("Returned " + pointsToRedeem + " points to your account.");
                    scanner.nextLine();
                    return;
                }
            } else {
                if (cashPaid > 0) {
                    System.out.println("Order total: $" + df.format(totalCost) + ". Your redeemed points cover $" + df.format(pointsUsed * 0.2) + ". You will pay $" + df.format(cashPaid) + " in cash.");
                } else {
                    System.out.println("Order total: $" + df.format(totalCost) + ". Your redeemed points cover the full amount.");
                }
                System.out.println("Are you sure you want to place the order? (y/n): ");
                String confirmOrder;
                while (true) {
                    confirmOrder = scanner.nextLine().trim().toLowerCase();
                    if (confirmOrder.equals("y")) {
                        break;
                    } else if (confirmOrder.equals("n")) {
                        System.out.println("Redemption cancelled.");
                        studentManagement.addLoyaltyPoints(studentManagement.getId(), pointsToRedeem);
                        System.out.println("Returned " + pointsToRedeem + " points to your account.");
                        return;
                    } else {
                        System.out.println("Invalid input. Please enter 'y' or 'n'.");
                    }
                }
            }

            try {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Orders(student_id, total_amount, points_used) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setInt(1, studentManagement.getId());
                ps.setDouble(2, totalCost);
                ps.setInt(3, pointsUsed);
                ps.executeUpdate();

                ResultSet rsKeys = ps.getGeneratedKeys();
                int orderId = 0;
                if (rsKeys.next()) {
                    orderId = rsKeys.getInt(1);
                }

                for (MenuItem item : selectedItems) {
                    try (PreparedStatement psItem = connection.prepareStatement(
                        "INSERT INTO OrderDetails(order_id, item_id, quantity) VALUES (?, ?, ?)"
                    )) {
                        psItem.setInt(1, orderId);
                        psItem.setInt(2, Integer.parseInt(item.getId()));
                        psItem.setInt(3, item.getQuantity());
                        psItem.executeUpdate();
                    }
                }

                int pointsEarned = (int) (cashPaid / 10);
                if (pointsEarned > 0) {
                    studentManagement.addLoyaltyPoints(studentManagement.getId(), pointsEarned);
                    System.out.println("You earned " + pointsEarned + " points from this order.");
                }

                System.out.println("Points used for order: " + pointsUsed + ".");
                if (cashPaid > 0) {
                    System.out.println("Additional cash paid: $" + df.format(cashPaid) + ".");
                }
                System.out.println("Order placed successfully! Order ID: " + orderId);
                System.out.println("Total Cost: $" + df.format(totalCost));
                if (pointsEarned == 0) {
                    System.out.println("No points earned as the order was paid with points.");
                }
                System.out.println("Remaining loyalty points: " + (int) studentManagement.getLoggedInStudent().getLoyaltyPoints());

                ps.close();
                rsKeys.close();

            } catch (SQLException e) {
                System.err.println("Error processing redemption: " + e.getMessage());
                studentManagement.addLoyaltyPoints(studentManagement.getId(), pointsUsed);
                System.out.println("Returned " + pointsUsed + " points due to error.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid number of points.");
            scanner.nextLine();
        }
    }

    private static void placeOrderWithCash(Scanner scanner, List<MenuItem> items, double total) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO Orders(student_id, total_amount) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, studentManagement.getId());
            ps.setDouble(2, total);
            ps.executeUpdate();

            ResultSet rsKeys = ps.getGeneratedKeys();
            int orderId = 0;
            if (rsKeys.next()) {
                orderId = rsKeys.getInt(1);
            }

            for (MenuItem m : items) {
                try (PreparedStatement psItem = connection.prepareStatement("INSERT INTO OrderDetails(order_id, item_id, quantity) VALUES (?, ?, ?)")) {
                    psItem.setInt(1, orderId);
                    psItem.setInt(2, Integer.parseInt(m.getId()));
                    psItem.setInt(3, m.getQuantity());
                    psItem.executeUpdate();
                }
            }

            int pointsEarned = (int) (total / 10);
            studentManagement.addLoyaltyPoints(studentManagement.getId(), pointsEarned);

            System.out.println("Notification: Student " + studentManagement.getLoggedInStudent().getName() + " earned " + pointsEarned + " points.");
            System.out.println("Order placed successfully! Order ID: " + orderId);
            System.out.println("Total Price: $" + df.format(total));
            System.out.println("You earned " + pointsEarned + " points.");
            System.out.println("Your total loyalty points: " + (int) studentManagement.getLoggedInStudent().getLoyaltyPoints());

        } catch (SQLException e) {
            System.err.println("Error placing order: " + e.getMessage());
        }
    }

    public static void viewPendingOrders() {
        System.out.println("\n=== Pending Orders ===");
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT order_id, student_id, total_amount, order_date FROM Orders WHERE status = 'PENDING'";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            boolean hasOrders = false;
            while (rs.next()) {
                hasOrders = true;
                int orderId = rs.getInt("order_id");
                int studentId = rs.getInt("student_id");
                double total = rs.getDouble("total_amount");
                String date = rs.getString("order_date");

                String studentQuery = "SELECT name FROM Students WHERE student_id = ?";
                PreparedStatement studentStmt = conn.prepareStatement(studentQuery);
                studentStmt.setInt(1, studentId);
                ResultSet studentRs = studentStmt.executeQuery();
                String studentName = studentRs.next() ? studentRs.getString("name") : "Unknown";

                System.out.println("Order ID: " + orderId + " | Student: " + studentName + " | Total: $" + df.format(total) + " | Date: " + date);

                studentRs.close();
                studentStmt.close();
            }

            if (!hasOrders) {
                System.out.println("No pending orders found.");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error fetching pending orders: " + e.getMessage());
        }
    }

    public static void updateOrderStatus(Scanner scanner) {
        System.out.print("Enter order ID: ");
        try {
            int orderId = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Enter new status (PENDING, PREPARING, READY_FOR_PICKUP, CANCELLED): ");
            String newStatus = scanner.nextLine().trim().toUpperCase();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE Orders SET status = ? WHERE order_id = ?")) {
                ps.setString(1, newStatus);
                ps.setInt(2, orderId);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Order status updated successfully.");
                } else {
                    System.out.println("Order ID not found.");
                }
            } catch (SQLException e) {
                System.err.println("Error updating order status: " + e.getMessage());
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid order ID.");
            scanner.nextLine();
        }
    }

    public static void generateSalesReport(Scanner scanner) {
        System.out.print("Enter report type (1. Daily, 2. Weekly): ");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();

            String query;
            String period;
            LocalDate startDate;
            if (choice == 1) {
                period = "Daily";
                query = "SELECT COUNT(*) as order_count, SUM(total_amount) as total_sales FROM Orders WHERE DATE(order_date) = ?";
                startDate = LocalDate.now();
            } else if (choice == 2) {
                period = "Weekly";
                query = "SELECT COUNT(*) as order_count, SUM(total_amount) as total_sales FROM Orders WHERE order_date >= ? AND order_date < ?";
                startDate = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
            } else {
                System.out.println("Invalid option. Please choose 1 or 2.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                if (choice == 1) {
                    ps.setDate(1, Date.valueOf(startDate));
                } else {
                    ps.setDate(1, Date.valueOf(startDate));
                    ps.setDate(2, Date.valueOf(startDate.plusDays(7)));
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int orderCount = rs.getInt("order_count");
                    double totalSales = rs.getDouble("total_sales");
                    System.out.println("\n=== Sales Report ===");
                    System.out.println("Period: " + (choice == 1 ? startDate : "Week " + startDate.get(WeekFields.ISO.weekOfWeekBasedYear())) + " | Orders: " + orderCount + " | Total Sales: $" + df.format(totalSales));
                } else {
                    System.out.println("No data available for the " + period.toLowerCase() + " report.");
                }
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error generating report: " + e.getMessage());
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine();
        }
    }
}