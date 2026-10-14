package exception;

/**
 * Ngoại lệ khi xảy ra lỗi trong quá trình truy xuất hoặc thao tác với cơ sở dữ liệu ở tầng Repository.
 */
public class RepositoryException extends AppException {

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
