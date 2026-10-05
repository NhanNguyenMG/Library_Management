package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Account;
import db.DBConnection;

public class AccountRepository {

    public Account findByUsername(String username) {
        String sql = "SELECT * FROM tai_khoan WHERE ten_dang_nhap = ?";
        Account account = null;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username); // Chống SQL Injection

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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return account;
    }

    /**
     * Lấy thêm thông tin chi tiết (ID, Họ tên, Email) từ các bảng phân quyền
     * dựa vào tên đăng nhập và vai trò.
     */

    public String[] getUserDetails(String username, String role) {
        // Mảng lưu 3 giá trị: [0] = userId, [1] = fullName, [2] = email
        String[] details = new String[3];
        String sql = "";

        // Tùy theo vai trò để query đúng bảng (Thủ thư không có cột email trong CSDL)
        if ("QUAN_LI".equals(role)) {
            sql = "SELECT ma_quan_li as id, ho_ten as name, email FROM quan_li WHERE ten_dang_nhap = ?";
        } else if ("THU_THU".equals(role)) {
            sql = "SELECT ma_nhan_vien as id, ho_ten as name, NULL as email FROM thu_thu WHERE ten_dang_nhap = ?";
        } else if ("SINH_VIEN".equals(role)) {
            sql = "SELECT mssv as id, ho_ten as name, email FROM sinh_vien WHERE ten_dang_nhap = ?";
        } else {
            return details;
        }

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    details[0] = rs.getString("id");
                    details[1] = rs.getString("name");
                    details[2] = rs.getString("email");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }
}