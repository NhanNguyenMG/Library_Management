package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Lớp DBConnection quản lý kết nối đến cơ sở dữ liệu MySQL.
 * 
 * Áp dụng thiết kế Singleton Pattern:
 * - Đảm bảo chỉ có DUY NHẤT một đối tượng DBConnection trong toàn bộ vòng đời của ứng dụng.
 * - Tiết kiệm tài nguyên bộ nhớ và hạn chế tối đa việc mở quá nhiều kết nối đến MySQL Server (Connection Leak).
 * 
 * @author Người số 5 (Hạ tầng chung & CSDL)
 */
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

    /**
     * Tải cấu hình từ file db.properties.
     * Thứ tự ưu tiên tìm kiếm:
     * 1. Classpath (resources)
     * 2. Thư mục hiện tại (project root)
     * 3. Thư mục src/db/db.properties
     */
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
            System.out.println("[DBConnection] Successfully loaded configuration from db.properties.");
        } else {
            System.out.println("[DBConnection] db.properties not found, using default configuration (root@localhost:3306).");
        }
    }

    /**
     * Nạp MySQL JDBC Driver vào bộ nhớ JVM.
     */
    private void initDriver() {
        try {
            Class.forName(driver);
            System.out.println("[DBConnection] Successfully loaded MySQL JDBC Driver: " + driver);
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection ERROR] MySQL JDBC Driver not found!");
            System.err.println("-> Guide: Ensure mysql-connector-j-x.x.x.jar is added to the project classpath / lib.");
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
                System.out.println("[DBConnection] Successfully connected to database: " + url.split("\\?")[0]);
            } catch (SQLException e) {
                System.err.println("[DBConnection ERROR] Unable to connect to MySQL Server!");
                System.err.println("-> Check: 1. Is MySQL service running (XAMPP / MySQL Service)?");
                System.err.println("         2. Are username and password correct in db.properties?");
                System.err.println("         3. Has database 'quan_li_thu_vien' been created?");
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
                    System.out.println("[DBConnection] Database connection closed safely.");
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
        System.out.println("=== TEST MYSQL DATABASE CONNECTION ===");
        DBConnection db = DBConnection.getInstance();
        boolean success = db.testConnection(5);
        if (success) {
            System.out.println(">>> RESULT: Successfully connected to database 'quan_li_thu_vien'! <<<");
        } else {
            System.err.println(">>> RESULT: Connection failed. Please check MySQL service and credentials. <<<");
        }
    }
}
