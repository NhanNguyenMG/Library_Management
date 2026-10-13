package tests;

import model.Book;
import service.SearchService;

import java.util.List;

/**
 * Kiểm thử cho UC-02 Tra cứu tài liệu.
 * Bao phủ TC-03, TC-04, TC-05 theo Bảng 18 SRS.
 */
public class SearchServiceTest {

    private final SearchService searchService = new SearchService();

    /**
     * TC-03: Tra cứu theo mã hoặc từ khóa tồn tại.
     */
    public boolean testSearchExistingBook() {
        try {
            List<Book> books = searchService.searchBooks("Java");
            return books != null && !books.isEmpty();
        } catch (Exception e) {
            System.err.println("[ERROR] [SearchServiceTest.testSearchExistingBook] Ngoại lệ kiểm thử: " + e.getMessage());
            return false;
        }
    }

    /**
     * TC-04: Kiểm tra chống tấn công SQL Injection bằng PreparedStatement.
     */
    public boolean testSqlInjectionSafe() {
        try {
            // Chuỗi tấn công SQL Injection kinh điển
            List<Book> books = searchService.searchBooks("' OR 1=1 --");
            // Chuỗi không có trong DB nên danh sách trả về phải rỗng và không bị crash
            return books != null && books.isEmpty();
        } catch (Exception e) {
            System.err.println("[ERROR] [SearchServiceTest.testSqlInjectionSafe] Ngoại lệ kiểm thử: " + e.getMessage());
            return false;
        }
    }

    /**
     * TC-05: Tra cứu từ khóa không tồn tại trong thư viện.
     */
    public boolean testSearchNonExistentBook() {
        try {
            List<Book> books = searchService.searchBooks("MaNganhChuaCoSach_X999");
            return books != null && books.isEmpty();
        } catch (Exception e) {
            System.err.println("[ERROR] [SearchServiceTest.testSearchNonExistentBook] Ngoại lệ kiểm thử: " + e.getMessage());
            return false;
        }
    }

    /**
     * Test hiển thị toàn bộ danh sách sách trong thư viện (phục vụ người dùng xem danh mục).
     */
    public boolean testGetAllBooks() {
        try {
            List<Book> books = searchService.getAllBooks();
            return books != null && !books.isEmpty();
        } catch (Exception e) {
            System.err.println("[ERROR] [SearchServiceTest.testGetAllBooks] Ngoại lệ kiểm thử: " + e.getMessage());
            return false;
        }
    }

    /**
     * Test tra cứu với từ khóa rỗng -> Trả về toàn bộ sách thay vì báo lỗi.
     */
    public boolean testSearchEmptyKeywordReturnsAllBooks() {
        try {
            List<Book> allBooks = searchService.getAllBooks();
            List<Book> emptySearchBooks = searchService.searchBooks("");
            return emptySearchBooks != null && emptySearchBooks.size() == allBooks.size();
        } catch (Exception e) {
            System.err.println("[ERROR] [SearchServiceTest.testSearchEmptyKeywordReturnsAllBooks] Ngoại lệ kiểm thử: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        SearchServiceTest test = new SearchServiceTest();
        System.out.println("[TEST] TC-03 (Tra cứu tài liệu tồn tại trong cơ sở dữ liệu): " + (test.testSearchExistingBook() ? "[ PASSED ]" : "[ FAILED ]"));
        System.out.println("[TEST] TC-04 (Kiểm tra an toàn truy vấn SQL Injection): " + (test.testSqlInjectionSafe() ? "[ PASSED ]" : "[ FAILED ]"));
        System.out.println("[TEST] TC-05 (Tra cứu tài liệu không tồn tại): " + (test.testSearchNonExistentBook() ? "[ PASSED ]" : "[ FAILED ]"));
        System.out.println("[TEST] TC-EXT-01 (Truy vấn danh mục toàn bộ tài liệu): " + (test.testGetAllBooks() ? "[ PASSED ]" : "[ FAILED ]"));
        System.out.println("[TEST] TC-EXT-02 (Tra cứu từ khóa rỗng - Trả về toàn bộ danh mục): " + (test.testSearchEmptyKeywordReturnsAllBooks() ? "[ PASSED ]" : "[ FAILED ]"));
    }
}
