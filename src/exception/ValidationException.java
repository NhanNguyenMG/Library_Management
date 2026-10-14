package exception;

/**
 * Ngoại lệ khi dữ liệu đầu vào không hợp lệ hoặc vi phạm ràng buộc định dạng.
 */
public class ValidationException extends AppException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
