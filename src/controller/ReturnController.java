package controller;

import model.BorrowingItemDTO;
import model.ReturnResult;
import model.Student;
import service.ReturnService;

import java.sql.Timestamp;
import java.util.List;

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
     * Xác nhận trả sách
     */
    public ReturnResult xacNhanTraSach(List<BorrowingItemDTO> scannedItems,
                                       String slipId,
                                       Student student,
                                       String librarianId) {
        return returnService.xacNhanTraSach(scannedItems, slipId, student, librarianId);
    }
}
