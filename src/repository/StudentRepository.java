package repository;

import db.DBConnection;
import model.PriorityStudent;
import model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Tầng: Data Access Layer (repository)
 * Use Case: UC-04 Kiểm tra điều kiện mượn, UC-05 Xử lý trả sách, UC-07 Tính tiền phạt
 * Bảng CSDL: sinh_vien
 */
public class StudentRepository {

    /**
     * Tìm thông tin sinh viên theo mã số sinh viên (MSSV).
     * Tự động phân loại Student hoặc PriorityStudent dựa trên cột loai.
     *
     * @param studentId mã số sinh viên
     * @return đối tượng Student hoặc PriorityStudent, null nếu không tìm thấy
     * @throws SQLException nếu xảy ra lỗi truy vấn CSDL
     */
    public Student findByStudentId(String studentId) throws SQLException {
        String sql = """
                SELECT mssv, ten_dang_nhap, ho_ten, email, sdt,
                       so_sach_dang_muon, so_tien_no, muc_giam_gia, li_do, loai
                FROM sinh_vien
                WHERE mssv = ?
                """;

        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String loai = rs.getString("loai");
                    double mucGiamGia = rs.getDouble("muc_giam_gia");
                    String liDo = rs.getString("li_do");

                    if ("UU_TIEN".equalsIgnoreCase(loai)) {
                        return new PriorityStudent(
                                rs.getString("mssv"),
                                rs.getString("ho_ten"),
                                rs.getString("email"),
                                rs.getString("ten_dang_nhap"),
                                rs.getString("sdt"),
                                rs.getInt("so_sach_dang_muon"),
                                rs.getDouble("so_tien_no"),
                                loai,
                                mucGiamGia,
                                liDo
                        );
                    } else {
                        return new Student(
                                rs.getString("mssv"),
                                rs.getString("ho_ten"),
                                rs.getString("email"),
                                rs.getString("ten_dang_nhap"),
                                rs.getString("sdt"),
                                rs.getInt("so_sach_dang_muon"),
                                rs.getDouble("so_tien_no"),
                                loai
                        );
                    }
                }
            }
        }
        return null;
    }

    /**
     * Cập nhật số tiền nợ của sinh viên (cộng thêm khoản phạt trễ hạn / đền bù).
     *
     * @param studentId mã số sinh viên
     * @param addedDebt số tiền phạt phát sinh cần cộng thêm
     * @param conn kết nối Transaction đang mở
     * @throws SQLException nếu ghi CSDL thất bại
     */
    public void updateDebtAmount(String studentId, double addedDebt, Connection conn) throws SQLException {
        String sql = "UPDATE sinh_vien SET so_tien_no = so_tien_no + ? WHERE mssv = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, addedDebt);
            ps.setString(2, studentId);
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật số sách đang mượn (giảm khi trả sách).
     *
     * @param studentId mã số sinh viên
     * @param returnedCount số lượng sách trả
     * @param conn kết nối Transaction đang mở
     * @throws SQLException nếu ghi CSDL thất bại
     */
    public void decreaseBorrowedCount(String studentId, int returnedCount, Connection conn) throws SQLException {
        String sql = "UPDATE sinh_vien SET so_sach_dang_muon = GREATEST(0, so_sach_dang_muon - ?) WHERE mssv = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, returnedCount);
            ps.setString(2, studentId);
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật số sách đang mượn (tăng khi lập phiếu mượn - UC-03).
     *
     * @param studentId mã số sinh viên
     * @param borrowedCount số lượng sách mượn thêm
     * @param conn kết nối Transaction đang mở
     * @throws SQLException nếu ghi CSDL thất bại hoặc không tìm thấy sinh viên
     */
    public void increaseBorrowedCount(String studentId, int borrowedCount, Connection conn) throws SQLException {
        String sql = "UPDATE sinh_vien SET so_sach_dang_muon = so_sach_dang_muon + ? WHERE mssv = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowedCount);
            ps.setString(2, studentId);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Không tìm thấy sinh viên " + studentId + " để cập nhật số sách đang mượn.");
            }
        }
    }

    /**
     * Kiểm tra tài khoản của sinh viên có bị khóa không (UC-04 bước 3 - thẻ hợp lệ).
     *
     * @param studentId mã số sinh viên
     * @return true nếu tài khoản có trạng thái LOCKED
     * @throws SQLException nếu xảy ra lỗi truy vấn CSDL
     */
    public boolean isAccountLocked(String studentId) throws SQLException {
        String sql = """
                SELECT tk.trang_thai
                FROM sinh_vien sv
                JOIN tai_khoan tk ON sv.ten_dang_nhap = tk.ten_dang_nhap
                WHERE sv.mssv = ?
                """;

        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && "LOCKED".equalsIgnoreCase(rs.getString("trang_thai"));
            }
        }
    }
}
