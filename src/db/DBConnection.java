package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    // 1. Biến static duy nhất lưu trữ thể hiện (instance) của lớp
    private static DBConnection instance;

    // 2. Đối tượng kết nối JDBC
    private Connection connection;

    // 3. Các thông số cấu hình mặc định (fallback khi không tìm thấy file cấu hình)
    private String driver = "com.mysql.cj.jdbc.Driver";
    private String url = "jdbc:mysql://localhost:3306/quan_li_thu_vien?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";
    private String user = "root";
    private String password = "";

    /**
     * Constructor đặt ở chế độ private để ngăn chặn việc tạo đối tượng tùy tiện từ bên ngoài bằng từ khóa 'new'.
     */
    private DBConnection() {
        loadConfiguration();
        initDriver();
    }


    private void loadConfiguration() {
        Properties props = new Properties();
        boolean loaded = false;

        // Thử tìm trong classpath
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db/db.properties")) {
            if (is != null) {
                props.load(is);
                loaded = true;
            }
        } catch (Exception ignored) {}

        if (!loaded) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
                if (is != null) {
                    props.load(is);
                    loaded = true;
                }
            } catch (Exception ignored) {}
        }

        // Thử tìm theo đường dẫn file vật lý
        if (!loaded) {
            File[] files = new File[] {
                new File("db.properties"),
                new File("src/db/db.properties"),
                new File("LibraryManagement/db.properties"),
                new File("LibraryManagement/src/db/db.properties")
            };
            for (File file : files) {
                if (file.exists()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        props.load(fis);
                        loaded = true;
                        break;
                    } catch (Exception ignored) {}
                }
            }
        }

        if (loaded) {
            if (props.getProperty("db.driver") != null) this.driver = props.getProperty("db.driver").trim();
            if (props.getProperty("db.url") != null) this.url = props.getProperty("db.url").trim();
            if (props.getProperty("db.user") != null) this.user = props.getProperty("db.user").trim();
            if (props.getProperty("db.password") != null) this.password = props.getProperty("db.password").trim();
            System.out.println("[DBConnection] Đã tải thành công cấu hình kết nối từ db.properties.");
        } else {
            System.out.println("[DBConnection] Không tìm thấy db.properties, sử dụng cấu hình mặc định (root@localhost:3306).");
        }
    }

    /**
     * Nạp MySQL JDBC Driver vào bộ nhớ JVM.
     */
    private void initDriver() {
        try {
            Class.forName(driver);
            System.out.println("[DBConnection] Đã nạp thành công MySQL JDBC Driver: " + driver);
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection LỖI] Không tìm thấy Driver MySQL JDBC!");
            System.err.println("-> Hướng dẫn: Đảm bảo file mysql-connector-j-8.3.0.jar đã được thêm vào Classpath / lib của dự án.");
            e.printStackTrace();
        }
    }

    /**
     * Phương thức tĩnh cung cấp điểm truy cập toàn cục tới thể hiện duy nhất của lớp (Thread-Safe).
     * 
     * @return DBConnection đối tượng duy nhất
     */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Lấy kết nối JDBC đang hoạt động.
     * Nếu kết nối chưa mở hoặc đã bị đóng, tự động khởi tạo kết nối mới.
     * 
     * @return Connection đối tượng kết nối SQL
     * @throws SQLException nếu kết nối thất bại
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("[DBConnection] Mở kết nối thành công tới database: " + url.split("\\?")[0]);
            } catch (SQLException e) {
                System.err.println("[DBConnection LỖI] Không thể kết nối tới MySQL Server!");
                System.err.println("-> Kiểm tra: 1. MySQL đã được khởi động chưa (XAMPP / MySQL Service)?");
                System.err.println("            2. Tên đăng nhập và mật khẩu trong db.properties đã đúng chưa?");
                System.err.println("            3. Database 'quan_li_thu_vien' đã được tạo qua setup_database.sql chưa?");
                throw e;
            }
        }
        return connection;
    }

    /**
     * Kiểm tra trạng thái kết nối tới CSDL.
     * 
     * @param timeoutSeconds thời gian tối đa chờ phản hồi (giây)
     * @return true nếu kết nối hoạt động bình thường, false nếu thất bại
     */
    public boolean testConnection(int timeoutSeconds) {
        try {
            Connection conn = getConnection();
            return conn != null && conn.isValid(timeoutSeconds);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Đóng kết nối an toàn khi tắt ứng dụng.
     */
    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[DBConnection] Đã đóng kết nối CSDL an toàn.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Hàm main hỗ trợ chạy kiểm thử nhanh kết nối CSDL trực tiếp từ IDE hoặc dòng lệnh.
     */
    public static void main(String[] args) {
        System.out.println("=== KIỂM THỬ KẾT NỐI CƠ SỞ DỮ LIỆU MYSQL ===");
        DBConnection db = DBConnection.getInstance();
        boolean success = db.testConnection(5);
        if (success) {
            System.out.println(">>> KẾT QUẢ: KẾT NỐI TỚI DATABASE 'quan_li_thu_vien' THÀNH CÔNG RỰC RỠ! <<<");
        } else {
            System.err.println(">>> KẾT QUẢ: KẾT NỐI THẤT BẠI. Hãy kiểm tra dịch vụ MySQL và mật khẩu. <<<");
        }
    }
}
