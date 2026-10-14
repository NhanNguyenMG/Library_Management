package tests;

import model.PriorityStudent;
import model.Student;
import service.ReturnService;

import java.sql.Timestamp;

/**
 * Kiểm thử cho UC-05, UC-06, UC-07 Xử lý trả sách, tồn kho và tiền phạt.
 * Bao phủ TC-09, TC-10, TC-11, TC-12, TC-13, TC-14 theo Bảng 18 SRS.
 */
public class ReturnServiceTest {

    private final ReturnService returnService = new ReturnService();

    /**
     * TC-09: Trả đúng hạn (0 ngày trễ) -> Tiền phạt = 0 đ.
     */
    public boolean testReturnOnTime() {
        Timestamp due = Timestamp.valueOf("2026-10-15 12:00:00");
        Timestamp actual = Timestamp.valueOf("2026-10-15 10:00:00");
        long lateDays = returnService.calculateLateDays(due, actual);
        Student student = new Student("SV0001", "Bảo", "b@sv.edu.vn", "sv001", "0901", 1, 0, "THUONG");
        double fine = returnService.calculateFine(lateDays, student, 0.0);
        return lateDays == 0 && fine == 0.0;
    }

    /**
     * TC-10: Trả trễ 3 ngày (sinh viên thường) -> Phạt đúng 15.000 đ (5.000đ/ngày).
     */
    public boolean testReturnLate3Days() {
        Student student = new Student("SV0001", "Bảo", "b@sv.edu.vn", "sv001", "0901", 1, 0, "THUONG");
        double fine = returnService.calculateFine(3, student, 0.0);
        return fine == 15000.0;
    }

    /**
     * TC-11: Trả trễ đúng 1 ngày -> Phạt 5.000 đ.
     */
    public boolean testReturnLate1Day() {
        Student student = new Student("SV0001", "Bảo", "b@sv.edu.vn", "sv001", "0901", 1, 0, "THUONG");
        double fine = returnService.calculateFine(1, student, 0.0);
        return fine == 5000.0;
    }

    /**
     * TC-13: Sinh viên ưu tiên được giảm trừ 20% tiền phạt (15.000đ -> 12.000đ).
     */
    public boolean testPriorityStudentDiscount() {
        PriorityStudent priorityStudent = new PriorityStudent("SV0002", "Bình", "binh@sv.edu.vn", "sv002", "0902", 1, 0.0, "UU_TIEN", 20.0, "Hộ nghèo");
        double fine = returnService.calculateFine(3, priorityStudent, 0.0);
        return fine == 12000.0;
    }

    /**
     * TC-14: Sách hư hỏng -> Cộng thêm phí bồi thường (phí đền bù không bị giảm trừ ưu tiên).
     */
    public boolean testDamageCompensationFee() {
        PriorityStudent priorityStudent = new PriorityStudent("SV0002", "Bình", "binh@sv.edu.vn", "sv002", "0902", 1, 0.0, "UU_TIEN", 20.0, "Hộ nghèo");
        // 3 ngày trễ (12.000đ sau giảm) + 50.000đ bồi thường = 62.000đ
        double fine = returnService.calculateFine(3, priorityStudent, 50000.0);
        return fine == 62000.0;
    }

    /**
     * Kiểm tra nghiệp vụ mở rộng: Tính phí phạt tỷ lệ theo số lượng tài liệu trễ hạn (SRS 2.3.7 bước 4).
     * 2 cuốn sách trễ 2 ngày -> 2 * 2 * 5.000 = 20.000 VNĐ.
     */
    public boolean testMultiItemFineWithQuantity() {
        Student student = new Student("SV0001", "Bảo", "b@sv.edu.vn", "sv001", "0901", 2, 0, "THUONG");
        Timestamp now = Timestamp.valueOf("2026-10-10 10:00:00");
        Timestamp due = Timestamp.valueOf("2026-10-08 10:00:00"); // Trễ 2 ngày

        java.util.List<dto.BorrowingItemDTO> items = new java.util.ArrayList<>();
        dto.BorrowingItemDTO item1 = new dto.BorrowingItemDTO("PM0001", "MS0001", "Sách 1", due, 1);
        item1.setStatus("Đã quét");
        dto.BorrowingItemDTO item2 = new dto.BorrowingItemDTO("PM0001", "MS0002", "Sách 2", due, 1);
        item2.setStatus("Đã quét");
        items.add(item1);
        items.add(item2);

        double totalFine = returnService.calculateTotalFineForItems(items, student, now);
        return totalFine == 20000.0;
    }

    /**
     * Kiểm tra nghiệp vụ mở rộng: Tính phí phạt độc lập theo thời hạn trả (dueDate) của từng tài liệu.
     * Sách 1 trễ 5 ngày (25.000đ), Sách 2 trễ 1 ngày (5.000đ) -> Tổng: 30.000 VNĐ.
     */
    public boolean testIndependentDueDates() {
        Student student = new Student("SV0001", "Bảo", "b@sv.edu.vn", "sv001", "0901", 2, 0, "THUONG");
        Timestamp now = Timestamp.valueOf("2026-10-10 10:00:00");
        Timestamp due1 = Timestamp.valueOf("2026-10-05 10:00:00"); // Trễ 5 ngày
        Timestamp due2 = Timestamp.valueOf("2026-10-09 10:00:00"); // Trễ 1 ngày

        java.util.List<dto.BorrowingItemDTO> items = new java.util.ArrayList<>();
        dto.BorrowingItemDTO item1 = new dto.BorrowingItemDTO("PM0001", "MS0001", "Sách 1", due1, 1);
        item1.setStatus("Đã quét");
        dto.BorrowingItemDTO item2 = new dto.BorrowingItemDTO("PM0001", "MS0002", "Sách 2", due2, 1);
        item2.setStatus("Đã quét");
        items.add(item1);
        items.add(item2);

        double totalFine = returnService.calculateTotalFineForItems(items, student, now);
        long maxLateDays = returnService.calculateMaxLateDays(items, now);
        return totalFine == 30000.0 && maxLateDays == 5;
    }

    public static void main(String[] args) {
        ReturnServiceTest test = new ReturnServiceTest();
        System.out.println("[TEST] TC-09 (Xử lý trả sách đúng hạn - Mức phạt 0 VNĐ): " + (test.testReturnOnTime() ? "[ PASSED ]" : "[ FAILED ]"));
        System.out.println("[TEST] TC-10 (Xử lý trả sách trễ hạn 3 ngày - Phạt 15.000 VNĐ): " + (test.testReturnLate3Days() ? "[ PASSED ]" : "[ FAILED ]"));
        System.out.println("[TEST] TC-11 (Xử lý trả sách trễ hạn 1 ngày - Phạt 5.000 VNĐ): " + (test.testReturnLate1Day() ? "[ PASSED ]" : "[ FAILED ]"));
        System.out.println("[TEST] TC-13 (Chính sách sinh viên ưu tiên - Giảm trừ 20%): " + (test.testPriorityStudentDiscount() ? "[ PASSED ]" : "[ FAILED ]"));
        System.out.println("[TEST] TC-14 (Xử lý tài liệu hư hỏng kèm phí bồi thường): " + (test.damageCompensationFee() ? "[ PASSED ]" : "[ FAILED ]"));
        System.out.println("[TEST] TC-EXT-01 (Tính phí phạt theo cấp số lượng tài liệu trễ hạn): " + (test.testMultiItemFineWithQuantity() ? "[ PASSED ]" : "[ FAILED ]"));
        System.out.println("[TEST] TC-EXT-02 (Xử lý thời hạn hoàn trả độc lập theo từng tài liệu): " + (test.testIndependentDueDates() ? "[ PASSED ]" : "[ FAILED ]"));
    }

    private boolean damageCompensationFee() {
        return testDamageCompensationFee();
    }
}
