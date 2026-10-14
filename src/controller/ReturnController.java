package controller;

import dto.BorrowingItemDTO;
import dto.ReturnResultDTO;
import model.Student;
import service.ReturnService;

import java.sql.Timestamp;
import java.util.List;

/**
 * Controller điều phối luồng nghiệp vụ Trả sách và xử lý phạt (UC-05, UC-06, UC-07).
 * Tuân thủ Coding Standards: Tên phương thức 100% bằng tiếng Anh.
 */
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController() {
        this.returnService = new ReturnService();
    }

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    /**
     * Tra cứu thông tin hồ sơ sinh viên theo mã thẻ / MSSV.
     */
    public Student getStudentInfo(String studentId) {
        return returnService.getStudentInfo(studentId);
    }

    /**
     * Lấy danh sách các đầu sách đang mượn của sinh viên.
     */
    public List<BorrowingItemDTO> getBorrowingDetails(String studentId) {
        return returnService.getBorrowingDetailsByStudent(studentId);
    }

    /**
     * Tính số ngày trễ hạn dựa vào hạn trả và ngày trả thực tế.
     */
    public long calculateLateDays(Timestamp dueDate, Timestamp actualReturnDate) {
        return returnService.calculateLateDays(dueDate, actualReturnDate);
    }

    /**
     * Tính số tiền phạt phát sinh cho một đầu sách.
     */
    public double calculateFine(long lateDays, Student student, double compensationFees) {
        return returnService.calculateFine(lateDays, student, compensationFees);
    }

    /**
     * Tính tổng số tiền phạt phát sinh cho danh sách các cuốn sách được quét.
     */
    public double calculateTotalFine(List<BorrowingItemDTO> items, Student student, Timestamp actualReturnDate) {
        return returnService.calculateTotalFineForItems(items, student, actualReturnDate);
    }

    /**
     * Tính số ngày trễ lớn nhất trong danh sách các cuốn sách được quét.
     */
    public long calculateMaxLateDays(List<BorrowingItemDTO> items, Timestamp actualReturnDate) {
        return returnService.calculateMaxLateDays(items, actualReturnDate);
    }

    /**
     * Xác nhận trả sách với giao dịch Database Transaction (mặc định chưa thu tiền mặt ngay).
     */
    public ReturnResultDTO confirmReturn(List<BorrowingItemDTO> scannedItems,
                                         String slipId,
                                         Student student,
                                         String librarianId) {
        return returnService.confirmReturn(scannedItems, slipId, student, librarianId, false);
    }

    /**
     * Xác nhận trả sách kèm lựa chọn thu tiền mặt ngay hoặc ghi nhận nợ.
     */
    public ReturnResultDTO confirmReturn(List<BorrowingItemDTO> scannedItems,
                                         String slipId,
                                         Student student,
                                         String librarianId,
                                         boolean isPaidNow) {
        return returnService.confirmReturn(scannedItems, slipId, student, librarianId, isPaidNow);
    }
}
