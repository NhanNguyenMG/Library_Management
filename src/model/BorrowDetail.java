package model;

public class BorrowDetail {
    // Sửa kiểu int thành String để khớp với varchar trong CSDL
    private String id; 
    private String slipId;
    private String bookId;
    private int quantity;
    private String note;

    public BorrowDetail() {}

    // Bổ sung constructor có tham số
    public BorrowDetail(String id, String slipId, String bookId, int quantity, String note) {
        this.id = id;
        this.slipId = slipId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.note = note;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSlipId() { return slipId; }
    public void setSlipId(String slipId) { this.slipId = slipId; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}