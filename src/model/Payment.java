package model;

public class Payment {
    private String paymentId;
    private String slipId;
    private double fineAmount;
    private String reason;

    public Payment() {}

    public Payment(String paymentId, String slipId, double fineAmount, String reason) {
        this.paymentId = paymentId;
        this.slipId = slipId;
        this.fineAmount = fineAmount;
        this.reason = reason;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getSlipId() { return slipId; }
    public void setSlipId(String slipId) { this.slipId = slipId; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}