package repository;

import db.DBConnection;
import model.BorrowSlip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * =========================================================================
 * Tầng: Data Access Layer (repository/)
 * Use Case: UC-03 Tạo phiếu mượn sách, UC-04 Kiểm tra điều kiện mượn
 * Bảng CSDL: phieu_muon (ma_phieu, mssv, ma_nhan_vien, ngay_muon, han_tra, ngay_tra_thuc_te)
 *
 * Các chức năng chính:
 * Sinh mã phiếu tự động (PMxxxx)
 * Lưu phiếu mượn mới (save)
 * Tìm phiếu theo mã (findById)
 * Đếm số phiếu quá hạn chưa trả (countOverdueSlipsByStudentId)
 * =========================================================================
 *
 * @author Người số 4 (UC-03, UC-04 Mượn sách)
 */
public class BorrowSlipRepository {

    private static final String SLIP_ID_PREFIX = "PM";
    private static final int SLIP_ID_NUMBER_LENGTH = 4;

    private static final String SELECT_SLIP_COLUMNS =
            "SELECT ma_phieu, mssv, ma_nhan_vien, ngay_muon, han_tra, ngay_tra_thuc_te FROM phieu_muon ";

    // UC-03: TẠO PHIẾU MƯỢN

    /**
     * 1. Sinh mã phiếu mượn tiếp theo theo định dạng PMxxxx (ví dụ: PM0003 -> PM0004).
     * Gọi bên trong Transaction tạo phiếu, dùng chung Connection với hàm save().
     *
     * @param conn kết nối đang mở Transaction (do Service quản lý)
     * @return mã phiếu mới chưa tồn tại trong bảng phieu_muon
     */
    public String generateNextSlipId(Connection conn) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(ma_phieu, ?) AS UNSIGNED)) AS max_number "
                + "FROM phieu_muon WHERE ma_phieu LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, SLIP_ID_PREFIX.length() + 1);
            ps.setString(2, SLIP_ID_PREFIX + "%");
            try (ResultSet rs = ps.executeQuery()) {
                long maxNumber = 0;
                if (rs.next()) {
                    maxNumber = rs.getLong("max_number"); // Bảng rỗng -> NULL -> 0
                }
                return String.format("%s%0" + SLIP_ID_NUMBER_LENGTH + "d", SLIP_ID_PREFIX, maxNumber + 1);
            }
        }
    }

    /**
     * 2. Lưu phiếu mượn mới vào bảng phieu_muon (phiếu mới luôn có ngay_tra_thuc_te = NULL).
     *
     * @param borrowSlip phiếu mượn đã có đủ slipId, studentId, librarianId, borrowDate, dueDate
     * @param conn       kết nối đang mở Transaction (do Service quản lý)
     * @throws SQLException nếu ghi thất bại, để Service thực hiện rollback
     */
    public void save(BorrowSlip borrowSlip, Connection conn) throws SQLException {
        String sql = "INSERT INTO phieu_muon (ma_phieu, mssv, ma_nhan_vien, ngay_muon, han_tra, ngay_tra_thuc_te) "
                + "VALUES (?, ?, ?, ?, ?, NULL)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, borrowSlip.getSlipId());
            ps.setString(2, borrowSlip.getStudentId());
            ps.setString(3, borrowSlip.getLibrarianId());
            ps.setTimestamp(4, borrowSlip.getBorrowDate());
            ps.setTimestamp(5, borrowSlip.getDueDate());

            if (ps.executeUpdate() != 1) {
                throw new SQLException("Không thể lưu phiếu mượn " + borrowSlip.getSlipId());
            }
        }
    }

    /**
     * 3. Tìm phiếu mượn theo mã phiếu (dùng để hiển thị lại phiếu vừa tạo).
     *
     * @param slipId mã phiếu mượn
     * @return BorrowSlip nếu tìm thấy, null nếu không tồn tại
     */
    public BorrowSlip findById(String slipId) throws SQLException {
        String sql = SELECT_SLIP_COLUMNS + "WHERE ma_phieu = ?";
        Connection conn = DBConnection.getInstance().getConnection(); // Không đóng: kết nối dùng chung
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slipId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRowToBorrowSlip(rs) : null;
            }
        }
    }

    // UC-04: KIỂM TRA ĐIỀU KIỆN MƯỢN

    /**
     * 4. Đếm số phiếu mượn đã quá hạn mà sinh viên chưa trả.
     * Thời điểm so sánh được truyền vào (thay vì dùng NOW() trong SQL)
     * để có thể kiểm thử với dữ liệu mẫu ở các mốc thời gian khác nhau.
     *
     * @param studentId mã số sinh viên (mssv)
     * @param checkTime thời điểm kiểm tra (thường là thời điểm hiện tại)
     * @return số phiếu quá hạn chưa trả, 0 nếu không có
     */
    public int countOverdueSlipsByStudentId(String studentId, Timestamp checkTime) throws SQLException {
        String sql = "SELECT COUNT(*) AS overdue_count FROM phieu_muon "
                + "WHERE mssv = ? AND ngay_tra_thuc_te IS NULL AND han_tra < ?";
        Connection conn = DBConnection.getInstance().getConnection(); // Không đóng: kết nối dùng chung
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setTimestamp(2, checkTime);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("overdue_count") : 0;
            }
        }
    }

    // =========================================================================
    // UC-05: TRẢ SÁCH — Người số 1 bổ sung theo PROJECT_PLAN.md (Giai đoạn 2):
    // =========================================================================

    /**
     * Lấy danh sách các phiếu mượn chưa trả của một sinh viên (ngay_tra_thuc_te IS NULL).
     *
     * @param studentId mã số sinh viên
     * @return danh sách BorrowSlip chưa trả, sắp xếp theo ngày mượn giảm dần
     */
    public List<BorrowSlip> findActiveSlipsByStudentId(String studentId) throws SQLException {
        String sql = SELECT_SLIP_COLUMNS + "WHERE mssv = ? AND ngay_tra_thuc_te IS NULL ORDER BY ngay_muon DESC";
        List<BorrowSlip> activeSlips = new ArrayList<>();
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    activeSlips.add(mapRowToBorrowSlip(rs));
                }
            }
        }
        return activeSlips;
    }

    /**
     * Lấy phiếu mượn chưa trả gần nhất của một sinh viên.
     *
     * @param studentId mã số sinh viên
     * @return BorrowSlip chưa trả gần nhất, hoặc null nếu không có
     */
    public BorrowSlip findActiveSlipByStudentId(String studentId) throws SQLException {
        List<BorrowSlip> activeSlips = findActiveSlipsByStudentId(studentId);
        return activeSlips.isEmpty() ? null : activeSlips.get(0);
    }

    /**
     * Cập nhật ngày trả thực tế của phiếu mượn trong Transaction.
     *
     * @param slipId mã phiếu mượn
     * @param returnDate thời điểm trả thực tế
     * @param conn kết nối Transaction đang mở
     * @return true nếu cập nhật thành công 1 dòng
     * @throws SQLException nếu có lỗi cập nhật
     */
    public boolean updateReturnDate(String slipId, Timestamp returnDate, Connection conn) throws SQLException {
        String sql = "UPDATE phieu_muon SET ngay_tra_thuc_te = ? WHERE ma_phieu = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, returnDate);
            ps.setString(2, slipId);
            return ps.executeUpdate() == 1;
        }
    }


    /**
     * Ánh xạ một dòng ResultSet của bảng phieu_muon sang đối tượng BorrowSlip.
     */
    private BorrowSlip mapRowToBorrowSlip(ResultSet rs) throws SQLException {
        BorrowSlip borrowSlip = new BorrowSlip();
        borrowSlip.setSlipId(rs.getString("ma_phieu"));
        borrowSlip.setStudentId(rs.getString("mssv"));
        borrowSlip.setLibrarianId(rs.getString("ma_nhan_vien"));
        borrowSlip.setBorrowDate(rs.getTimestamp("ngay_muon"));
        borrowSlip.setDueDate(rs.getTimestamp("han_tra"));
        borrowSlip.setActualReturnDate(rs.getTimestamp("ngay_tra_thuc_te"));
        return borrowSlip;
    }
}