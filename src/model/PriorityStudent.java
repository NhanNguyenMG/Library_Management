package model;

public class PriorityStudent extends Student {
    private double discountRate; 
    private String priorityReason;

    public PriorityStudent() {}

    // Bổ sung constructor có tham số
    public PriorityStudent(String studentId, String fullName, String email, String username, String phone, int borrowedCount, double debtAmount, String type, double discountRate, String priorityReason) {
        super(studentId, fullName, email, username, phone, borrowedCount, debtAmount, type);
        this.discountRate = discountRate;
        this.priorityReason = priorityReason;
    }

    public double calculateDiscountedFine(double originalFine) {
        return originalFine * (1.0 - discountRate / 100.0);
    }

    public double getDiscountRate() { return discountRate; }
    public void setDiscountRate(double discountRate) { this.discountRate = discountRate; }
    public String getPriorityReason() { return priorityReason; }
    public void setPriorityReason(String priorityReason) { this.priorityReason = priorityReason; }
}