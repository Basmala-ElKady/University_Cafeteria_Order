

import java.util.List;



public interface IStudentManagement {
    boolean addStudent(Student s);
    Student getStudentById(int id);
    List<Student> getAllStudents();
    void updateStudent(Student s);
    void removeStudent(int id);
    void addLoyaltyPoints(int studentId, double points);
    boolean redeemLoyaltyPoints(int studentId, double points);
}
