package exception;

/**
 * Ngoại lệ khi xác thực đăng nhập thất bại, sai mật khẩu hoặc tài khoản bị khóa.
 */
public class AuthenticationException extends AppException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
