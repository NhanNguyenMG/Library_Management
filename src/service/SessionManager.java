package service;

import java.util.Date;

/**
 * Quản lý phiên làm việc của người dùng trong hệ thống (In-Memory Session Manager).
 * Áp dụng mẫu thiết kế Singleton để duy trì trạng thái đăng nhập và phân quyền trên toàn ứng dụng.
 */
public class SessionManager {

    // Thể hiện duy nhất của lớp (Singleton instance)
    private static SessionManager instance;

    // Thông tin trạng thái phiên làm việc
    private boolean loggedIn = false;
    private String username;
    private String role;          // QUAN_LI, THU_THU, SINH_VIEN
    private String userId;        // MSSV hoặc mã nhân viên
    private String fullName;      // Họ tên hiển thị
    private String email;
    private Date loginTime;

    /**
     * Khởi tạo đối tượng với phạm vi private theo mẫu Singleton.
     */
    private SessionManager() {
        clearSession();
    }

    /**
     * Lấy thể hiện duy nhất của SessionManager (Thread-Safe).
     * 
     * @return SessionManager thể hiện quản lý phiên làm việc
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Khởi tạo phiên làm việc khi người dùng xác thực thành công.
     * 
     * @param username tên tài khoản đăng nhập
     * @param role vai trò người dùng (QUAN_LI, THU_THU, SINH_VIEN)
     * @param userId mã định danh tài khoản (MSSV hoặc mã nhân viên)
     * @param fullName họ tên đầy đủ
     * @param email địa chỉ thư điện tử
     */
    public synchronized void login(String username, String role, String userId, String fullName, String email) {
        this.loggedIn = true;
        this.username = username;
        this.role = (role != null) ? role.trim().toUpperCase() : "";
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.loginTime = new Date();
        System.out.println("[SessionManager] Người dùng đăng nhập: " + fullName + " (" + role + ") lúc " + loginTime);
    }

    /**
     * Kết thúc phiên làm việc hiện tại và xóa dữ liệu phiên.
     */
    public synchronized void logout() {
        System.out.println("[SessionManager] Người dùng '" + this.username + "' đã đăng xuất.");
        clearSession();
    }

    /**
     * Đặt lại trạng thái phiên làm việc về mặc định.
     */
    private void clearSession() {
        this.loggedIn = false;
        this.username = null;
        this.role = null;
        this.userId = null;
        this.fullName = null;
        this.email = null;
        this.loginTime = null;
    }

    // =========================================================================
    // PHƯƠNG THỨC KIỂM TRA QUYỀN VÀ TRẠNG THÁI (AUTHORIZATION & STATE)
    // =========================================================================

    /**
     * Kiểm tra trạng thái đăng nhập của người dùng.
     * 
     * @return true nếu đã đăng nhập, ngược lại false
     */
    public synchronized boolean isLoggedIn() {
        return this.loggedIn;
    }

    /**
     * Kiểm tra vai trò của người dùng hiện tại có khớp với vai trò yêu cầu hay không.
     * 
     * @param requiredRole vai trò cần kiểm tra
     * @return true nếu người dùng có vai trò tương ứng, ngược lại false
     */
    public synchronized boolean hasRole(String requiredRole) {
        if (!isLoggedIn() || this.role == null || requiredRole == null) {
            return false;
        }
        return this.role.equalsIgnoreCase(requiredRole.trim());
    }

    /**
     * Kiểm tra vai trò Quản lý hệ thống.
     */
    public boolean isManager() {
        return hasRole("QUAN_LI");
    }

    /**
     * Kiểm tra vai trò Thủ thư thư viện.
     */
    public boolean isLibrarian() {
        return hasRole("THU_THU");
    }

    /**
     * Kiểm tra vai trò Sinh viên / Độc giả.
     */
    public boolean isStudent() {
        return hasRole("SINH_VIEN");
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public synchronized String getUsername() {
        return username;
    }

    public synchronized String getRole() {
        return role;
    }

    public synchronized String getUserId() {
        return userId;
    }

    public synchronized String getFullName() {
        return fullName != null ? fullName : "Khách";
    }

    public synchronized String getEmail() {
        return email;
    }

    public synchronized Date getLoginTime() {
        return loginTime;
    }

    /**
     * Định dạng chuỗi hiển thị thông tin người dùng kèm vai trò trên thanh tiêu đề ứng dụng.
     * 
     * @return chuỗi định dạng tên và vai trò
     */
    public synchronized String getDisplayNameWithRole() {
        if (!loggedIn) {
            return "Chưa đăng nhập";
        }
        String roleName = role;
        if ("THU_THU".equals(role)) roleName = "Thủ thư";
        else if ("QUAN_LI".equals(role)) roleName = "Quản lý";
        else if ("SINH_VIEN".equals(role)) roleName = "Sinh viên";

        return fullName + " (" + roleName + (userId != null ? " - " + userId : "") + ")";
    }
}
