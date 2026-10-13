package service;

import java.util.Date;

/**
 * Quản lý phiên làm việc của người dùng hiện tại theo mẫu thiết kế Singleton.
 */
public class SessionManager {

    public static final String ROLE_MANAGER   = "QUAN_LI";
    public static final String ROLE_LIBRARIAN = "THU_THU";
    public static final String ROLE_STUDENT   = "SINH_VIEN";

    private static SessionManager instance;

    private boolean loggedIn = false;
    private String username;
    private String role;
    private String userId;
    private String fullName;
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
     * @return SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Thiết lập phiên làm việc khi người dùng xác thực thành công.
     * 
     * @param username tên đăng nhập
     * @param role vai trò người dùng (QUAN_LI, THU_THU, SINH_VIEN)
     * @param userId mã định danh người dùng (MSSV, mã nhân viên hoặc mã quản lý)
     * @param fullName họ tên hiển thị
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
        System.out.println("[INFO] [SessionManager] Khởi tạo phiên: " + username + " (" + this.role + ")");
    }

    /**
     * Đăng xuất và hủy phiên làm việc hiện tại.
     */
    public synchronized void logout() {
        System.out.println("[INFO] [SessionManager] Hủy phiên người dùng: " + this.username);
        clearSession();
    }

    /**
     * Đặt lại toàn bộ thông tin phiên về mặc định.
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
     * Kiểm tra trạng thái đăng nhập.
     * 
     * @return true nếu phiên đang hoạt động, ngược lại false
     */
    public synchronized boolean isLoggedIn() {
        return this.loggedIn;
    }

    /**
     * Kiểm tra người dùng hiện tại có vai trò tương ứng hay không.
     * 
     * @param requiredRole vai trò cần kiểm tra
     * @return true nếu vai trò trùng khớp, ngược lại false
     */
    public synchronized boolean hasRole(String requiredRole) {
        if (!isLoggedIn() || this.role == null || requiredRole == null) {
            return false;
        }
        return this.role.equalsIgnoreCase(requiredRole.trim());
    }

    public boolean isManager() {
        return hasRole(ROLE_MANAGER);
    }

    public boolean isLibrarian() {
        return hasRole(ROLE_LIBRARIAN);
    }

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
     * Định dạng chuỗi hiển thị thông tin người dùng kèm vai trò trên giao diện.
     * 
     * @return chuỗi định danh người dùng và vai trò
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
