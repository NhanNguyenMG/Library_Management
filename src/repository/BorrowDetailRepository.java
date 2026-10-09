package repository;

import db.DBConnection;
import model.BorrowDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BorrowDetailRepository {

    private static final String DETAIL_ID_PREFIX = "CT";
    private static final int DETAIL_ID_NUMBER_LENGTH = 4;

    /**
     * 1. Sinh mã chi tiết tiếp theo theo định dạng CTxxxx (ví dụ: CT0004 -> CT0005).
     *
     * @param conn kết nối đang mở Transaction (do Service quản lý)
     */
    public String generateNextDetailId(Connection conn) throws SQLException {
        return formatDetailId(findMaxDetailNumber(conn) + 1);
    }

    /**
     * 2. Lưu một dòng chi tiết phiếu mượn.
     *
     * @param borrowDetail chi tiết đã có đủ id, slipId, bookId, quantity
     * @param conn         kết nối đang mở Transaction (do Service quản lý)
     * @throws SQLException nếu ghi thất bại (ví dụ trùng cặp ma_phieu + ma_dau_sach), để Service rollback
     */
    public void save(BorrowDetail borrowDetail, Connection conn) throws SQLException {
        String sql = "INSERT INTO chi_tiet_phieu_muon (id, ma_phieu, ma_dau_sach, so_luong, ghi_chu) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, borrowDetail.getId());
            ps.setString(2, borrowDetail.getSlipId());
            ps.setString(3, borrowDetail.getBookId());
            ps.setInt(4, borrowDetail.getQuantity());
            ps.setString(5, borrowDetail.getNote());

            if (ps.executeUpdate() != 1) {
                throw new SQLException("Không thể lưu chi tiết phiếu mượn " + borrowDetail.getId());
            }
        }
    }

    /**
     * 3. Lưu toàn bộ danh sách chi tiết của một phiếu mượn.
     * Dòng nào chưa có id sẽ được tự động cấp mã CTxxxx liên tiếp nhau.
     *
     * @param borrowDetails danh sách chi tiết của cùng một phiếu
     * @param conn          kết nối đang mở Transaction (do Service quản lý)
     */
    public void saveAll(List<BorrowDetail> borrowDetails, Connection conn) throws SQLException {
        long nextNumber = findMaxDetailNumber(conn) + 1;
        for (BorrowDetail borrowDetail : borrowDetails) {
            if (borrowDetail.getId() == null || borrowDetail.getId().trim().isEmpty()) {
                borrowDetail.setId(formatDetailId(nextNumber));
                nextNumber++;
            }
            save(borrowDetail, conn);
        }
    }

    /**
     * 4. Lấy danh sách chi tiết (các đầu sách) thuộc một phiếu mượn.
     *
     * @param slipId mã phiếu mượn
     * @return danh sách chi tiết, rỗng nếu phiếu không có chi tiết nào
     */
    public List<BorrowDetail> findBySlipId(String slipId) throws SQLException {
        String sql = "SELECT id, ma_phieu, ma_dau_sach, so_luong, ghi_chu "
                + "FROM chi_tiet_phieu_muon WHERE ma_phieu = ? ORDER BY id";
        List<BorrowDetail> borrowDetails = new ArrayList<>();
        Connection conn = DBConnection.getInstance().getConnection(); // Không đóng: kết nối dùng chung
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slipId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    borrowDetails.add(mapRowToBorrowDetail(rs));
                }
            }
        }
        return borrowDetails;
    }

    /**
     * Lấy danh sách các chi tiết mượn CHƯA TRẢ của một phiếu mượn (ghi_chu chưa đánh dấu DA_TRA).
     *
     * @param slipId mã phiếu mượn
     * @return danh sách các cuốn sách chưa trả
     */
    public List<BorrowDetail> findActiveBySlipId(String slipId) throws SQLException {
        String sql = "SELECT id, ma_phieu, ma_dau_sach, so_luong, ghi_chu "
                + "FROM chi_tiet_phieu_muon WHERE ma_phieu = ? AND (ghi_chu IS NULL OR ghi_chu NOT LIKE 'DA_TRA%') ORDER BY id";
        List<BorrowDetail> borrowDetails = new ArrayList<>();
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slipId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    borrowDetails.add(mapRowToBorrowDetail(rs));
                }
            }
        }
        return borrowDetails;
    }

    /**
     * Đánh dấu một đầu sách cụ thể trong phiếu mượn là đã trả.
     *
     * @param slipId mã phiếu mượn
     * @param bookId mã đầu sách được trả
     * @param conn kết nối Transaction đang mở
     */
    public void markAsReturned(String slipId, String bookId, Connection conn) throws SQLException {
        String sql = "UPDATE chi_tiet_phieu_muon SET ghi_chu = 'DA_TRA' WHERE ma_phieu = ? AND ma_dau_sach = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slipId);
            ps.setString(2, bookId);
            ps.executeUpdate();
        }
    }

    /**
     * Đếm số lượng đầu sách chưa trả còn lại trong phiếu mượn.
     *
     * @param slipId mã phiếu mượn
     * @param conn kết nối Transaction đang mở
     * @return số đầu sách chưa trả
     */
    public int countUnreturnedDetails(String slipId, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM chi_tiet_phieu_muon WHERE ma_phieu = ? AND (ghi_chu IS NULL OR ghi_chu NOT LIKE 'DA_TRA%')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slipId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        }
    }

    /**
     * 5. Đếm tổng số cuốn sách sinh viên đang mượn (thuộc các phiếu chưa trả và sách chưa trả).
     *
     * @param studentId mã số sinh viên (mssv)
     * @return tổng số cuốn đang mượn, 0 nếu không có
     */
    public int countBorrowingBooksByStudentId(String studentId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(ct.so_luong), 0) AS borrowing_count "
                + "FROM chi_tiet_phieu_muon ct "
                + "JOIN phieu_muon pm ON ct.ma_phieu = pm.ma_phieu "
                + "WHERE pm.mssv = ? AND pm.ngay_tra_thuc_te IS NULL AND (ct.ghi_chu IS NULL OR ct.ghi_chu NOT LIKE 'DA_TRA%')";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("borrowing_count") : 0;
            }
        }
    }

    // HÀM HỖ TRỢ NỘI BỘ
    /**
     * Lấy phần số lớn nhất của mã chi tiết hiện có (CT0004 -> 4). Bảng rỗng trả về 0.
     */
    private long findMaxDetailNumber(Connection conn) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(id, ?) AS UNSIGNED)) AS max_number "
                + "FROM chi_tiet_phieu_muon WHERE id LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, DETAIL_ID_PREFIX.length() + 1);
            ps.setString(2, DETAIL_ID_PREFIX + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("max_number") : 0;
            }
        }
    }

    private String formatDetailId(long number) {
        return String.format("%s%0" + DETAIL_ID_NUMBER_LENGTH + "d", DETAIL_ID_PREFIX, number);
    }

    /**
     * Ánh xạ một dòng ResultSet của bảng chi_tiet_phieu_muon sang đối tượng BorrowDetail.
     */
    private BorrowDetail mapRowToBorrowDetail(ResultSet rs) throws SQLException {
        BorrowDetail borrowDetail = new BorrowDetail();
        borrowDetail.setId(rs.getString("id"));
        borrowDetail.setSlipId(rs.getString("ma_phieu"));
        borrowDetail.setBookId(rs.getString("ma_dau_sach"));
        borrowDetail.setQuantity(rs.getInt("so_luong"));
        borrowDetail.setNote(rs.getString("ghi_chu"));
        return borrowDetail;
    }
}