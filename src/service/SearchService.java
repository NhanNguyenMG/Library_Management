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
     * Lấy toàn bộ danh sách tài liệu có trong thư viện.
     */
    public List<Book> getAllBooks() throws SQLException {
        return bookRepository.getAllBooks();
    }

    /**
     * Tra cứu danh sách tài liệu theo từ khóa.
     * Nếu từ khóa rỗng hoặc null, hệ thống tự động trả về toàn bộ sách trong thư viện.
     */
    public List<Book> searchBooks(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBooks();
        }

        return bookRepository.searchBooks(keyword.trim());
    }
}