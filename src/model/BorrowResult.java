package model;

/**
 * =========================================================================
 * DTO: Kết quả nghiệp vụ Mượn sách (không ánh xạ bảng CSDL)
 * Use Case: UC-03 Tạo phiếu mượn sách, UC-04 Kiểm tra điều kiện mượn sách
 * Sequence Diagram: sd MuonSach (Hình 8 SRS, mục 5.5.2) - message ketQua(success, thongBao)
 * Luồng dữ liệu: BorrowService -> BorrowController -> BorrowForm (giống ReturnResult của UC-05)
 *
 * Các trường dữ liệu:
 * - success : true nếu nghiệp vụ thành công / đủ điều kiện
 * - message : thông báo chính hiển thị cho Thủ thư (thành công hoặc lý do thất bại)
 * - warning : cảnh báo phụ khi vẫn thành công (ví dụ "Sắp đạt giới hạn mượn" - UC-04 AF-2)
 * - student : thông tin sinh viên sau khi kiểm tra điều kiện (UC-03 bước 3)
 * - book    : thông tin đầu sách vừa quét hợp lệ (UC-03 bước 4)
 * - slipId  : mã phiếu mượn vừa tạo (UC-03 bước 8)
 * =========================================================================
 *
 * @author Người số 4 (UC-03, UC-04 Mượn sách)
 */
public class BorrowResult {

    private boolean success;
    private String message;
    private String warning;
    private Student student;
    private Book book;
    private String slipId;

    public BorrowResult() {}

    public BorrowResult(boolean success, String message) {
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
