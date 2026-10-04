package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BorrowSlip {
    private String slipId;
    private String studentId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate actualReturnDate;

    public BorrowSlip() {}

    public BorrowSlip(String slipId, String studentId, LocalDate borrowDate, LocalDate dueDate, LocalDate actualReturnDate) {
        this.slipId = slipId;
        this.studentId = studentId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.actualReturnDate = actualReturnDate;
    }

    public long calculateLateDays() {
        if (actualReturnDate != null && actualReturnDate.isAfter(dueDate)) {
            return ChronoUnit.DAYS.between(dueDate, actualReturnDate);
        }
        return 0; 
    }

    public String getSlipId() { return slipId; }
    public void setSlipId(String slipId) { this.slipId = slipId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }
}