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
     * =========================================================================
     * Use Case: UC-02 Tra cứu tài liệu
     * Traceability: UI gửi từ khóa → SearchController → SearchService
     * =========================================================================
     */
    public List<Book> searchBooks(String keyword)
            throws SQLException, IllegalArgumentException {

        return searchService.searchBooks(keyword);
    }
}