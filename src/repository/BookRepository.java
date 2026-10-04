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
                SELECT ma_dau_sach, tac_sach, tac_gia, so_luong_con
                FROM dau_sach
                WHERE ma_dau_sach LIKE ?
                   OR tac_sach LIKE ?
                   OR tac_gia LIKE ?
                ORDER BY tac_sach
                """;

        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            String searchKeyword = "%" + keyword.trim() + "%";

            preparedStatement.setString(1, searchKeyword);
            preparedStatement.setString(2, searchKeyword);
            preparedStatement.setString(3, searchKeyword);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {

                    Book book = new Book(
                            resultSet.getString("ma_dau_sach"),
                            resultSet.getString("tac_sach"),
                            resultSet.getString("tac_gia"),
                            resultSet.getInt("so_luong_con")
                    );

                    books.add(book);
                }
            }
        }

        return books;
    }
}