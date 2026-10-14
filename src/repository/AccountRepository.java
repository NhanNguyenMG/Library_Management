package repository;

import db.DBConnection;
import model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AccountRepository {

    /**
     * Tìm tài khoản theo tên đăng nhập (chống SQL Injection qua PreparedStatement).
     *
     * @param username tên đăng nhập
     * @return đối tượng Account nếu tồn tại, null nếu không tìm thấy
     */
    public Account findByUsername(String username) {
        String sql = "SELECT ten_dang_nhap, mat_khau, quyen_truy_cap, trang_thai FROM tai_khoan WHERE ten_dang_nhap = ?";
        Account account = null;

        try {
            Connection conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        account = new Account(
                                rs.getString("ten_dang_nhap"),
                                rs.getString("mat_khau"),
                                rs.getString("quyen_truy_cap"),
                                rs.getString("trang_thai")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] [AccountRepository] Lỗi truy vấn tài khoản '" + username + "': " + e.getMessage());
        }

        return account;
    }

    /**
     * Lấy thêm thông tin chi tiết (ID, Họ tên, Email) từ các bảng phân quyền
     * dựa vào tên đăng nhập và vai trò.
     *
     * @param username tên đăng nhập
     * @param role vai trò người dùng (QUAN_LI, THU_THU, SINH_VIEN)
     * @return mảng 3 phần tử [userId, fullName, email]
     */
    public String[] getUserDetails(String username, String role) {
        String[] details = new String[3];
        String sql;

        if ("QUAN_LI".equals(role)) {
            sql = "SELECT ma_quan_li as id, ho_ten as name, email FROM quan_li WHERE ten_dang_nhap = ?";
        } else if ("THU_THU".equals(role)) {
            sql = "SELECT ma_nhan_vien as id, ho_ten as name, NULL as email FROM thu_thu WHERE ten_dang_nhap = ?";
        } else if ("SINH_VIEN".equals(role)) {
            sql = "SELECT mssv as id, ho_ten as name, email FROM sinh_vien WHERE ten_dang_nhap = ?";
        } else {
            return details;
        }

        try {
            Connection conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        details[0] = rs.getString("id");
                        details[1] = rs.getString("name");
                        details[2] = rs.getString("email");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] [AccountRepository] Lỗi lấy thông tin chi tiết cho tài khoản '" + username + "': " + e.getMessage());
        }
        return details;
    }
}