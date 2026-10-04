package model;

public class BorrowDetail {
    private String slipId;
    private String bookId;
    private int quantity;

    public BorrowDetail() {}

    public BorrowDetail(String slipId, String bookId, int quantity) {
        this.slipId = slipId;
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public String getSlipId() { return slipId; }
    public void setSlipId(String slipId) { this.slipId = slipId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}