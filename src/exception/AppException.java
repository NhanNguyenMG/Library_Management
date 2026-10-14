package exception;

/**
 * Lớp ngoại lệ cơ sở cho toàn bộ hệ thống Thư viện (Base Application Exception).
 * Kế thừa RuntimeException để hỗ trợ unchecked exception
 */
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
