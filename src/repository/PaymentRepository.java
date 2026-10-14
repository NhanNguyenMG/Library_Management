package repository;

import db.DBConnection;
import model.Payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class PaymentRepository {

    private static final String TRANSACTION_ID_PREFIX = "TT";
    private static final int TRANSACTION_ID_NUMBER_LENGTH = 4;

    /**
     * Sinh mã giao dịch thanh toán tiếp theo theo định dạng TTxxxx (ví dụ: TT0001 -> TT0002).
     *
     * @param conn kết nối Transaction đang mở
     * @return mã giao dịch mới chưa tồn tại
     * @throws SQLException nếu truy vấn CSDL thất bại
     */
    public String generateNextTransactionId(Connection conn) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(ma_giao_dich, ?) AS UNSIGNED)) AS max_number "
                + "FROM thanh_toan WHERE ma_giao_dich LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, TRANSACTION_ID_PREFIX.length() + 1);
            ps.setString(2, TRANSACTION_ID_PREFIX + "%");
            try (ResultSet rs = ps.executeQuery()) {
                long maxNumber = 0;
                if (rs.next()) {
                    maxNumber = rs.getLong("max_number");
                }
                return String.format("%s%0" + TRANSACTION_ID_NUMBER_LENGTH + "d", TRANSACTION_ID_PREFIX, maxNumber + 1);
            }
        }
    }

    /**
     * Lưu thông tin thanh toán phạt vào bảng thanh_toan.
     *
     * @param payment đối tượng Payment cần lưu
     * @param conn kết nối Transaction đang mở
     * @throws SQLException nếu ghi CSDL thất bại
     */
    public void save(Payment payment, Connection conn) throws SQLException {
        String sql = """
                INSERT INTO thanh_toan (ma_giao_dich, ma_phieu, so_tien, phuong_thuc, trang_thai, thoi_gian)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, payment.getTransactionId());
            ps.setString(2, payment.getSlipId());
            ps.setDouble(3, payment.getAmount());
            ps.setString(4, payment.getPaymentMethod());
            ps.setString(5, payment.getStatus());
            ps.setTimestamp(6, payment.getPaymentTime() != null ? payment.getPaymentTime() : new Timestamp(System.currentTimeMillis()));

            if (ps.executeUpdate() != 1) {
                throw new SQLException("Không thể lưu bản ghi thanh toán: " + payment.getTransactionId());
            }
        }
    }

    /**
     * Tạo nhanh bản ghi phạt và lưu vào CSDL trong Transaction.
     */
    public Payment createFineRecord(String slipId, double fineAmount, String method, String status, Connection conn) throws SQLException {
        String txId = generateNextTransactionId(conn);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Payment payment = new Payment(txId, slipId, fineAmount, method, status, now);
        save(payment, conn);
        return payment;
    }

    /**
     * Lưu mới hoặc cập nhật bản ghi phạt nếu phiếu mượn đã có giao dịch trước đó.
     */
    public Payment saveOrUpdateFineRecord(String slipId, double fineAmount, String method, String status, Connection conn) throws SQLException {
        Payment existing = findBySlipId(slipId, conn);
        if (existing != null) {
            String newStatus = status;
            if ("CHUA_THANH_TOAN".equalsIgnoreCase(existing.getStatus()) && "DA_THANH_TOAN".equalsIgnoreCase(status)) {
                newStatus = "CHUA_THANH_TOAN";
            }
            String sql = "UPDATE thanh_toan SET so_tien = so_tien + ?, phuong_thuc = ?, trang_thai = ?, thoi_gian = ? WHERE ma_phieu = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, fineAmount);
                ps.setString(2, method);
                ps.setString(3, newStatus);
                ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                ps.setString(5, slipId);
                ps.executeUpdate();
            }
            existing.setAmount(existing.getAmount() + fineAmount);
            existing.setStatus(newStatus);
            return existing;
        } else {
            return createFineRecord(slipId, fineAmount, method, status, conn);
        }
    }

    /**
     * Tìm thông tin thanh toán theo mã phiếu mượn (sử dụng Connection truyền vào).
     */
    public Payment findBySlipId(String slipId, Connection conn) throws SQLException {
        String sql = """
                SELECT ma_giao_dich, ma_phieu, so_tien, phuong_thuc, trang_thai, thoi_gian
                FROM thanh_toan
                WHERE ma_phieu = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slipId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Payment(
                            rs.getString("ma_giao_dich"),
                            rs.getString("ma_phieu"),
                            rs.getDouble("so_tien"),
                            rs.getString("phuong_thuc"),
                            rs.getString("trang_thai"),
                            rs.getTimestamp("thoi_gian")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Tìm thông tin thanh toán theo mã phiếu mượn (kết nối tự nạp).
     */
    public Payment findBySlipId(String slipId) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        return findBySlipId(slipId, conn);
    }

    /**
     * Tìm danh sách các khoản thanh toán phạt chưa thanh toán (CHUA_THANH_TOAN) của một sinh viên.
     */
    public List<Payment> findUnpaidPaymentsByStudentId(String studentId, Connection conn) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = """
                SELECT tt.ma_giao_dich, tt.ma_phieu, tt.so_tien, tt.phuong_thuc, tt.trang_thai, tt.thoi_gian
                FROM thanh_toan tt
                JOIN phieu_muon pm ON tt.ma_phieu = pm.ma_phieu
                WHERE pm.mssv = ? AND tt.trang_thai = 'CHUA_THANH_TOAN'
                ORDER BY tt.thoi_gian ASC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Payment(
                            rs.getString("ma_giao_dich"),
                            rs.getString("ma_phieu"),
                            rs.getDouble("so_tien"),
                            rs.getString("phuong_thuc"),
                            rs.getString("trang_thai"),
                            rs.getTimestamp("thoi_gian")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Tìm danh sách các khoản phạt chưa thanh toán (tự mở kết nối).
     */
    public List<Payment> findUnpaidPaymentsByStudentId(String studentId) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        return findUnpaidPaymentsByStudentId(studentId, conn);
    }

    /**
     * Cập nhật số tiền còn nợ của một bản ghi thanh toán (khi sinh viên trả nợ một phần).
     *
     * @param transactionId mã giao dịch
     * @param newAmount số tiền nợ mới sau khi trừ
     * @param conn kết nối Transaction
     * @throws SQLException nếu cập nhật thất bại
     */
    public void updateRemainingAmount(String transactionId, double newAmount, Connection conn) throws SQLException {
        String sql = "UPDATE thanh_toan SET so_tien = ? WHERE ma_giao_dich = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newAmount);
            ps.setString(2, transactionId);
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật trạng thái bản ghi thanh toán sang DA_THANH_TOAN khi thu nợ.
     */
    public void markAsPaid(String transactionId, String paymentMethod, Connection conn) throws SQLException {
        String sql = "UPDATE thanh_toan SET trang_thai = 'DA_THANH_TOAN', phuong_thuc = ?, thoi_gian = ? WHERE ma_giao_dich = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentMethod);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setString(3, transactionId);
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật trạng thái bản ghi thanh toán sang trạng thái chỉ định.
     */
    public void updatePaymentStatus(String transactionId, String status, String paymentMethod, String librarianId, Connection conn) throws SQLException {
        markAsPaid(transactionId, paymentMethod, conn);
    }
}
