package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quản lý kết nối cơ sở dữ liệu MySQL theo mẫu thiết kế Singleton.
 * Đảm bảo khởi tạo và tái sử dụng duy nhất một kết nối JDBC trong suốt vòng đời ứng dụng.
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
     * Tải thông số cấu hình kết nối từ tệp db.properties.
     */
    private void loadConfiguration() {
        Properties props = new Properties();
        boolean loaded = false;

        // Tìm kiếm tệp cấu hình trong classpath
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

        // Tìm kiếm tệp cấu hình theo đường dẫn tương đối trong dự án
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
            System.out.println("[DBConnection] Tải cấu hình từ db.properties thành công.");
        } else {
            System.out.println("[DBConnection] Không tìm thấy db.properties, sử dụng cấu hình mặc định (root@localhost:3306).");
        }
    }

    /**
     * Nạp MySQL JDBC Driver vào bộ nhớ.
     */
    private void initDriver() {
        try {
            Class.forName(driver);
            System.out.println("[DBConnection] Đã nạp MySQL JDBC Driver: " + driver);
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection][LỖI] Không tìm thấy Driver MySQL JDBC: " + driver);
            System.err.println("[DBConnection][LỖI] Vui lòng đảm bảo mysql-connector-j đã được thêm vào Classpath / lib của dự án.");
            e.printStackTrace();
        }
    }

    /**
     * Lấy thể hiện duy nhất của lớp DBConnection (Thread-Safe).
     * 
     * @return DBConnection đối tượng quản lý kết nối
     */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Lấy đối tượng kết nối JDBC hiện thời.
     * Tự động khởi tạo kết nối mới nếu kết nối chưa mở hoặc đã bị đóng.
     * 
     * @return Connection đối tượng kết nối cơ sở dữ liệu
     * @throws SQLException khi xảy ra lỗi kết nối cơ sở dữ liệu
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("[DBConnection] Kết nối thành công tới CSDL: " + url.split("\\?")[0]);
            } catch (SQLException e) {
                System.err.println("[DBConnection][LỖI] Không thể kết nối tới MySQL Server (" + url.split("\\?")[0] + ")");
                System.err.println("[DBConnection][LỖI] Nguyên nhân: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    /**
     * Kiểm tra trạng thái hoạt động của kết nối cơ sở dữ liệu.
     * 
     * @param timeoutSeconds thời gian chờ tối đa (giây)
     * @return true nếu kết nối hợp lệ, false nếu thất bại
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
     * Đóng kết nối cơ sở dữ liệu an toàn khi ứng dụng kết thúc.
     */
    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[DBConnection] Đã đóng kết nối CSDL.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Phương thức hỗ trợ kiểm tra nhanh trạng thái kết nối cơ sở dữ liệu độc lập.
     */
    public static void main(String[] args) {
        System.out.println("[DBConnection] Kiểm tra kết nối cơ sở dữ liệu MySQL...");
        DBConnection db = DBConnection.getInstance();
        boolean success = db.testConnection(5);
        if (success) {
            System.out.println("[DBConnection] Kết nối cơ sở dữ liệu 'quan_li_thu_vien' thành công.");
        } else {
            System.err.println("[DBConnection][LỖI] Kết nối thất bại. Vui lòng kiểm tra lại dịch vụ MySQL và cấu hình kết nối.");
        }
    }
}
