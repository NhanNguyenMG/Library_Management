package model;
import java.sql.Timestamp;

public class Payment {
    private String transactionId;
    private String slipId;
    private double amount;
    private String paymentMethod;
    private String status;
    private Timestamp paymentTime;

    public Payment() {}

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getSlipId() { return slipId; }
    public void setSlipId(String slipId) { this.slipId = slipId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getPaymentTime() { return paymentTime; }
    public void setPaymentTime(Timestamp paymentTime) { this.paymentTime = paymentTime; }
}