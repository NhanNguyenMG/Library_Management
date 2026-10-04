package model;

public class Manager extends Account {
    private String managerId;
    private String fullName;
    private String email;
    private String title;

    public Manager() { super(); }

    // Bổ sung constructor có tham số
    public Manager(String username, String password, String status, String managerId, String fullName, String email, String title) {
        super(username, password, "MANAGER", status);
        this.managerId = managerId;
        this.fullName = fullName;
        this.email = email;
        this.title = title;
    }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}