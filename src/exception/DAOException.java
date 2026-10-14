package exception;

/**
 * Ngoại lệ khi xảy ra lỗi trong quá trình truy xuất hoặc thao tác với cơ sở dữ liệu.
 * Kế thừa RepositoryException để duy trì tính tương thích.
 */
public class DAOException extends RepositoryException {

    public DAOException(String message) {
        super(message);
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
}
