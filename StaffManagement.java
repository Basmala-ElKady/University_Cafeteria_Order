import java.sql.*;

class StaffManagement {
    private Staff loggedInStaff;

    public boolean addStaff(Staff staff) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO Staff (name, email, password) VALUES (?, ?, ?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, staff.getName());
            ps.setString(2, staff.getEmail());
            ps.setString(3, staff.getPassword());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    staff.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding staff: " + e.getMessage());
            return false;
        }
    }

    public Staff login(String email, String password) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT staff_id, name, email, password FROM Staff WHERE email = ? AND password = ?")) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Staff staff = new Staff(rs.getString("name"), rs.getString("email"), rs.getString("password"));
                staff.setId(rs.getInt("staff_id"));
                loggedInStaff = staff;
                return staff;
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error logging in staff: " + e.getMessage());
            return null;
        }
    }

    public Staff getLoggedInStaff() {
        return loggedInStaff;
    }

    public void logout() {
        loggedInStaff = null;
    }
}