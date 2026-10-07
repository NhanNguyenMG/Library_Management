package repository;

import db.DBConnection;
import model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookRepository {

    /**
     * Tìm kiếm sách theo từ khóa (mã sách, tên sách hoặc tác giả)
     */
    public List<Book> searchBooks(String keyword) throws SQLException {

        List<Book> books = new ArrayList<>();

        String sql = """
                SELECT ma_dau_sach, tac_sach, tac_gia, so_luong_con, mo_ta
                FROM dau_sach
                WHERE ma_dau_sach LIKE ?
                   OR tac_sach LIKE ?
                   OR tac_gia LIKE ?
                ORDER BY tac_sach
                """;

        Connection connection =
                DBConnection.getInstance().getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            String searchKeyword = "%" + keyword.trim() + "%";

            preparedStatement.setString(1, searchKeyword);
            preparedStatement.setString(2, searchKeyword);
            preparedStatement.setString(3, searchKeyword);

            try (ResultSet resultSet =
                         preparedStatement.executeQuery()) {

                while (resultSet.next()) {

                    Book book = new Book(
                            resultSet.getString("ma_dau_sach"),
                            resultSet.getString("tac_sach"),
                            resultSet.getString("tac_gia"),
                            resultSet.getInt("so_luong_con"),
                            resultSet.getString("mo_ta")
                    );

                    books.add(book);
                }
            }
        }

        return books;
    }

    /**
     * Tăng số lượng tồn kho của đầu sách
     *
     * @param bookId mã đầu sách
     * @param quantity số lượng sách tăng thêm khi trả
     * @param conn kết nối Transaction đang mở
     * @return true nếu cập nhật thành công
     * @throws SQLException nếu có lỗi cập nhật
     */
    public boolean increaseStock(String bookId, int quantity, Connection conn) throws SQLException {
        String sql = "UPDATE dau_sach SET so_luong_con = so_luong_con + ? WHERE ma_dau_sach = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, bookId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Tìm thông tin đầu sách theo mã đầu sách.
     *
     * @param bookId mã đầu sách
     * @return Book nếu tìm thấy, null nếu không tồn tại
     * @throws SQLException nếu lỗi truy vấn
     */
    public Book findById(String bookId) throws SQLException {
        String sql = "SELECT ma_dau_sach, tac_sach, tac_gia, so_luong_con, mo_ta FROM dau_sach WHERE ma_dau_sach = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Book(
                            rs.getString("ma_dau_sach"),
                            rs.getString("tac_sach"),
                            rs.getString("tac_gia"),
                            rs.getInt("so_luong_con"),
                            rs.getString("mo_ta")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Giảm số lượng tồn kho đầu sách khi lập phiếu mượn
     */
    public void decreaseStock(String bookId, int quantity, Connection connection) throws SQLException {

        // Điều kiện so_luong_con >= ? đảm bảo tồn kho không bao giờ bị âm
        String sql = """
                UPDATE dau_sach
                SET so_luong_con = so_luong_con - ?
                WHERE ma_dau_sach = ? AND so_luong_con >= ?
                """;

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, quantity);
            preparedStatement.setString(2, bookId);
            preparedStatement.setInt(3, quantity);

            if (preparedStatement.executeUpdate() != 1) {
                throw new SQLException("Đầu sách " + bookId + " không tồn tại hoặc không đủ số lượng để mượn.");
            }
        }
    }
}