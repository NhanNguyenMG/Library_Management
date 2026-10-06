package service;

import model.Account;
import repository.AccountRepository;

public class LoginService {

    private final AccountRepository accountRepo = new AccountRepository();

    /**
     * =========================================================================
     * Use Case: UC-01 Đăng nhập
     * Sequence Diagram: sd DangNhap (SRS)
     * Traceability Message: message #1 - loginService.login()
     * Test Case tương ứng: TC-01, TC-02
     * =========================================================================
     */
    public Account login(String username, String password) throws Exception {
        Account account = accountRepo.findByUsername(username);

        // Kiểm tra xem tài khoản có tồn tại không
        if (account == null) {
            throw new Exception("Tài khoản không tồn tại.");
        }

        // Kiểm tra mật khẩu
        if (!account.getPassword().equals(password)) {
            throw new Exception("Mật khẩu không chính xác.");
        }

        // Kiểm tra trạng thái tài khoản
        if ("LOCKED".equalsIgnoreCase(account.getStatus()) || "INACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new Exception("Tài khoản đã bị khóa hoặc không hoạt động.");
        }

        // Tích hợp với SessionManager để lưu thông tin phiên làm việc
        authenticate(account);

        // Trả về thông tin tài khoản nếu đăng nhập thành công
        return account;
    }

    /**
     * Tích hợp với SessionManager để tiến hành phân quyền
     */
    public void authenticate(Account account) {
        // 1. Gọi Repository để lấy thêm mã định danh, họ tên, email
        String[] details = accountRepo.getUserDetails(account.getUsername(), account.getRole());
        String userId = details[0];
        String fullName = details[1];
        String email = details[2];

        // 2. Ghi thông tin vào RAM để toàn hệ thống sử dụng
        SessionManager.getInstance().login(
                account.getUsername(),
                account.getRole(),
                userId,
                fullName,
                email
        );
    }
}