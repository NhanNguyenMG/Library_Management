package service;

import db.DBConnection;
import model.DebtPaymentResult;
import model.Payment;
import model.Student;
import repository.PaymentRepository;
import repository.StudentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DebtService {

    private final StudentRepository studentRepo;
    private final PaymentRepository paymentRepo;

    public DebtService() {
        this.studentRepo = new StudentRepository();
        this.paymentRepo = new PaymentRepository();
    }

    public DebtService(StudentRepository studentRepo, PaymentRepository paymentRepo) {
        this.studentRepo = studentRepo;
        this.paymentRepo = paymentRepo;
    }

    /**
     * Tra cứu thông tin sinh viên và số nợ phạt hiện tại.
     */
    public Student getStudentDebtInfo(String studentId) throws Exception {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã thẻ sinh viên không được để trống.");
        }
        Student student = studentRepo.findByStudentId(studentId.trim());
        if (student == null) {
            throw new Exception("Không tìm thấy sinh viên với mã: " + studentId);
        }
        return student;
    }

    /**
     * Lấy danh sách các khoản phạt chưa thanh toán của sinh viên.
     */
    public List<Payment> getUnpaidFines(String studentId) throws Exception {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã thẻ sinh viên không được để trống.");
        }
        return paymentRepo.findUnpaidPaymentsByStudentId(studentId.trim());
    }

    /**
     * Thực hiện thanh toán thu nợ cho sinh viên trong một Database Transaction an toàn.
     *
     * @param studentId mã số sinh viên
     * @param amountToPay số tiền thu nợ
     * @param paymentMethod phương thức thanh toán (TIEN_MAT, CHUYEN_KHOAN, ...)
     * @param librarianId mã thủ thư thu tiền
     * @return DebtPaymentResult kết quả giao dịch thu nợ
     */
    public DebtPaymentResult payDebt(String studentId, double amountToPay, String paymentMethod, String librarianId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return new DebtPaymentResult(false, "Mã sinh viên không được để trống.", studentId, 0, 0);
        }
        if (amountToPay <= 0) {
            return new DebtPaymentResult(false, "Số tiền thanh toán phải lớn hơn 0 VNĐ.", studentId, 0, 0);
        }

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            Student student = studentRepo.findByStudentId(studentId.trim());
            if (student == null) {
                return new DebtPaymentResult(false, "Không tìm thấy thông tin sinh viên " + studentId, studentId, 0, 0);
            }

            double currentDebt = student.getDebtAmount();
            if (currentDebt <= 0) {
                return new DebtPaymentResult(false, "Sinh viên " + student.getFullName() + " hiện không có nợ tiền phạt.", studentId, 0, 0);
            }

            // Số tiền thực thu không được vượt quá số nợ hiện tại
            double actualPayAmount = Math.min(amountToPay, currentDebt);

            // 1. Giảm nợ của sinh viên trong bảng sinh_vien
            studentRepo.deductDebt(studentId.trim(), actualPayAmount, conn);

            // 2. Cập nhật các bản ghi thanh toán chưa trả (thanh_toan)
            List<Payment> unpaidPayments = paymentRepo.findUnpaidPaymentsByStudentId(studentId.trim(), conn);
            double remainingToAllocate = actualPayAmount;

            for (Payment p : unpaidPayments) {
                if (remainingToAllocate <= 0) {
                    break;
                }
                if (remainingToAllocate >= p.getAmount()) {
                    // Trả hết khoản phạt này
                    paymentRepo.markAsPaid(p.getTransactionId(), paymentMethod, conn);
                    remainingToAllocate -= p.getAmount();
                } else {
                    // Nếu trả một phần của khoản phạt này: giảm số tiền còn nợ của phiếu
                    double newSlipDebt = p.getAmount() - remainingToAllocate;
                    String sql = "UPDATE thanh_toan SET so_tien = ? WHERE ma_giao_dich = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setDouble(1, newSlipDebt);
                        ps.setString(2, p.getTransactionId());
                        ps.executeUpdate();
                    }
                    remainingToAllocate = 0;
                }
            }

            // Commit thành công giao dịch
            conn.commit();

            double remainingDebt = Math.max(0, currentDebt - actualPayAmount);
            String successMsg = String.format("Thanh toán nợ thành công cho sinh viên %s (%s)!\n"
                    + "Đã thu: %,.0f VNĐ (Phương thức: %s).\n"
                    + "Số nợ còn lại: %,.0f VNĐ.",
                    student.getFullName(), studentId, actualPayAmount, paymentMethod, remainingDebt);

            return new DebtPaymentResult(true, successMsg, studentId, actualPayAmount, remainingDebt);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return new DebtPaymentResult(false, "Lỗi cơ sở dữ liệu khi thanh toán nợ: " + e.getMessage(), studentId, 0, 0);

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return new DebtPaymentResult(false, "Sự cố hệ thống: " + e.getMessage(), studentId, 0, 0);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
