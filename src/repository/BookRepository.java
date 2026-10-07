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
     * =========================================================================
     * Use Case: UC-02 Tra cứu tài liệu
     * Traceability: Truy vấn bảng dau_sach theo mã sách, tên sách hoặc tác giả
     * Database Table: dau_sach
     * =========================================================================
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
     * =========================================================================
     * Use Case: UC-03 Tạo phiếu mượn sách
     * Traceability: Tìm chính xác một đầu sách theo mã (dùng khi kiểm tra sách còn hàng)
     * Database Table: dau_sach
     * =========================================================================
     */
    public Book findById(String bookId) throws SQLException {

        String sql = """
                SELECT ma_dau_sach, tac_sach, tac_gia, so_luong_con, mo_ta
                FROM dau_sach
                WHERE ma_dau_sach = ?
                """;

        Connection connection =
                DBConnection.getInstance().getConnection();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setString(1, bookId.trim());

            try (ResultSet resultSet =
                         preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return new Book(
                            resultSet.getString("ma_dau_sach"),
                            resultSet.getString("tac_sach"),
                            resultSet.getString("tac_gia"),
                            resultSet.getInt("so_luong_con"),
                            resultSet.getString("mo_ta")
                    );
                }
            }
        }

        return null;
    }

    /**
     * =========================================================================
     * Use Case: UC-03 Tạo phiếu mượn sách
     * Traceability: Trừ tồn kho đầu sách khi lập phiếu mượn (chạy trong Transaction của BorrowService)
     * Database Table: dau_sach (so_luong_con)
     * =========================================================================
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