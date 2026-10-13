package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quản lý kết nối cơ sở dữ liệu MySQL theo mẫu Singleton
 */
public class DBConnection {

    // Thể hiện duy nhất của lớp (Singleton instance)
    private static DBConnection instance;

    // Đối tượng kết nối JDBC
    private Connection connection;

    // Cấu hình kết nối cơ sở dữ liệu mặc định
    private String driver = "com.mysql.cj.jdbc.Driver";
    private String url = "jdbc:mysql://localhost:3306/quan_li_thu_vien?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";
    private String user = "root";
    private String password = "";

    /**
     * Khởi tạo đối tượng với phạm vi private theo mẫu Singleton.
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

        // Ưu tiên 1: File cấu hình cục bộ ở thư mục gốc (db.properties)
        File rootFile = new File("db.properties");
        if (rootFile.exists()) {
            try (FileInputStream fis = new FileInputStream(rootFile)) {
                props.load(fis);
                loaded = true;
            } catch (Exception ignored) {}
        }

        // Ưu tiên 2: Classpath resources
        if (!loaded) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("db/db.properties")) {
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
            System.out.println("[INFO] [DBConnection] Đã tải thành công cấu hình kết nối từ tệp db.properties.");
        } else {
            System.out.println("[WARN] [DBConnection] Không tìm thấy tệp db.properties; áp dụng cấu hình kết nối mặc định (root@localhost:3306).");
        }
    }

    /**
     * Nạp MySQL JDBC Driver vào bộ nhớ JVM.
     */
    private void initDriver() {
        try {
            Class.forName(driver);
            System.out.println("[INFO] [DBConnection] Đã nạp thành công MySQL JDBC Driver: " + driver);
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] [DBConnection] Không tìm thấy trình điều khiển MySQL JDBC Driver trong classpath.");
            System.err.println("[ERROR] [DBConnection] Vui lòng đảm bảo tệp thư viện mysql-connector-j đã được thêm vào Classpath/lib của dự án.");
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
                System.out.println("[INFO] [DBConnection] Thiết lập kết nối thành công tới cơ sở dữ liệu: " + url.split("\\?")[0]);
            } catch (SQLException e) {
                System.err.println("[ERROR] [DBConnection] Không thể thiết lập kết nối tới máy chủ cơ sở dữ liệu MySQL.");
                System.err.println("[ERROR] [DBConnection] Vui lòng kiểm tra: Dịch vụ MySQL đang hoạt động, thông tin tài khoản tại db.properties và cơ sở dữ liệu 'quan_li_thu_vien'.");
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
                    System.out.println("[INFO] [DBConnection] Đã đóng kết nối cơ sở dữ liệu an toàn.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Hàm main hỗ trợ kiểm tra nhanh kết nối CSDL khi cần thiết.
     */
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("[DIAGNOSTIC] Kiểm tra kết nối cơ sở dữ liệu MySQL");
        System.out.println("=================================================");
        DBConnection db = DBConnection.getInstance();
        boolean success = db.testConnection(5);
        if (success) {
            System.out.println("[SUCCESS] Kết nối đến cơ sở dữ liệu thành công.");
        } else {
            System.err.println("[FAILED] Kết nối cơ sở dữ liệu thất bại. Vui lòng kiểm tra dịch vụ MySQL và thông số xác thực.");
        }
    }
}
