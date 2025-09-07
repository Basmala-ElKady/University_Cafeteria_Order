

public interface IStudentAuthentication {
    Student login(String email, String password);
    int getId();
    String getStudentName(int id);
    void addPoints(int studentId, double points);
    Student getStudent(int studentId);
    double applyDiscount(int studentId, double amount);
}
