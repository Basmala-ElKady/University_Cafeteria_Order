public class Student {
    private int id;
    private String name;
    private String email;
    private String password;
    private double loyaltyPoints;

    // full constructor (for when fetching from DB)
    public Student(int id, String name, String email, String password, double loyaltyPoints) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.loyaltyPoints = loyaltyPoints;
    }

    // constructor without id (for inserting new student)
    public Student(String name, String email, String password, double loyaltyPoints) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.loyaltyPoints = loyaltyPoints;
    }

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public double getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(double loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', email='" + email +
                "', loyaltyPoints=" + loyaltyPoints + "}";
    }
}
