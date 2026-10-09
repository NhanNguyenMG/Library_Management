package controller;

import model.DebtPaymentResult;
import model.Payment;
import model.Student;
import service.DebtService;

import java.util.List;

public class DebtController {

    private final DebtService debtService;

    public DebtController() {
        this.debtService = new DebtService();
    }

    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }

    /**
     * Tra cứu thông tin hồ sơ và số tiền nợ của sinh viên
     */
    public Student layThongTinNoSinhVien(String studentId) throws Exception {
        return debtService.getStudentDebtInfo(studentId);
    }

    /**
     * Lấy danh sách các khoản phạt chưa thanh toán của sinh viên
     */
    public List<Payment> layDanhSachKhoanPhatChuaTra(String studentId) throws Exception {
        return debtService.getUnpaidFines(studentId);
    }

    /**
     * Xác nhận thu nợ tiền phạt cho sinh viên
     */
    public DebtPaymentResult thanhToanNo(String studentId, double amountToPay, String paymentMethod, String librarianId) {
        return debtService.payDebt(studentId, amountToPay, paymentMethod, librarianId);
    }
}
