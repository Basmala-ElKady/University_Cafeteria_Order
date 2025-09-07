


import java.sql.*;
import java.util.*;

public class OrderRepositoryController implements IOrderRepository {

    @Override
    public void save(IOrder order) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO orders(order_id, total_price, status, created_at) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, order.getOrderId());
            ps.setDouble(2, order.getTotalPrice());
            ps.setString(3, order.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(order.getCreatedAt()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<IOrder> findById(String id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders WHERE order_id=?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Dummy items list for simplicity
                return Optional.of(new OrderController(rs.getString("order_id"), new ArrayList<>()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<IOrder> getAll() {
        List<IOrder> orders = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM orders")) {
            while (rs.next()) {
                orders.add(new OrderController(rs.getString("order_id"), new ArrayList<>()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    @Override
    public void deleteById(String id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM orders WHERE order_id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
