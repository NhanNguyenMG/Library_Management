package dto;

/**
 * Data Transfer Object (DTO) chứa kết quả giao dịch thanh toán nợ tiền phạt
 * truyền tải từ DebtService sang DebtController, DashboardForm và SearchBookForm.
 */
public class DebtPaymentResultDTO {

    private final boolean success;
    private final String message;
    private final String studentId;
    private final double paidAmount;
    private final double remainingDebt;

    public DebtPaymentResultDTO(boolean success, String message, String studentId, double paidAmount, double remainingDebt) {
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
