package service;

import repository.AccountRepository;
import exception.AuthenticationException;
import model.Account;
import util.ValidationUtil;

public class LoginService {

    private final AccountRepository accountRepo = new AccountRepository();

    /**
     * Kiểm tra thông tin đăng nhập của người dùng.
     * Áp dụng ValidationUtil và AuthenticationException
     */
    public Account login(String username, String password) throws AuthenticationException {
        // Kiểm tra tính hợp lệ của dữ liệu đầu vào
        ValidationUtil.validateCredentials(username, password);

        Account account = accountRepo.findByUsername(username.trim());

        // Kiểm tra xem tài khoản có tồn tại không
        if (account == null) {
            throw new AuthenticationException("Tài khoản không tồn tại trong hệ thống.");
        }

        // Kiểm tra mật khẩu
        if (!account.getPassword().equals(password)) {
            throw new AuthenticationException("Mật khẩu xác thực không chính xác.");
        }

        // Kiểm tra trạng thái tài khoản
        if ("LOCKED".equalsIgnoreCase(account.getStatus()) || "INACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new AuthenticationException("Tài khoản đã bị khóa hoặc ngừng hoạt động.");
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