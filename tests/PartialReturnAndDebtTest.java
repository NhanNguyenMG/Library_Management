package tests;

import db.DBConnection;
import model.BorrowSlip;
import model.BorrowingItemDTO;
import model.Payment;
import model.ReturnResult;
import model.Student;
import repository.BookRepository;
import repository.BorrowDetailRepository;
import repository.BorrowSlipRepository;
import repository.PaymentRepository;
import repository.StudentRepository;
import service.ReturnService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PartialReturnAndDebtTest {

    private final ReturnService returnService = new ReturnService();
    private final BorrowSlipRepository borrowSlipRepo = new BorrowSlipRepository();
    private final BorrowDetailRepository borrowDetailRepo = new BorrowDetailRepository();
    private final BookRepository bookRepo = new BookRepository();
    private final PaymentRepository paymentRepo = new PaymentRepository();
    private final StudentRepository studentRepo = new StudentRepository();

    public static void main(String[] args) {
        PartialReturnAndDebtTest runner = new PartialReturnAndDebtTest();
        boolean allPassed = true;

        System.out.println("=================================================");
        System.out.println("   KIỂM THỬ TỰ ĐỘNG: TRẢ SÁCH TỪNG PHẦN & XỬ LÝ PHẠT ");
        System.out.println("=================================================");

        try {
            allPassed &= runner.testPartialReturnKeepsSlipOpen();
            allPassed &= runner.testDiemCham2CashPaymentDoesNotIncreaseDebt();
            allPassed &= runner.testDiemCham2DebtIncreasesStudentDebt();
        } catch (Exception e) {
            e.printStackTrace();
            allPassed = false;
        }

        System.out.println("=================================================");
        System.out.println("KẾT QUẢ TỔNG HỢP: " + (allPassed ? "[ TẤT CẢ KIỂM THỬ ĐÃ ĐẠT ]" : "[ CÓ KIỂM THỬ THẤT BẠI ]"));
        System.out.println("=================================================");
    }

    /**
     * Kiểm tra nghiệp vụ trả sách từng phần: Phiếu mượn 2 cuốn, chỉ trả 1 cuốn.
     * Kết quả kỳ vọng:
     * - Phiếu mượn chưa đóng (ngay_tra_thuc_te IS NULL).
     * - Cuốn đã trả được đánh dấu 'DA_TRA', cuốn còn lại vẫn hiển thị trong danh sách sách đang mượn.
     */
    public boolean testPartialReturnKeepsSlipOpen() throws Exception {
        System.out.print("[TEST 1] Nghiệp vụ trả sách từng phần (Partial Return)... ");
        String testSlipId = "PM_TEST_01";
        String testStudentId = "SV0001";
        String book1 = "DS0001";
        String book2 = "DS0002";

        cleanTestData(testSlipId, testStudentId);

        // Tạo phiếu mượn 2 cuốn mẫu
        createTestSlipWithTwoBooks(testSlipId, testStudentId, book1, book2);

        // Chuẩn bị item trả chỉ gồm cuốn 1
        BorrowingItemDTO itemToReturn = new BorrowingItemDTO(testSlipId, book1, "Sách 1", Timestamp.valueOf("2026-10-01 10:00:00"), 1);
        itemToReturn.setStatus("Đã quét");
        List<BorrowingItemDTO> scannedList = new ArrayList<>();
        scannedList.add(itemToReturn);

        Student student = studentRepo.findByStudentId(testStudentId);
        ReturnResult result = returnService.xacNhanTraSach(scannedList, testSlipId, student, "TT0001", true);

        if (!result.isSuccess()) {
            System.out.println("[ FAILED ] (Xác nhận trả không thành công: " + result.getMessage() + ")");
            return false;
        }

        // Kiểm tra ngay_tra_thuc_te của phiếu mượn
        BorrowSlip slipAfter = borrowSlipRepo.findById(testSlipId);
        if (slipAfter.getActualReturnDate() != null) {
            System.out.println("[ FAILED ] (Lỗi: Phiếu mượn bị đóng sớm ngay_tra_thuc_te != null)");
            return false;
        }

        // Kiểm tra danh sách sách còn lại của sinh viên
        List<BorrowingItemDTO> remainingBooks = returnService.getBorrowingDetailsByStudent(testStudentId);
        boolean hasBook1 = remainingBooks.stream().anyMatch(b -> b.getBookId().equals(book1));
        boolean hasBook2 = remainingBooks.stream().anyMatch(b -> b.getBookId().equals(book2));

        if (hasBook1 || !hasBook2) {
            System.out.println("[ FAILED ] (Lỗi: Danh sách sách mượn không khớp dữ liệu kiểm thử)");
            return false;
        }

        cleanTestData(testSlipId, testStudentId);
        System.out.println("[ PASSED ]");
        return true;
    }

    /**
     * Kiểm tra quy trình xử lý phạt - Nộp tiền mặt trực tiếp tại quầy (isPaidNow = true):
     * Kết quả kỳ vọng:
     * - thanh_toan.trang_thai = 'DA_THANH_TOAN'
     * - sinh_vien.so_tien_no KHÔNG tăng
     */
    public boolean testDiemCham2CashPaymentDoesNotIncreaseDebt() throws Exception {
        System.out.print("[TEST 2] Xử lý thu tiền mặt trực tiếp tại quầy (isPaidNow = true)... ");
        String testSlipId = "PM_TEST_02";
        String testStudentId = "SV0001";
        String book1 = "DS0001";

        cleanTestData(testSlipId, testStudentId);

        Student studentBefore = studentRepo.findByStudentId(testStudentId);
        double debtBefore = studentBefore.getDebtAmount();

        // Tạo phiếu mượn trễ hạn 2 ngày
        Timestamp dueDate = Timestamp.valueOf("2026-10-01 10:00:00");
        createTestSlipSingleBook(testSlipId, testStudentId, book1, dueDate);

        BorrowingItemDTO item = new BorrowingItemDTO(testSlipId, book1, "Sách 1", dueDate, 1);
        item.setStatus("Đã quét");
        List<BorrowingItemDTO> scannedList = List.of(item);

        ReturnResult result = returnService.xacNhanTraSach(scannedList, testSlipId, studentBefore, "TT0001", true);

        if (!result.isSuccess() || result.getFineAmount() <= 0) {
            System.out.println("[ FAILED ] (Không phát sinh tiền phạt theo quy định)");
            return false;
        }

        // Kiểm tra số nợ của sinh viên
        Student studentAfter = studentRepo.findByStudentId(testStudentId);
        if (studentAfter.getDebtAmount() != debtBefore) {
            System.out.println("[ FAILED ] (Số dư nợ thay đổi không đúng khi đã thu tiền mặt trực tiếp)");
            return false;
        }

        // Kiểm tra thanh_toan
        Payment payment = paymentRepo.findBySlipId(testSlipId);
        if (payment == null || !"DA_THANH_TOAN".equals(payment.getStatus())) {
            System.out.println("[ FAILED ] (Trạng thái giao dịch không phải DA_THANH_TOAN: " + (payment != null ? payment.getStatus() : "null") + ")");
            return false;
        }

        cleanTestData(testSlipId, testStudentId);
        System.out.println("[ PASSED ]");
        return true;
    }

    /**
     * Kiểm tra quy trình xử lý phạt - Ghi nợ vào tài khoản sinh viên (isPaidNow = false):
     * Kết quả kỳ vọng:
     * - thanh_toan.trang_thai = 'CHUA_THANH_TOAN'
     * - sinh_vien.so_tien_no TĂNG đúng bằng số tiền phạt
     */
    public boolean testDiemCham2DebtIncreasesStudentDebt() throws Exception {
        System.out.print("[TEST 3] Ghi nhận nợ phạt vào tài khoản sinh viên (isPaidNow = false)... ");
        String testSlipId = "PM_TEST_03";
        String testStudentId = "SV0001";
        String book1 = "DS0001";

        cleanTestData(testSlipId, testStudentId);

        Student studentBefore = studentRepo.findByStudentId(testStudentId);
        double debtBefore = studentBefore.getDebtAmount();

        // Tạo phiếu mượn trễ hạn 2 ngày
        Timestamp dueDate = Timestamp.valueOf("2026-10-01 10:00:00");
        createTestSlipSingleBook(testSlipId, testStudentId, book1, dueDate);

        BorrowingItemDTO item = new BorrowingItemDTO(testSlipId, book1, "Sách 1", dueDate, 1);
        item.setStatus("Đã quét");
        List<BorrowingItemDTO> scannedList = List.of(item);

        ReturnResult result = returnService.xacNhanTraSach(scannedList, testSlipId, studentBefore, "TT0001", false);

        if (!result.isSuccess() || result.getFineAmount() <= 0) {
            System.out.println("[ FAILED ] (Không phát sinh tiền phạt theo quy định)");
            return false;
        }

        // Kiểm tra số nợ của sinh viên
        Student studentAfter = studentRepo.findByStudentId(testStudentId);
        double expectedDebt = debtBefore + result.getFineAmount();
        if (Math.abs(studentAfter.getDebtAmount() - expectedDebt) > 0.01) {
            System.out.println("[ FAILED ] (Số tiền nợ ghi nhận không khớp với phí phạt phát sinh)");
            return false;
        }

        // Kiểm tra thanh_toan
        Payment payment = paymentRepo.findBySlipId(testSlipId);
        if (payment == null || !"CHUA_THANH_TOAN".equals(payment.getStatus())) {
            System.out.println("[ FAILED ] (Trạng thái giao dịch không phải CHUA_THANH_TOAN: " + (payment != null ? payment.getStatus() : "null") + ")");
            return false;
        }

        cleanTestData(testSlipId, testStudentId);
        // Trả lại nợ ban đầu cho SV0001
        Connection conn = DBConnection.getInstance().getConnection();
        studentRepo.deductDebt(testStudentId, result.getFineAmount(), conn);

        System.out.println("[ PASSED ]");
        return true;
    }

    private void cleanTestData(String slipId, String studentId) {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM thanh_toan WHERE ma_phieu = ?")) {
                ps.setString(1, slipId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM chi_tiet_phieu_muon WHERE ma_phieu = ?")) {
                ps.setString(1, slipId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM phieu_muon WHERE ma_phieu = ?")) {
                ps.setString(1, slipId);
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {
        }
    }

    private void createTestSlipWithTwoBooks(String slipId, String studentId, String book1, String book2) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        String sqlSlip = "INSERT INTO phieu_muon (ma_phieu, mssv, ma_nhan_vien, ngay_muon, han_tra, ngay_tra_thuc_te) VALUES (?, ?, 'TT0001', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), NULL)";
        try (PreparedStatement ps = conn.prepareStatement(sqlSlip)) {
            ps.setString(1, slipId);
            ps.setString(2, studentId);
            ps.executeUpdate();
        }

        String sqlDetail = "INSERT INTO chi_tiet_phieu_muon (id, ma_phieu, ma_dau_sach, so_luong, ghi_chu) VALUES (?, ?, ?, 1, NULL)";
        try (PreparedStatement ps = conn.prepareStatement(sqlDetail)) {
            ps.setString(1, "CT_T1_" + (System.currentTimeMillis() % 10000));
            ps.setString(2, slipId);
            ps.setString(3, book1);
            ps.executeUpdate();

            ps.setString(1, "CT_T2_" + ((System.currentTimeMillis() + 1) % 10000));
            ps.setString(2, slipId);
            ps.setString(3, book2);
            ps.executeUpdate();
        }
    }

    private void createTestSlipSingleBook(String slipId, String studentId, String book, Timestamp dueDate) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        String sqlSlip = "INSERT INTO phieu_muon (ma_phieu, mssv, ma_nhan_vien, ngay_muon, han_tra, ngay_tra_thuc_te) VALUES (?, ?, 'TT0001', NOW(), ?, NULL)";
        try (PreparedStatement ps = conn.prepareStatement(sqlSlip)) {
            ps.setString(1, slipId);
            ps.setString(2, studentId);
            ps.setTimestamp(3, dueDate);
            ps.executeUpdate();
        }

        String sqlDetail = "INSERT INTO chi_tiet_phieu_muon (id, ma_phieu, ma_dau_sach, so_luong, ghi_chu) VALUES (?, ?, ?, 1, NULL)";
        try (PreparedStatement ps = conn.prepareStatement(sqlDetail)) {
            ps.setString(1, "CT_T3_" + (System.currentTimeMillis() % 10000));
            ps.setString(2, slipId);
            ps.setString(3, book);
            ps.executeUpdate();
        }
    }
}
