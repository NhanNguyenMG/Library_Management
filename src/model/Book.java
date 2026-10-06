package model;

public class Book {

    private String bookId;
    private String title;
    private String author;
    private int stockQuantity;
    private String description;

    public Book() {}

    public Book(String bookId, String title, String author,
                int stockQuantity, String description) {

        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.stockQuantity = stockQuantity;
        this.description = description;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}