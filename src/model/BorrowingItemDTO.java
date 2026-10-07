package model;

import java.sql.Timestamp;

/**
 * Thông tin chi tiết sách đang mượn hiển thị trong bảng trả sách
 */
public class BorrowingItemDTO {

    private String slipId;
    private String bookId;
    private String bookTitle;
    private Timestamp dueDate;
    private String status;              // "Chưa trả", "Đã quét"
    private String physicalCondition;   // "Tốt", "Hư hỏng", "Mất sách"
    private double compensationFee;     // Phí bồi thường nếu hư hỏng/mất sách
    private int quantity;

    public BorrowingItemDTO() {
        this.status = "Chưa trả";
        this.physicalCondition = "Tốt";
        this.compensationFee = 0.0;
        this.quantity = 1;
    }

    public BorrowingItemDTO(String slipId, String bookId, String bookTitle, Timestamp dueDate, int quantity) {
        this.slipId = slipId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.dueDate = dueDate;
        this.status = "Chưa trả";
        this.physicalCondition = "Tốt";
        this.compensationFee = 0.0;
        this.quantity = quantity;
    }

    public String getSlipId() { return slipId; }
    public void setSlipId(String slipId) { this.slipId = slipId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public Timestamp getDueDate() { return dueDate; }
    public void setDueDate(Timestamp dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPhysicalCondition() { return physicalCondition; }
    public void setPhysicalCondition(String physicalCondition) { this.physicalCondition = physicalCondition; }

    public double getCompensationFee() { return compensationFee; }
    public void setCompensationFee(double compensationFee) { this.compensationFee = compensationFee; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean isScanned() {
        return "Đã quét".equalsIgnoreCase(this.status);
    }
}
