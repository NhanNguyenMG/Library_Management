package model;

public class Student {
    private String studentId;
    private String fullName;
    private String email;
    private String username;
    private String phone;
    private int borrowedCount;
    private double debtAmount;
    private String type;

    public Student() {}

    // Bổ sung constructor có tham số
    public Student(String studentId, String fullName, String email, String username, String phone, int borrowedCount, double debtAmount, String type) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.borrowedCount = borrowedCount;
        this.debtAmount = debtAmount;
        this.type = type;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getBorrowedCount() { return borrowedCount; }
    public void setBorrowedCount(int borrowedCount) { this.borrowedCount = borrowedCount; }
    public double getDebtAmount() { return debtAmount; }
    public void setDebtAmount(double debtAmount) { this.debtAmount = debtAmount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}