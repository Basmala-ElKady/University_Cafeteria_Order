

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;


public abstract class AbstractRepoController {
    protected final Supplier<List<IOrder>> ordersProvider;

    public AbstractRepoController(Supplier<List<IOrder>> ordersProvider) {
        this.ordersProvider = Objects.requireNonNull(ordersProvider);
    }

    protected List<IOrder> allOrders() {
        return ordersProvider.get();
    }

    public static class DatabaseConnection {
        private static final String URL = "jdbc:mysql://localhost:3306/cafeteria_system";
        private static final String USER = "root";
        private static final String PASSWORD = "MyNewPass456";

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }
}
