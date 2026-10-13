package tests;

import model.Account;
import service.LoginService;
import service.SessionManager;

/**
 * Kiểm thử đơn vị cho UC-01 Đăng nhập & Phân quyền người dùng.
 * Bao phủ TC-01 và TC-02 theo Bảng 18 SRS.
 */
public class AuthServiceTest {

    private final LoginService loginService = new LoginService();

    /**
     * TC-01: Đăng nhập hợp lệ với các vai trò trong hệ thống.
     */
    public boolean testValidLogin() {
        try {
            // 1. Quản lý
            Account admin = loginService.login("admin", "admin123");
            boolean adminPass = admin != null && SessionManager.ROLE_MANAGER.equals(admin.getRole());

            // 2. Thủ thư
            Account librarian = loginService.login("thuthu01", "thuthu123");
            boolean libPass = librarian != null && SessionManager.ROLE_LIBRARIAN.equals(librarian.getRole());

            // 3. Sinh viên
            Account student = loginService.login("sv001", "123456");
            boolean stuPass = student != null && SessionManager.ROLE_STUDENT.equals(student.getRole());

            SessionManager.getInstance().logout();
            return adminPass && libPass && stuPass;
        } catch (Exception e) {
            System.err.println("[ERROR] [AuthServiceTest.testValidLogin] Ngoại lệ kiểm thử: " + e.getMessage());
            return false;
        }
    }

    /**
     * TC-02: Đăng nhập sai mật khẩu hoặc tài khoản không tồn tại.
     */
    public boolean testInvalidLogin() {
        boolean passWrongPass = false;
        boolean passWrongUser = false;

        try {
            loginService.login("admin", "wrong_password_123");
        } catch (Exception e) {
            passWrongPass = e.getMessage().contains("không chính xác");
        }

        try {
            loginService.login("user_not_exist_999", "123456");
        } catch (Exception e) {
            passWrongUser = e.getMessage().contains("không tồn tại");
        }

        return passWrongPass && passWrongUser;
    }

    public static void main(String[] args) {
        AuthServiceTest test = new AuthServiceTest();
        System.out.println("[TEST] TC-01 (Xác thực đăng nhập tài khoản hợp lệ theo vai trò): " + (test.testValidLogin() ? "[ PASSED ]" : "[ FAILED ]"));
        System.out.println("[TEST] TC-02 (Xử lý từ chối thông tin đăng nhập không hợp lệ): " + (test.testInvalidLogin() ? "[ PASSED ]" : "[ FAILED ]"));
    }
}
