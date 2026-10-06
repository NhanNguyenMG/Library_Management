package test;

import model.BorrowingItemDTO;
import model.PriorityStudent;
import model.ReturnResult;
import model.Student;
import service.ReturnService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Kiểm thử Console độc lập cho Use Case UC-05, UC-06, UC-07
 * Bao phủ các Test Case theo SRS:
 * - TC-09: Trả đúng hạn (0 ngày) -> không phạt (0 đ)
 * - TC-10: Trả trễ 3 ngày -> phạt 15.000 đ (sinh viên thường)
 * - TC-11: Trả trễ 1 ngày -> phạt 5.000 đ
 * - TC-12: Quét mã sách không thuộc phiếu mượn -> chặn xác nhận
 * - TC-13: Sinh viên ưu tiên được giảm trừ phạt (ví dụ: giảm 20%)
 * - TC-14: Hư hỏng sách -> cộng thêm phí đền bù
 */
public class TestReturn {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println(" KIỂM THỬ ĐỘC LẬP: UC-05 (TRẢ SÁCH), UC-06 (KHO), UC-07 (PHẠT)");
        System.out.println("=================================================================\n");

        ReturnService service = new ReturnService();
        int passCount = 0;
        int totalTests = 6;

        // ---------------------------------------------------------------------
        // TEST CASE 09: Trả đúng hạn (0 ngày trễ) -> Tiền phạt = 0 đ
        // ---------------------------------------------------------------------
        try {
            Timestamp dueDate = Timestamp.valueOf("2026-10-15 12:00:00");
            Timestamp returnDate = Timestamp.valueOf("2026-10-15 10:00:00"); // Trước hạn 2 tiếng

            long lateDays = service.calculateLateDays(dueDate, returnDate);
            Student student = new Student("SV0001", "Nguyễn Văn A", "a@sv.edu.vn", "sv001", "0901234567", 2, 0.0, "THUONG");
            double fine = service.calculateFine(lateDays, student, 0.0);

            if (lateDays == 0 && fine == 0.0) {
                System.out.println("[PASS] TC-09: Trả đúng hạn -> 0 ngày trễ, Tiền phạt = 0 VNĐ");
                passCount++;
            } else {
                System.err.println("[FAIL] TC-09: Kết quả không mong muốn. Ngày: " + lateDays + ", Phạt: " + fine);
            }
        } catch (Exception e) {
            System.err.println("[FAIL] TC-09: Ngoại lệ " + e.getMessage());
        }

        // ---------------------------------------------------------------------
        // TEST CASE 10: Trả trễ 3 ngày (Sinh viên thường) -> Tiền phạt = 15.000 đ
        // ---------------------------------------------------------------------
        try {
            Timestamp dueDate = Timestamp.valueOf("2026-10-10 12:00:00");
            Timestamp returnDate = Timestamp.valueOf("2026-10-13 12:00:00"); // Trễ 3 ngày

            long lateDays = service.calculateLateDays(dueDate, returnDate);
            Student student = new Student("SV0001", "Nguyễn Văn A", "a@sv.edu.vn", "sv001", "0901234567", 2, 0.0, "THUONG");
            double fine = service.calculateFine(lateDays, student, 0.0);

            if (lateDays == 3 && fine == 15000.0) {
                System.out.println("[PASS] TC-10: Trả trễ 3 ngày -> Phạt đúng 15.000 VNĐ (5.000đ/ngày)");
                passCount++;
            } else {
                System.err.println("[FAIL] TC-10: Ngày: " + lateDays + ", Phạt: " + fine);
            }
        } catch (Exception e) {
            System.err.println("[FAIL] TC-10: Ngoại lệ " + e.getMessage());
        }

        // ---------------------------------------------------------------------
        // TEST CASE 11: Trả trễ 1 ngày -> Tiền phạt = 5.000 đ
        // ---------------------------------------------------------------------
        try {
            Timestamp dueDate = Timestamp.valueOf("2026-10-10 12:00:00");
            Timestamp returnDate = Timestamp.valueOf("2026-10-11 12:00:00"); // Trễ 1 ngày

            long lateDays = service.calculateLateDays(dueDate, returnDate);
            Student student = new Student("SV0001", "Nguyễn Văn A", "a@sv.edu.vn", "sv001", "0901234567", 2, 0.0, "THUONG");
            double fine = service.calculateFine(lateDays, student, 0.0);

            if (lateDays == 1 && fine == 5000.0) {
                System.out.println("[PASS] TC-11: Trả trễ 1 ngày -> Phạt đúng 5.000 VNĐ");
                passCount++;
            } else {
                System.err.println("[FAIL] TC-11: Ngày: " + lateDays + ", Phạt: " + fine);
            }
        } catch (Exception e) {
            System.err.println("[FAIL] TC-11: Ngoại lệ " + e.getMessage());
        }

        // ---------------------------------------------------------------------
        // TEST CASE 12: Chưa quét cuốn nào mà bấm xác nhận -> Chặn xác nhận
        // ---------------------------------------------------------------------
        try {
            List<BorrowingItemDTO> items = new ArrayList<>();
            BorrowingItemDTO item = new BorrowingItemDTO("PM0001", "DS0001", "Java Programming", Timestamp.valueOf("2026-10-15 00:00:00"), 1);
            // Chưa gọi setStatus("Đã quét") -> status = "Chưa trả"
            items.add(item);

            Student student = new Student("SV0001", "Nguyễn Văn A", "a@sv.edu.vn", "sv001", "0901234567", 1, 0.0, "THUONG");
            ReturnResult result = service.xacNhanTraSach(items, "PM0001", student, "NV0001");

            if (!result.isSuccess() && result.getMessage().contains("Đã quét")) {
                System.out.println("[PASS] TC-12: Chưa quét sách -> Hệ thống chặn xác nhận trả hợp lệ");
                passCount++;
            } else {
                System.err.println("[FAIL] TC-12: Không chặn xác nhận");
            }
        } catch (Exception e) {
            System.err.println("[FAIL] TC-12: Ngoại lệ " + e.getMessage());
        }

        // ---------------------------------------------------------------------
        // TEST CASE 13: Sinh viên ưu tiên trễ 3 ngày (Giảm giá 20%) -> Phạt = 12.000 đ
        // ---------------------------------------------------------------------
        try {
            Timestamp dueDate = Timestamp.valueOf("2026-10-10 12:00:00");
            Timestamp returnDate = Timestamp.valueOf("2026-10-13 12:00:00"); // Trễ 3 ngày

            long lateDays = service.calculateLateDays(dueDate, returnDate);
            PriorityStudent priorityStudent = new PriorityStudent(
                    "SV0002", "Trần Thị Ưu Tiên", "b@sv.edu.vn", "sv002", "0909888777",
                    2, 0.0, "UU_TIEN", 20.0, "Hoàn cảnh khó khăn"
            );

            // Gốc: 3 * 5000 = 15.000 đ -> Giảm 20% còn 12.000 đ
            double fine = service.calculateFine(lateDays, priorityStudent, 0.0);

            if (fine == 12000.0) {
                System.out.println("[PASS] TC-13: Sinh viên ưu tiên (giảm 20%) -> Phạt đúng 12.000 VNĐ (gốc 15.000đ)");
                passCount++;
            } else {
                System.err.println("[FAIL] TC-13: Phạt: " + fine + " (Kỳ vọng: 12.000)");
            }
        } catch (Exception e) {
            System.err.println("[FAIL] TC-13: Ngoại lệ " + e.getMessage());
        }

        // ---------------------------------------------------------------------
        // TEST CASE 14: Sách bị hư hỏng -> Cộng thêm phí bồi thường 50.000 đ
        // ---------------------------------------------------------------------
        try {
            Timestamp dueDate = Timestamp.valueOf("2026-10-15 12:00:00");
            Timestamp returnDate = Timestamp.valueOf("2026-10-15 10:00:00"); // Đúng hạn

            long lateDays = service.calculateLateDays(dueDate, returnDate);
            Student student = new Student("SV0001", "Nguyễn Văn A", "a@sv.edu.vn", "sv001", "0901234567", 1, 0.0, "THUONG");
            double compensation = 50000.0; // Phí hư hỏng
            double fine = service.calculateFine(lateDays, student, compensation);

            if (fine == 50000.0) {
                System.out.println("[PASS] TC-14: Sách hư hỏng đúng hạn -> Tính đúng phí bồi thường 50.000 VNĐ");
                passCount++;
            } else {
                System.err.println("[FAIL] TC-14: Phạt: " + fine + " (Kỳ vọng: 50.000)");
            }
        } catch (Exception e) {
            System.err.println("[FAIL] TC-14: Ngoại lệ " + e.getMessage());
        }

        System.out.println("\n-----------------------------------------------------------------");
        System.out.printf(" KẾT QUẢ KIỂM THỬ: %d/%d TEST CASES PASS (%.1f%%)\n", passCount, totalTests, (passCount * 100.0 / totalTests));
        System.out.println("-----------------------------------------------------------------");
    }
}
