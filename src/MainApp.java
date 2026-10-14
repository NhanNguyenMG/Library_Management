import db.DBConnection;
import ui.InfrastructureStatusFrame;
import ui.LoginForm;

import javax.swing.*;
import java.awt.*;

/**
 * Điểm khởi chạy ứng dụng Quản lý Thư viện.
 * Tuân thủ Single Responsibility Principle (SRP): Chỉ điều phối kiểm tra hạ tầng và khởi chạy màn hình chính.
 */
public class MainApp {

    public static final String APP_TITLE = "HỆ THỐNG QUẢN LÝ THƯ VIỆN";
    public static final String APP_VERSION = "v1.0.0";

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Khởi chạy trên Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            setupLookAndFeel();
            checkDatabaseAndLaunch();
        });
    }

    /**
     * Thiết lập giao diện theo hệ điều hành (System Look & Feel).
     */
    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("[WARN] [MainApp] Không thể tải System Look & Feel: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra trạng thái kết nối CSDL trước khi mở giao diện chính.
     */
    private static void checkDatabaseAndLaunch() {
        System.out.println("[INFO] [MainApp] Khởi động ứng dụng: " + APP_TITLE + " (" + APP_VERSION + ")");
        System.out.println("[INFO] [MainApp] Kiểm tra kết nối máy chủ CSDL MySQL...");

        boolean isDbConnected = DBConnection.getInstance().testConnection(4);

        if (isDbConnected) {
            System.out.println("[INFO] [MainApp] Kết nối CSDL thành công. Đang mở màn hình đăng nhập...");
            openLoginForm();
        } else {
            System.err.println("[WARN] [MainApp] Kết nối máy chủ CSDL thất bại.");

            String errorMessage = "<html><body style='width: 340px; font-family: sans-serif;'>"
                    + "<h3 style='color: #c0392b; margin: 0 0 8px 0;'>Không thể kết nối CSDL MySQL</h3>"
                    + "<p style='margin: 4px 0;'>Hệ thống không thể kết nối cơ sở dữ liệu <b>quan_li_thu_vien</b>.</p>"
                    + "<p style='margin: 4px 0;'><b>Vui lòng kiểm tra:</b></p>"
                    + "<ul style='margin-left: -15px; margin-top: 4px;'>"
                    + "<li>Dịch vụ MySQL đang chạy (XAMPP / MySQL Service)</li>"
                    + "<li>CSDL đã khởi tạo bằng script <code>setup_database.sql</code></li>"
                    + "<li>Thông tin cấu hình trong <code>db.properties</code></li>"
                    + "</ul>"
                    + "<p style='margin: 8px 0 0 0;'>Tiếp tục mở ứng dụng ở chế độ ngoại tuyến (Offline Preview)?</p>"
                    + "</body></html>";

            int option = JOptionPane.showConfirmDialog(
                    null,
                    errorMessage,
                    "Cảnh báo Kết nối CSDL",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (option == JOptionPane.YES_OPTION) {
                openLoginForm();
            } else {
                System.out.println("[INFO] [MainApp] Đã hủy khởi động ứng dụng.");
                showInfrastructureOverviewWindow();
            }
        }
    }

    /**
     * Khởi chạy màn hình đăng nhập LoginForm trực tiếp mà không dùng phản chiếu.
     */
    public static void openLoginForm() {
        try {
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        } catch (Exception e) {
            System.err.println("[ERROR] [MainApp] Lỗi khởi chạy giao diện đăng nhập: " + e.getMessage());
            showInfrastructureOverviewWindow();
        }
    }

    /**
     * Hiển thị cửa sổ trạng thái hạ tầng kỹ thuật (Ủy quyền cho InfrastructureStatusFrame).
     */
    public static void showInfrastructureOverviewWindow() {
        InfrastructureStatusFrame statusFrame = new InfrastructureStatusFrame(APP_TITLE, APP_VERSION);
        statusFrame.setVisible(true);
    }
}
