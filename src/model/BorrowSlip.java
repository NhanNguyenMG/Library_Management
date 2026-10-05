package model;
import java.sql.Timestamp;

public class BorrowSlip {
    private String slipId;
    private String studentId;
    private String librarianId;
    private Timestamp borrowDate;
    private Timestamp dueDate;
    private Timestamp actualReturnDate;

    public BorrowSlip() {}

    // Bổ sung constructor có tham số
    public BorrowSlip(String slipId, String studentId, String librarianId, Timestamp borrowDate, Timestamp dueDate, Timestamp actualReturnDate) {
        this.slipId = slipId;
        this.studentId = studentId;
        this.librarianId = librarianId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.actualReturnDate = actualReturnDate;
    }

    public long calculateLateDays() {
        if (actualReturnDate != null && actualReturnDate.after(dueDate)) {
            long diffInMillis = actualReturnDate.getTime() - dueDate.getTime();
            return diffInMillis / (1000 * 60 * 60 * 24); 
        }
        return 0; 
    }

    public String getSlipId() { return slipId; }
    public void setSlipId(String slipId) { this.slipId = slipId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getLibrarianId() { return librarianId; }
    public void setLibrarianId(String librarianId) { this.librarianId = librarianId; }
    public Timestamp getBorrowDate() { return borrowDate; }
    public void setBorrowDate(Timestamp borrowDate) { this.borrowDate = borrowDate; }
    public Timestamp getDueDate() { return dueDate; }
    public void setDueDate(Timestamp dueDate) { this.dueDate = dueDate; }
    public Timestamp getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(Timestamp actualReturnDate) { this.actualReturnDate = actualReturnDate; }
}