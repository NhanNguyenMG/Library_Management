package model;

public class PriorityStudent extends Student {
    private double discountRate; 
    private String priorityReason;

    public PriorityStudent() {}

    public double calculateDiscountedFine(double originalFine) {
        return originalFine * (1.0 - discountRate / 100.0);
    }

    public double getDiscountRate() { return discountRate; }
    public void setDiscountRate(double discountRate) { this.discountRate = discountRate; }
    public String getPriorityReason() { return priorityReason; }
    public void setPriorityReason(String priorityReason) { this.priorityReason = priorityReason; }
}