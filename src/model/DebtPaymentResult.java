package model;

/**
 * Đối tượng kết quả trả về khi thực hiện giao dịch thu nợ sinh viên
 */
public class DebtPaymentResult {

    private final boolean success;
    private final String message;
    private final String studentId;
    private final double paidAmount;
    private final double remainingDebt;

    public DebtPaymentResult(boolean success, String message, String studentId, double paidAmount, double remainingDebt) {
        this.success = success;
        this.message = message;
        this.studentId = studentId;
        this.paidAmount = paidAmount;
        this.remainingDebt = remainingDebt;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getStudentId() {
        return studentId;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public double getRemainingDebt() {
        return remainingDebt;
    }
}
