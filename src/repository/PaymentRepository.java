package repository;

import db.DBConnection;
import model.Payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Tầng: Data Access Layer (repository)
 * Use Case: UC-07 Tính tiền phạt trễ hạn & Thanh toán
 * Bảng CSDL: thanh_toan (ma_giao_dich, ma_phieu, so_tien, phuong_thuc, trang_thai, thoi_gian)
 */
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
     * @throws SQLException nếu ghi CSDL thất bại để rollback
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
                throw new SQLException("Không thể lưu bản ghi thanh toán " + payment.getTransactionId());
            }
        }
    }

    /**
     * Tạo nhanh bản ghi phạt và lưu vào CSDL trong Transaction.
     *
     * @param slipId mã phiếu mượn
     * @param fineAmount số tiền phạt
     * @param method phương thức thanh toán (TIEN_MAT, CHUYEN_KHOAN, VNPAY, CASH)
     * @param status trạng thái (CHUA_THANH_TOAN, DA_THANH_TOAN)
     * @param conn kết nối Transaction đang mở
     * @return đối tượng Payment đã được lưu
     * @throws SQLException nếu ghi thất bại
     */
    public Payment createFineRecord(String slipId, double fineAmount, String method, String status, Connection conn) throws SQLException {
        String txId = generateNextTransactionId(conn);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Payment payment = new Payment(txId, slipId, fineAmount, method, status, now);
        save(payment, conn);
        return payment;
    }

    /**
     * Tìm thông tin thanh toán theo mã phiếu mượn.
     *
     * @param slipId mã phiếu mượn
     * @return Payment nếu tìm thấy, null nếu chưa có bản ghi thanh toán
     * @throws SQLException nếu xảy ra lỗi truy vấn
     */
    public Payment findBySlipId(String slipId) throws SQLException {
        String sql = """
                SELECT ma_giao_dich, ma_phieu, so_tien, phuong_thuc, trang_thai, thoi_gian
                FROM thanh_toan
                WHERE ma_phieu = ?
                """;
        Connection conn = DBConnection.getInstance().getConnection();
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
}
