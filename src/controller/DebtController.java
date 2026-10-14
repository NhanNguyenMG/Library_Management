package controller;

import dto.DebtPaymentResultDTO;
import model.Payment;
import model.Student;
import service.DebtService;

import java.util.List;

/**
 * Controller điều phối luồng nghiệp vụ Quản lý & Thu nợ tiền phạt sinh viên.
 * Tuân thủ Coding Standards: Tên phương thức 100% bằng tiếng Anh.
 */
public class DebtController {

    private final DebtService debtService;

    public DebtController() {
        this.debtService = new DebtService();
    }

    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }

    /**
     * Tra cứu thông tin hồ sơ và số tiền nợ của sinh viên.
     */
    public Student getStudentDebtInfo(String studentId) {
        return debtService.getStudentDebtInfo(studentId);
    }

    /**
     * Lấy danh sách các khoản phạt chưa thanh toán của sinh viên.
     */
    public List<Payment> getUnpaidFines(String studentId) {
        return debtService.getUnpaidFines(studentId);
    }

    /**
     * Xác nhận thu nợ tiền phạt cho sinh viên.
     */
    public DebtPaymentResultDTO payDebt(String studentId, double amountToPay, String paymentMethod, String librarianId) {
        return debtService.payDebt(studentId, amountToPay, paymentMethod, librarianId);
    }

    // =========================================================================
    // TRACEABILITY ALIASES (Khớp với Bảng 13 và Bảng 17 trong SRS)
    // =========================================================================

    public Student layThongTinNoSinhVien(String studentId) {
        return getStudentDebtInfo(studentId);
    }

    public List<Payment> layDanhSachKhoanPhatChuaTra(String studentId) {
        return getUnpaidFines(studentId);
    }

    public DebtPaymentResultDTO thanhToanNo(String studentId, double amountToPay, String paymentMethod, String librarianId) {
        return payDebt(studentId, amountToPay, paymentMethod, librarianId);
    }
}
