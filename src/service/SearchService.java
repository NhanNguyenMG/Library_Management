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
     * =========================================================================
     * Use Case: UC-02 Tra cứu tài liệu
     * Traceability: SearchController → SearchService → BookRepository
     * Business Rule: Từ khóa không được rỗng
     * =========================================================================
     */
    public List<Book> searchBooks(String keyword) throws SQLException {

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Từ khóa tìm kiếm không được để trống."
            );
        }

        return bookRepository.searchBooks(keyword.trim());
    }
}