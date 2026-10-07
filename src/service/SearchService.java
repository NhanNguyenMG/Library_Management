package service;

import model.Book;
import repository.BookRepository;

import java.sql.SQLException;
import java.util.List;

public class SearchService {

    private final BookRepository bookRepository;

    public SearchService() {
        this.bookRepository = new BookRepository();
    }

    /**
     * Tra cứu danh sách tài liệu theo từ khóa
     */
    public List<Book> searchBooks(String keyword)
            throws SQLException {

        if (keyword == null
                || keyword.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Từ khóa tìm kiếm không được để trống."
            );
        }

        return bookRepository.searchBooks(
                keyword.trim()
        );
    }
}