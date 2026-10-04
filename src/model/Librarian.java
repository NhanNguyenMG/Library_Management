package model;

public class Librarian extends Account {
    private String librarianId;
    private String fullName;
    private String shift;

    public Librarian() { super(); }

    public String getLibrarianId() { return librarianId; }
    public void setLibrarianId(String librarianId) { this.librarianId = librarianId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
}