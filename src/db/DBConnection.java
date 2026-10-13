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
 */
public class DBConnection {

    private static DBConnection instance;
    private Connection connection;

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
     * Nạp cấu hình kết nối từ tệp db.properties.
     * Thứ tự ưu tiên:
     * 1. Đường dẫn tệp vật lý (db.properties, src/db/db.properties)
     * 2. Classpath resource (db/db.properties, db.properties)
     */
    private void loadConfiguration() {
        Properties props = new Properties();
        boolean loaded = false;

        String[] candidatePaths = {"db.properties", "src/db/db.properties"};
        for (String path : candidatePaths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    props.load(fis);
                    loaded = true;
                    break;
                } catch (Exception ignored) {}
            }
        }

        if (!loaded) {
            String[] resourcePaths = {"db/db.properties", "db.properties"};
            for (String res : resourcePaths) {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(res)) {
                    if (is != null) {
                        props.load(is);
                        loaded = true;
                        break;
                    }
                } catch (Exception ignored) {}
            }
        }

        if (loaded) {
            if (props.getProperty("db.driver") != null) this.driver = props.getProperty("db.driver").trim();
            if (props.getProperty("db.url") != null) this.url = props.getProperty("db.url").trim();
            if (props.getProperty("db.user") != null) this.user = props.getProperty("db.user").trim();
            if (props.getProperty("db.password") != null) this.password = props.getProperty("db.password").trim();
            System.out.println("[INFO] [DBConnection] Nạp cấu hình từ db.properties thành công.");
        } else {
            System.out.println("[WARN] [DBConnection] Không tìm thấy db.properties; áp dụng cấu hình mặc định (root@localhost:3306).");
        }
    }

    /**
     * Nạp MySQL JDBC Driver vào JVM.
     */
    private void initDriver() {
        try {
            Class.forName(driver);
            System.out.println("[INFO] [DBConnection] Nạp JDBC Driver thành công: " + driver);
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] [DBConnection] Không tìm thấy JDBC Driver trong classpath: " + driver);
            e.printStackTrace();
        }
    }

    /**
     * Cung cấp điểm truy cập toàn cục tới thể hiện duy nhất của lớp (Thread-Safe).
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
     * Tự động tái tạo kết nối nếu kết nối chưa mở hoặc đã bị đóng.
     * 
     * @return Connection đối tượng kết nối SQL
     * @throws SQLException nếu kết nối thất bại
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("[INFO] [DBConnection] Thiết lập kết nối CSDL thành công: " + url.split("\\?")[0]);
            } catch (SQLException e) {
                System.err.println("[ERROR] [DBConnection] Lỗi thiết lập kết nối CSDL (" + url.split("\\?")[0] + "): " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    /**
     * Kiểm tra trạng thái kết nối tới CSDL.
     * 
     * @param timeoutSeconds thời gian tối đa chờ phản hồi (giây)
     * @return true nếu kết nối hợp lệ, ngược lại false
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
     * Đóng kết nối CSDL và giải phóng tài nguyên.
     */
    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[INFO] [DBConnection] Đã đóng kết nối CSDL.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Điểm kiểm tra chẩn đoán kết nối CSDL độc lập.
     */
    public static void main(String[] args) {
        System.out.println("[DIAGNOSTIC] Kiểm tra kết nối CSDL MySQL...");
        boolean success = DBConnection.getInstance().testConnection(5);
        if (success) {
            System.out.println("[SUCCESS] Kết nối cơ sở dữ liệu thành công.");
        } else {
            System.err.println("[FAILED] Kết nối cơ sở dữ liệu thất bại. Vui lòng kiểm tra dịch vụ MySQL và thông số kết nối.");
        }
    }
}
