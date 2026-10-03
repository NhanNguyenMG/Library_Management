package service;

import java.util.Date;

/**
 * Lớp SessionManager quản lý phiên đăng nhập của người dùng hiện tại trong toàn bộ ứng dụng.
 * 
 * Áp dụng Singleton Pattern:
 * - Lưu trữ thông tin tài khoản đang làm việc trong bộ nhớ RAM (In-Memory).
 * - Cung cấp thông tin nhân viên/sinh viên hiện tại cho tất cả các Form (UI), Controller và Service.
 * - Giúp phân quyền truy cập chức năng theo vai trò (QUAN_LI, THU_THU, SINH_VIEN).
 * 
 * @author Người số 5 (Hạ tầng chung & CSDL)
 */
public class SessionManager {

    // 1. Thể hiện duy nhất của SessionManager (Singleton)
    private static SessionManager instance;

    // 2. Thông tin phiên làm việc
    private boolean loggedIn = false;
    private String username;
    private String role;          // QUAN_LI, THU_THU, SINH_VIEN
    private String userId;        // mssv, ma_nhan_vien, hoặc ma_quan_li
    private String fullName;      // Họ và tên hiển thị trên giao diện
    private String email;
    private Date loginTime;

    /**
     * Constructor private để bảo vệ tính toàn vẹn của Singleton.
     */
    private SessionManager() {
        clearSession();
    }

    /**
     * Lấy thể hiện duy nhất của SessionManager (Thread-Safe).
     * 
     * @return SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Thiết lập phiên làm việc khi người dùng đăng nhập thành công.
     * 
     * @param username tên đăng nhập
     * @param role vai trò (QUAN_LI, THU_THU, SINH_VIEN)
     * @param userId mã định danh (MSSV hoặc mã nhân viên)
     * @param fullName họ tên đầy đủ
     * @param email địa chỉ email
     */
    public synchronized void login(String username, String role, String userId, String fullName, String email) {
        this.loggedIn = true;
        this.username = username;
        this.role = (role != null) ? role.trim().toUpperCase() : "";
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.loginTime = new Date();
        System.out.println("[SessionManager] User logged in successfully: " + fullName + " (" + role + ") at " + loginTime);
    }

    /**
     * Đăng xuất khỏi hệ thống, xóa sạch thông tin phiên làm việc.
     */
    public synchronized void logout() {
        System.out.println("[SessionManager] User '" + this.username + "' has logged out.");
        clearSession();
    }

    /**
     * Xóa dữ liệu phiên.
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
    // CÁC HÀM KIỂM TRA QUYỀN VÀ TRẠNG THÁI (AUTHORIZATION & STATE)
    // =========================================================================

    /**
     * Kiểm tra xem người dùng đã đăng nhập hay chưa.
     */
    public synchronized boolean isLoggedIn() {
        return this.loggedIn;
    }

    /**
     * Kiểm tra xem người dùng hiện tại có vai trò tương ứng hay không.
     * 
     * @param requiredRole vai trò cần kiểm tra (ví dụ: "THU_THU")
     * @return true nếu trùng khớp vai trò
     */
    public synchronized boolean hasRole(String requiredRole) {
        if (!isLoggedIn() || this.role == null || requiredRole == null) {
            return false;
        }
        return this.role.equalsIgnoreCase(requiredRole.trim());
    }

    /**
     * Kiểm tra có phải là Quản lý không.
     */
    public boolean isManager() {
        return hasRole("QUAN_LI");
    }

    /**
     * Kiểm tra có phải là Thủ thư không.
     */
    public boolean isLibrarian() {
        return hasRole("THU_THU");
    }

    /**
     * Kiểm tra có phải là Sinh viên không.
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
        return fullName != null ? fullName : "Guest";
    }

    public synchronized String getEmail() {
        return email;
    }

    public synchronized Date getLoginTime() {
        return loginTime;
    }

    /**
     * Lấy chuỗi định dạng thông tin người dùng đang đăng nhập để hiển thị trên tiêu đề giao diện Desktop.
     * Ví dụ: "Tran Thi Mai (Librarian - TT0001)"
     */
    public synchronized String getDisplayNameWithRole() {
        if (!loggedIn) {
            return "Not logged in";
        }
        String roleName = role;
        if ("THU_THU".equals(role)) roleName = "Librarian";
        else if ("QUAN_LI".equals(role)) roleName = "Manager";
        else if ("SINH_VIEN".equals(role)) roleName = "Student";

        return fullName + " (" + roleName + (userId != null ? " - " + userId : "") + ")";
    }
}
