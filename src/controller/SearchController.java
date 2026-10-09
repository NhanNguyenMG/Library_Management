package controller;

import model.Book;
import service.SearchService;

import java.sql.SQLException;
import java.util.List;

public class SearchController {

    private final SearchService searchService;

    public SearchController() {
        this.searchService = new SearchService();
    }

    /**
     * Tra cứu tài liệu theo từ khóa
     */
    public List<Book> searchBooks(String keyword)
            throws SQLException, IllegalArgumentException {

        return searchService.searchBooks(keyword);
    }

    /**
     * Lấy toàn bộ danh sách sách có trong thư viện
     */
    public List<Book> getAllBooks() throws SQLException {
        return searchService.getAllBooks();
    }
}