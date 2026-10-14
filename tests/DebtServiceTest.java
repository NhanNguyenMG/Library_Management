package tests;

import db.DBConnection;
import repository.PaymentRepository;
import repository.StudentRepository;
import dto.DebtPaymentResultDTO;
import model.Payment;
import model.Student;
import service.DebtService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class DebtServiceTest {

    private final DebtService debtService = new DebtService();
    private final StudentRepository studentRepo = new StudentRepository();
    private final PaymentRepository paymentRepo = new PaymentRepository();

    public static void main(String[] args) {
        DebtServiceTest test = new DebtServiceTest();
        boolean allPassed = true;

        System.out.println("=================================================");
        System.out.println("   KIỂM THỬ TỰ ĐỘNG: NGHIỆP VỤ THU NỢ SINH VIÊN  ");
        System.out.println("=================================================");

        allPassed &= test.testLookupStudentDebt();
        allPassed &= test.testPayDebtUpdatesStudentAndPaymentTable();

        System.out.println("=================================================");
        System.out.println("KẾT QUẢ TỔNG HỢP: " + (allPassed ? "[ TẤT CẢ KIỂM THỬ ĐẠT YÊU CẦU ]" : "[ CÓ KIỂM THỬ THẤT BẠI ]"));
        System.out.println("=================================================");
    }

    /**
     * Test tra cứu thông tin nợ của sinh viên
     */
    public boolean testLookupStudentDebt() {
        System.out.print("[TEST 1] Tra cứu thông tin nợ sinh viên (SV0003)... ");
        try {
            Student student = debtService.getStudentDebtInfo("SV0003");
            boolean valid = student != null && "SV0003".equals(student.getStudentId());
            List<Payment> unpaids = debtService.getUnpaidFines("SV0003");
            boolean pass = valid && unpaids != null;
            System.out.println(pass ? "[ PASSED ]" : "[ FAILED ]");
            return pass;
        } catch (Exception e) {
            System.out.println("[ FAILED ] (Chi tiết: " + e.getMessage() + ")");
            return false;
        }
    }

    /**
     * Test quy trình thanh toán thu nợ (Database Transaction)
     */
    public boolean testPayDebtUpdatesStudentAndPaymentTable() {
        System.out.print("[TEST 2] Giao dịch thanh toán nợ tiền phạt sinh viên... ");
        String testStudentId = "SV_DEBT_TEST";
        String testSlipId = "PM_DEBT_TEST";
        String testTxId = "TT_DEBT_TEST";

        try {
            cleanTestData(testStudentId, testSlipId, testTxId);

            // Tạo sinh viên mẫu đang nợ 50.000 VNĐ
            createTestStudentWithDebt(testStudentId, testSlipId, testTxId, 50000.0);

            // Kiểm tra trước khi trả nợ
            Student sBefore = studentRepo.findByStudentId(testStudentId);
            if (sBefore == null || sBefore.getDebtAmount() != 50000.0) {
                System.out.println("[ FAILED ] (Khởi tạo dữ liệu nợ mẫu không thành công)");
                return false;
            }

            // Thực hiện thu nợ 50.000 VNĐ
            DebtPaymentResultDTO result = debtService.payDebt(testStudentId, 50000.0, "TIEN_MAT", "TT0001");
            if (!result.isSuccess() || result.getRemainingDebt() != 0.0) {
                System.out.println("[ FAILED ] (Giao dịch thu nợ thất bại: " + result.getMessage() + ")");
                return false;
            }

            // Kiểm tra số nợ trong CSDL sau khi thanh toán
            Student sAfter = studentRepo.findByStudentId(testStudentId);
            if (sAfter.getDebtAmount() != 0.0) {
                System.out.println("[ FAILED ] (Số dư nợ chưa được quy về 0: " + sAfter.getDebtAmount() + ")");
                return false;
            }

            // Kiểm tra bản ghi thanh_toan chuyển sang DA_THANH_TOAN
            Payment pAfter = paymentRepo.findBySlipId(testSlipId);
            if (pAfter == null || !"DA_THANH_TOAN".equals(pAfter.getStatus())) {
                System.out.println("[ FAILED ] (Trạng thái thanh toán không hợp lệ: " + (pAfter != null ? pAfter.getStatus() : "null") + ")");
                return false;
            }

            cleanTestData(testStudentId, testSlipId, testTxId);
            System.out.println("[ PASSED ]");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[ FAILED ] (Chi tiết lỗi: " + e.getMessage() + ")");
            return false;
        }
    }

    private void cleanTestData(String studentId, String slipId, String txId) {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM thanh_toan WHERE ma_phieu = ? OR ma_giao_dich = ?")) {
                ps.setString(1, slipId);
                ps.setString(2, txId);
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
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sinh_vien WHERE mssv = ?")) {
                ps.setString(1, studentId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tai_khoan WHERE ten_dang_nhap = ?")) {
                ps.setString(1, "acc_" + studentId);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {}
    }

    private void createTestStudentWithDebt(String studentId, String slipId, String txId, double debtAmount) throws Exception {
        Connection conn = DBConnection.getInstance().getConnection();

        // 1. Tạo tài khoản
        String sqlAcc = "INSERT INTO tai_khoan (ten_dang_nhap, mat_khau, quyen_truy_cap, trang_thai) VALUES (?, '123456', 'SINH_VIEN', 'ACTIVE')";
        try (PreparedStatement ps = conn.prepareStatement(sqlAcc)) {
            ps.setString(1, "acc_" + studentId);
            ps.executeUpdate();
        }

        // 2. Tạo sinh viên
        String sqlSv = "INSERT INTO sinh_vien (mssv, ten_dang_nhap, ho_ten, email, sdt, so_sach_dang_muon, so_tien_no, loai) VALUES (?, ?, 'Sinh viên Test Nợ', 'test@sv.edu.vn', '0999999999', 0, ?, 'THUONG')";
        try (PreparedStatement ps = conn.prepareStatement(sqlSv)) {
            ps.setString(1, studentId);
            ps.setString(2, "acc_" + studentId);
            ps.setDouble(3, debtAmount);
            ps.executeUpdate();
        }

        // 3. Tạo phiếu mượn
        String sqlSlip = "INSERT INTO phieu_muon (ma_phieu, mssv, ma_nhan_vien, ngay_muon, han_tra, ngay_tra_thuc_te) VALUES (?, ?, 'TT0001', NOW(), NOW(), NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sqlSlip)) {
            ps.setString(1, slipId);
            ps.setString(2, studentId);
            ps.executeUpdate();
        }

        // 4. Tạo bản ghi thanh toán chưa thanh toán
        String sqlPay = "INSERT INTO thanh_toan (ma_giao_dich, ma_phieu, so_tien, phuong_thuc, trang_thai, thoi_gian) VALUES (?, ?, ?, 'TIEN_MAT', 'CHUA_THANH_TOAN', NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sqlPay)) {
            ps.setString(1, txId);
            ps.setString(2, slipId);
            ps.setDouble(3, debtAmount);
            ps.executeUpdate();
        }
    }
}
