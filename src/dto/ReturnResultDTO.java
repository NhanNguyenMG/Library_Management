package dto;

/**
 * Data Transfer Object (DTO) chứa kết quả xử lý nghiệp vụ trả sách và tiền phạt
 * truyền tải từ ReturnService sang ReturnController và ReturnBookForm.
 */
public class ReturnResultDTO {
    private boolean isSuccess;
    private String message;
    private String slipId;
    private double fineAmount;
    private long lateDays;

    public ReturnResultDTO() {}

    public ReturnResultDTO(boolean isSuccess, String message, String slipId, double fineAmount, long lateDays) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.slipId = slipId;
        this.fineAmount = fineAmount;
        this.lateDays = lateDays;
    }

    public boolean isSuccess() { return isSuccess; }
    public void setSuccess(boolean success) { this.isSuccess = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSlipId() { return slipId; }
    public void setSlipId(String slipId) { this.slipId = slipId; }
    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }
    public long getLateDays() { return lateDays; }
    public void setLateDays(long lateDays) { this.lateDays = lateDays; }
}
