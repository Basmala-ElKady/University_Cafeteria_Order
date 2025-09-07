import java.sql.*;
import java.util.List;

public class StudentManagement implements IStudentManagement, IStudentAuthentication, IStudentPoints, IStudentDiscount {
    private Student currentStudent;
    private Connection connection;

    public StudentManagement() {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean addStudent(Student student) {
        String checkQuery = "SELECT * FROM Students WHERE email = ?";
        String insertQuery = "INSERT INTO Students (name, email, password, loyalty_points) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            checkStmt.setString(1, student.getEmail());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next())
                return false;

            insertStmt.setString(1, student.getName());
            insertStmt.setString(2, student.getEmail());
            insertStmt.setString(3, student.getPassword());
            insertStmt.setDouble(4, student.getLoyaltyPoints());
            insertStmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void removeStudent(int studentId) {
        String sql = "DELETE FROM Students WHERE student_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Remove student error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Student getStudent(int studentId) {
        String sql = "SELECT * FROM Students WHERE student_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Student(rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getDouble("loyalty_points"));
            }
        } catch (SQLException e) {
            System.out.println("Get student error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Student login(String email, String password) {
        String sql = "SELECT * FROM Students WHERE email = ? AND password = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentStudent = new Student(rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getDouble("loyalty_points"));
                currentStudent.setLoyaltyPoints(rs.getDouble("loyalty_points"));
                System.out.println("Login successful for: " + currentStudent.getName());
                return currentStudent;
            }
            System.out.println("No match found for email: " + email);
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getId() {
        return currentStudent != null ? currentStudent.getId() : 0;
    }

    @Override
    public String getStudentName(int id) {
        String sql = "SELECT name FROM Students WHERE student_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString("name");
        } catch (SQLException e) {
            System.out.println("Get name error: " + e.getMessage());
            e.printStackTrace();
        }
        return "Unknown";
    }

    public void addLoyaltyPoints(int studentId, double points) {
        String sql = "UPDATE Students SET loyalty_points = loyalty_points + ? WHERE student_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, points);
            ps.setInt(2, studentId);
            ps.executeUpdate();
            if (currentStudent != null && currentStudent.getId() == studentId) {
                currentStudent.setLoyaltyPoints(currentStudent.getLoyaltyPoints() + points); // Sync with currentStudent
            }
        } catch (SQLException e) {
            System.out.println("Add loyalty points error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean redeemLoyaltyPoints(int studentId, double points) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            String checkSql = "SELECT loyalty_points FROM Students WHERE student_id = ?";
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setInt(1, studentId);
                ResultSet rs = check.executeQuery();
                if (!rs.next())
                    return false;

                double current = rs.getDouble("loyalty_points");
                if (current < points)
                    return false;

                String updateSql = "UPDATE Students SET loyalty_points = loyalty_points - ? WHERE student_id = ?";
                try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                    update.setDouble(1, points);
                    update.setInt(2, studentId);
                    update.executeUpdate();
                    conn.commit();

                    if (currentStudent != null && currentStudent.getId() == studentId) {
                        currentStudent.setLoyaltyPoints(current - points); // Sync with currentStudent
                    }

                    System.out.println("You redeemed " + points + " points.");
                    System.out.println("Remaining points: " + (current - points));

                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Redeem points error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public double applyDiscount(int studentId, double amount) {
        return amount * 0.9;
    }

    @Override
    public Student getStudentById(int id) {
        return getStudent(id);
    }

    @Override
    public List<Student> getAllStudents() {
        return null;
    }

    @Override
    public void updateStudent(Student s) {
        String sql = "UPDATE Students SET name = ?, email = ?, password = ?, loyalty_points = ? WHERE student_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getPassword());
            ps.setDouble(4, s.getLoyaltyPoints());
            ps.setInt(5, s.getId());
            ps.executeUpdate();
            if (currentStudent != null && currentStudent.getId() == s.getId()) {
                currentStudent.setLoyaltyPoints(s.getLoyaltyPoints()); // Sync with currentStudent
            }
        } catch (SQLException e) {
            System.out.println("Update student error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Student getLoggedInStudent() {
        return currentStudent;
    }

    @Override
    public void addPoints(int studentId, double points) {
        addLoyaltyPoints(studentId, points);
    }

    public void logOut() {
        System.out.println("Logging out student: " + (currentStudent != null ? currentStudent.getName() : "none"));
        currentStudent = null;
    }
}