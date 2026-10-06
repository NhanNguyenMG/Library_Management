package controller;

import model.BorrowingItemDTO;
import model.ReturnResult;
import model.Student;
import service.ReturnService;

import java.sql.Timestamp;
import java.util.List;

/**
 * Tầng: Controller Layer (controller)
 * Use Case: UC-05 Xử lý trả sách, UC-06 Cập nhật số lượng tồn kho, UC-07 Tính tiền phạt
 * Kiến trúc: Closed 4-Layer Architecture + MVC
 * Vai trò: Tiếp nhận yêu cầu từ ReturnBookForm (UI), ủy quyền xử lý cho ReturnService và trả kết quả về View
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
     * Tra cứu hồ sơ sinh viên theo mã thẻ / MSSV.
     */
    public Student layThongTinSinhVien(String studentId) throws Exception {
        return returnService.getStudentInfo(studentId);
    }

    /**
     * Lấy danh sách các đầu sách đang mượn của sinh viên.
     */
    public List<BorrowingItemDTO> laySachDangMuon(String studentId) throws Exception {
        return returnService.getBorrowingDetailsByStudent(studentId);
    }

    /**
     * Tính số ngày trễ hạn.
     */
    public long tinhSoNgayTre(Timestamp dueDate, Timestamp actualReturnDate) {
        return returnService.calculateLateDays(dueDate, actualReturnDate);
    }

    /**
     * Tính tổng số tiền phạt phát sinh.
     */
    public double tinhTienPhat(long lateDays, Student student, double compensationFees) {
        return returnService.calculateFine(lateDays, student, compensationFees);
    }

    /**
     * Xác nhận trả sách, ủy quyền cho ReturnService thực hiện Transaction ACID.
     */
    public ReturnResult xacNhanTraSach(List<BorrowingItemDTO> scannedItems,
                                       String slipId,
                                       Student student,
                                       String librarianId) {
        return returnService.xacNhanTraSach(scannedItems, slipId, student, librarianId);
    }
}
