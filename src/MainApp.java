import db.DBConnection;
import service.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Điểm khởi chạy ứng dụng Quản lý Thư viện.
 * Điều phối kiểm tra kết nối CSDL và hiển thị giao diện phù hợp.
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
                System.exit(0);
            }
        }
    }

    /**
     * Khởi chạy màn hình đăng nhập LoginForm (nếu có) hoặc cửa sổ trạng thái hạ tầng.
     */
    public static void openLoginForm() {
        try {
            Class<?> loginFormClass = Class.forName("ui.LoginForm");
            JFrame loginFrame = (JFrame) loginFormClass.getDeclaredConstructor().newInstance();
            loginFrame.setVisible(true);
            return;
        } catch (ClassNotFoundException e) {
            System.out.println("[INFO] [MainApp] Không tìm thấy ui.LoginForm, chuyển sang cửa sổ trạng thái hệ thống.");
        } catch (Exception e) {
            System.err.println("[ERROR] [MainApp] Lỗi khởi chạy giao diện đăng nhập: " + e.getMessage());
        }

        showInfrastructureOverviewWindow();
    }

    /**
     * Cửa sổ hiển thị trạng thái hạ tầng kỹ thuật (Fallback Window).
     */
    private static void showInfrastructureOverviewWindow() {
        JFrame frame = new JFrame(APP_TITLE + " - " + APP_VERSION);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(720, 520);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tiêu đề
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 3, 3));
        JLabel titleLabel = new JLabel("TRẠNG THÁI HẠ TẦNG HỆ THỐNG", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(24, 76, 120));

        JLabel subtitleLabel = new JLabel(APP_TITLE + " (" + APP_VERSION + ")", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.DARK_GRAY);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Vùng hiển thị thông tin kỹ thuật
        JTextArea statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        statusArea.setBackground(new Color(248, 249, 250));
        statusArea.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));

        updateStatusText(statusArea);
        mainPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        // Thanh điều khiển
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton refreshBtn = new JButton("Làm mới trạng thái");
        refreshBtn.addActionListener(e -> {
            updateStatusText(statusArea);
            JOptionPane.showMessageDialog(frame, "Đã cập nhật trạng thái hệ thống.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton testDbBtn = new JButton("Kiểm tra kết nối CSDL");
        testDbBtn.addActionListener(e -> {
            boolean test = DBConnection.getInstance().testConnection(3);
            updateStatusText(statusArea);
            if (test) {
                JOptionPane.showMessageDialog(frame, "Kết nối cơ sở dữ liệu MySQL thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Kết nối cơ sở dữ liệu MySQL thất bại. Vui lòng kiểm tra lại dịch vụ MySQL.", "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton closeBtn = new JButton("Thoát");
        closeBtn.addActionListener(e -> System.exit(0));

        buttonPanel.add(refreshBtn);
        buttonPanel.add(testDbBtn);
        buttonPanel.add(closeBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    /**
     * Tạo nội dung báo cáo trạng thái kỹ thuật hệ thống.
     */
    private static void updateStatusText(JTextArea area) {
        StringBuilder sb = new StringBuilder();
        sb.append("BÁO CÁO TRẠNG THÁI HẠ TẦNG KỸ THUẬT\n");
        sb.append("-----------------------------------------------------------------\n\n");

        boolean dbStatus = DBConnection.getInstance().testConnection(2);
        sb.append("1. TRẠNG THÁI KẾT NỐI CƠ SỞ DỮ LIỆU MYSQL:\n");
        if (dbStatus) {
            sb.append("Trạng thái: KẾT NỐI THÀNH CÔNG\n");
            sb.append("Database:   quan_li_thu_vien\n");
            sb.append("Driver:     com.mysql.cj.jdbc.Driver\n\n");
        } else {
            sb.append("Trạng thái: KHÔNG THỂ KẾT NỐI\n");
            sb.append("Khắc phục:  Khởi động dịch vụ MySQL và nạp script setup_database.sql\n\n");
        }

        SessionManager session = SessionManager.getInstance();
        sb.append("2. PHIÊN LÀM VIỆC HIỆN TẠI (SESSION MANAGER):\n");
        sb.append("Xác thực:   ").append(session.isLoggedIn() ? "Đã xác thực" : "Chưa xác thực (Khách)").append("\n");
        sb.append("Người dùng: ").append(session.getDisplayNameWithRole()).append("\n\n");

        sb.append("3. MÔI TRƯỜNG THỰC THI (RUNTIME ENVIRONMENT):\n");
        sb.append("Java:       ").append(System.getProperty("java.version")).append(" (").append(System.getProperty("java.vendor")).append(")\n");
        sb.append("Hệ điều hành: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.arch")).append("\n");

        area.setText(sb.toString());
    }
}
