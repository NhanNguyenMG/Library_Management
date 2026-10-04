package model;

public class Librarian extends Account {
    private String librarianId;
    private String fullName;
    private String shift;

    public Librarian() { super(); }

    // Bổ sung constructor có tham số
    public Librarian(String username, String password, String status, String librarianId, String fullName, String shift) {
        super(username, password, "LIBRARIAN", status);
        this.librarianId = librarianId;
        this.fullName = fullName;
        this.shift = shift;
    }

    public String getLibrarianId() { return librarianId; }
    public void setLibrarianId(String librarianId) { this.librarianId = librarianId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
}