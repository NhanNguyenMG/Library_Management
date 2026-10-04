package model;

public class FineInvoiceDTO {
    private String slipId;
    private String studentId;
    private long lateDays;
    private double totalFine;

    public FineInvoiceDTO() {}

    public FineInvoiceDTO(String slipId, String studentId, long lateDays, double totalFine) {
        this.slipId = slipId;
        this.studentId = studentId;
        this.lateDays = lateDays;
        this.totalFine = totalFine;
    }

    public String getSlipId() { return slipId; }
    public void setSlipId(String slipId) { this.slipId = slipId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public long getLateDays() { return lateDays; }
    public void setLateDays(long lateDays) { this.lateDays = lateDays; }

    public double getTotalFine() { return totalFine; }
    public void setTotalFine(double totalFine) { this.totalFine = totalFine; }
}