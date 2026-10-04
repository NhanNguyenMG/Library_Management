package model;

public class PriorityStudent extends Student {
    private double discountRate;

    public PriorityStudent() {}

    public PriorityStudent(String studentId, String fullName, String email, double discountRate) {
        super(studentId, fullName, email);
        this.discountRate = discountRate;
    }

    public double calculateDiscountedFine(double originalFine) {
        return originalFine - (originalFine * discountRate);
    }

    public double getDiscountRate() { return discountRate; }
    public void setDiscountRate(double discountRate) { this.discountRate = discountRate; }
}