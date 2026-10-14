package controller;

import dto.BorrowingItemDTO;
import dto.ReturnResultDTO;
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
    public Student layThongTinSinhVien(String studentId) {
        return returnService.getStudentInfo(studentId);
    }

    /**
     * Lấy danh sách các đầu sách đang mượn của sinh viên.
     */
    public List<BorrowingItemDTO> laySachDangMuon(String studentId) {
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
     * Tính tổng số tiền phạt phát sinh cho danh sách các cuốn sách được quét.
     */
    public double tinhTongTienPhat(List<BorrowingItemDTO> items, Student student, Timestamp actualReturnDate) {
        return returnService.calculateTotalFineForItems(items, student, actualReturnDate);
    }

    /**
     * Tính số ngày trễ lớn nhất trong danh sách các cuốn sách được quét.
     */
    public long tinhSoNgayTreLonNhat(List<BorrowingItemDTO> items, Timestamp actualReturnDate) {
        return returnService.calculateMaxLateDays(items, actualReturnDate);
    }

    /**
     * Xác nhận trả sách (mặc định chưa thu tiền mặt ngay).
     */
    public ReturnResultDTO xacNhanTraSach(List<BorrowingItemDTO> scannedItems,
                                           String slipId,
                                           Student student,
                                           String librarianId) {
        return returnService.xacNhanTraSach(scannedItems, slipId, student, librarianId, false);
    }

    /**
     * Xác nhận trả sách kèm lựa chọn thu tiền mặt ngay hoặc ghi nhận nợ.
     */
    public ReturnResultDTO xacNhanTraSach(List<BorrowingItemDTO> scannedItems,
                                           String slipId,
                                           Student student,
                                           String librarianId,
                                           boolean isPaidNow) {
        return returnService.xacNhanTraSach(scannedItems, slipId, student, librarianId, isPaidNow);
    }
}
