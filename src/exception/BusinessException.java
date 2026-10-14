package exception;

/**
 * Ngoại lệ khi vi phạm các quy tắc nghiệp vụ (vượt hạn mức mượn, còn nợ phạt, hết sách trong kho,...).
 */
public class BusinessException extends AppException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
