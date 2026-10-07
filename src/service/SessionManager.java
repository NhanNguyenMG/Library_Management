package service;

import java.util.Date;

/**
 * Quản lý phiên làm việc của người dùng hiện tại
 */
public class SessionManager {

    // Các hằng số vai trò hệ thống
    public static final String ROLE_MANAGER   = "QUAN_LI";
    public static final String ROLE_LIBRARIAN = "THU_THU";
    public static final String ROLE_STUDENT   = "SINH_VIEN";

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
     * Thiết lập phiên làm việc khi người dùng đăng nhập thành công (gọi từ LoginService).
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
        System.out.println("[SessionManager] Người dùng đăng nhập thành công: " + fullName + " (" + this.role + ") lúc " + loginTime);
    }

    /**
     * Đăng xuất khỏi hệ thống, xóa sạch thông tin phiên làm việc.
     */
    public synchronized void logout() {
        System.out.println("[SessionManager] Người dùng '" + this.username + "' đã đăng xuất.");
        clearSession();
    }

    /**
     * Xóa dữ liệu phiên làm việc nội bộ.
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

    /**
     * Kiểm tra xem người dùng đã đăng nhập hay chưa.
     */
    public synchronized boolean isLoggedIn() {
        return this.loggedIn;
    }

    /**
     * Kiểm tra xem người dùng hiện tại có vai trò tương ứng hay không.
     * 
     * @param requiredRole vai trò cần kiểm tra (ví dụ: SessionManager.ROLE_LIBRARIAN)
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
        return hasRole(ROLE_MANAGER);
    }

    /**
     * Kiểm tra có phải là Thủ thư không.
     */
    public boolean isLibrarian() {
        return hasRole(ROLE_LIBRARIAN);
    }

    /**
     * Kiểm tra có phải là Sinh viên không.
     */
    public boolean isStudent() {
        return hasRole(ROLE_STUDENT);
    }

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
     * Lấy chuỗi định dạng thông tin người dùng đang đăng nhập để hiển thị trên tiêu đề giao diện Desktop.
     * Ví dụ: "Trần Thị Mai (Thủ thư - TT0001)"
     */
    public synchronized String getDisplayNameWithRole() {
        if (!loggedIn) {
            return "Chưa đăng nhập";
        }
        String roleName = role;
        if (ROLE_LIBRARIAN.equals(role)) roleName = "Thủ thư";
        else if (ROLE_MANAGER.equals(role)) roleName = "Quản lý";
        else if (ROLE_STUDENT.equals(role)) roleName = "Sinh viên";

        return fullName + " (" + roleName + (userId != null ? " - " + userId : "") + ")";
    }
}
