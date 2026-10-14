package dto;

import model.Book;
import model.Student;

/**
 * Data Transfer Object (DTO) chứa kết quả xử lý nghiệp vụ mượn sách
 * truyền tải từ BorrowService sang BorrowController và BorrowForm.
 */
public class BorrowResultDTO {

    private boolean success;
    private String message;
    private String warning;
    private Student student;
    private Book book;
    private String slipId;

    public BorrowResultDTO() {}

    public BorrowResultDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getWarning() { return warning; }
    public void setWarning(String warning) { this.warning = warning; }
    public boolean hasWarning() { return warning != null && !warning.isEmpty(); }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public String getSlipId() { return slipId; }
    public void setSlipId(String slipId) { this.slipId = slipId; }
}
