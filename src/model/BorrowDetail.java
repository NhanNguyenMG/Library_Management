package model;

public class BorrowDetail {
    private int id;
    private String slipId;
    private String bookId;
    private int quantity;
    private String note;

    public BorrowDetail() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSlipId() { return slipId; }
    public void setSlipId(String slipId) { this.slipId = slipId; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}