package service;

import db.DBConnection;
import repository.PaymentRepository;
import repository.StudentRepository;
import dto.DebtPaymentResultDTO;
import exception.BusinessException;
import exception.RepositoryException;
import model.Payment;
import model.Student;
import util.ValidationUtil;

import java.sql.Connection;
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
    public Student getStudentDebtInfo(String studentId) {
        ValidationUtil.validateStudentId(studentId);
        try {
            Student student = studentRepo.findByStudentId(studentId.trim());
            if (student == null) {
                throw new BusinessException("Không tìm thấy thông tin sinh viên với mã: " + studentId);
            }
            return student;
        } catch (SQLException e) {
            throw new RepositoryException("Lỗi truy vấn CSDL hồ sơ sinh viên: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách các khoản phạt chưa thanh toán của sinh viên.
     */
    public List<Payment> getUnpaidFines(String studentId) {
        ValidationUtil.validateStudentId(studentId);
        try {
            return paymentRepo.findUnpaidPaymentsByStudentId(studentId.trim());
        } catch (SQLException e) {
            throw new RepositoryException("Lỗi truy vấn CSDL danh sách khoản nợ phạt: " + e.getMessage(), e);
        }
    }

    /**
     * Thực hiện thanh toán thu nợ cho sinh viên trong một Database Transaction an toàn.
     *
     * @param studentId mã số sinh viên
     * @param amountToPay số tiền thu nợ
     * @param paymentMethod phương thức thanh toán (TIEN_MAT, CHUYEN_KHOAN, ONLINE...)
     * @param librarianId mã thủ thư thu tiền
     * @return DebtPaymentResultDTO kết quả giao dịch thu nợ
     */
    public DebtPaymentResultDTO payDebt(String studentId, double amountToPay, String paymentMethod, String librarianId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return new DebtPaymentResultDTO(false, "Mã thẻ sinh viên không được để trống.", studentId, 0, 0);
        }
        if (amountToPay <= 0) {
            return new DebtPaymentResultDTO(false, "Số tiền thanh toán phải lớn hơn 0 VNĐ.", studentId, 0, 0);
        }

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            Student student = studentRepo.findByStudentId(studentId.trim());
            if (student == null) {
                return new DebtPaymentResultDTO(false, "Không tìm thấy thông tin sinh viên có mã: " + studentId, studentId, 0, 0);
            }

            double currentDebt = student.getDebtAmount();
            if (currentDebt <= 0) {
                return new DebtPaymentResultDTO(false, "Sinh viên " + student.getFullName() + " hiện không có nợ tiền phạt cần thanh toán.", studentId, 0, 0);
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
                    paymentRepo.updatePaymentStatus(p.getPaymentId(), "DA_THANH_TOAN", paymentMethod, librarianId, conn);
                    remainingToAllocate -= p.getAmount();
                } else {
                    // Trả một phần khoản phạt
                    double remainingForThisFine = p.getAmount() - remainingToAllocate;
                    paymentRepo.updateRemainingAmount(p.getPaymentId(), remainingForThisFine, conn);
                    remainingToAllocate = 0;
                }
            }

            conn.commit(); // Hoàn tất Transaction
            double remainingDebt = Math.max(0, currentDebt - actualPayAmount);

            String successMsg;
            if (remainingDebt == 0) {
                successMsg = "Thanh toán thành công toàn bộ số nợ tiền phạt ("
                        + String.format("%,.0f VNĐ", actualPayAmount) + ").";
            } else {
                successMsg = "Thanh toán thành công " + String.format("%,.0f VNĐ", actualPayAmount)
                        + ". Số nợ tiền phạt còn lại: " + String.format("%,.0f VNĐ", remainingDebt) + ".";
            }

            return new DebtPaymentResultDTO(true, successMsg, studentId, actualPayAmount, remainingDebt);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return new DebtPaymentResultDTO(false, "Lỗi cơ sở dữ liệu trong quá trình thanh toán nợ: " + e.getMessage(), studentId, 0, 0);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return new DebtPaymentResultDTO(false, "Lỗi hệ thống trong quá trình xử lý thanh toán: " + e.getMessage(), studentId, 0, 0);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
