import db.DBConnection;
import service.SessionManager;

import javax.swing.*;
import java.awt.*;

public class MainApp {

    public static final String APP_TITLE = "HỆ THỐNG QUẢN LÝ THƯ VIỆN";
    public static final String APP_VERSION = "Phiên bản v1.0.0 (2026)";

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Chạy giao diện trên luồng Event Dispatch Thread (EDT) chuẩn của Swing
        SwingUtilities.invokeLater(() -> {
            setupLookAndFeel();
            checkDatabaseAndLaunch();
        });
    }

    /**
     * Cài đặt giao diện chuẩn theo hệ điều hành (Windows / macOS / Linux).
     */
    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("[WARN] [MainApp] Không thể tải giao diện hệ thống (System Look & Feel); sử dụng giao diện mặc định.");
        }
    }

    /**
     * Kiểm tra kết nối CSDL và khởi động form tương ứng.
     */
    private static void checkDatabaseAndLaunch() {
        System.out.println("=================================================================");
        System.out.println(" KHỞI ĐỘNG HỆ THỐNG: " + APP_TITLE);
        System.out.println(" PHIÊN BẢN: " + APP_VERSION);
        System.out.println("=================================================================");
        System.out.println("[INFO] [MainApp] Đang kiểm tra kết nối tới máy chủ cơ sở dữ liệu MySQL...");

        boolean isDbConnected = DBConnection.getInstance().testConnection(4);

        if (isDbConnected) {
            System.out.println("[INFO] [MainApp] Kết nối cơ sở dữ liệu thành công. Đang tải màn hình Đăng nhập...");
            openLoginForm();
        } else {
            System.err.println("[WARN] [MainApp] Không thể thiết lập kết nối tới cơ sở dữ liệu MySQL.");
            
            String errorMessage = "<html><body style='width: 380px; font-family: sans-serif;'>"
                    + "<h3 style='color: #c0392b;'>Không thể kết nối Cơ sở dữ liệu MySQL</h3>"
                    + "<p>Hệ thống không thể thiết lập kết nối tới cơ sở dữ liệu <b>quan_li_thu_vien</b>.</p>"
                    + "<p><b>Vui lòng kiểm tra lại các cấu hình sau:</b></p>"
                    + "<ol>"
                    + "<li>Dịch vụ máy chủ MySQL (XAMPP / MySQL Service) đã được kích hoạt chưa?</li>"
                    + "<li>Cơ sở dữ liệu đã được tạo thông qua tệp kịch bản <code>setup_database.sql</code> chưa?</li>"
                    + "<li>Kiểm tra tính chính xác của thông tin tài khoản trong <code>db.properties</code>.</li>"
                    + "</ol>"
                    + "<p>Bạn có muốn tiếp tục khởi chạy ứng dụng ở chế độ ngoại tuyến/xem trước không?</p>"
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
                System.out.println("[INFO] [MainApp] Người dùng đã hủy tiến trình khởi động ứng dụng.");
                System.exit(0);
            }
        }
    }

    /**
     * Mở màn hình đăng nhập LoginForm.
     */
    public static void openLoginForm() {
        try {
            // Thử khởi tạo ui.LoginForm bằng Reflection
            Class<?> loginFormClass = Class.forName("ui.LoginForm");
            JFrame loginFrame = (JFrame) loginFormClass.getDeclaredConstructor().newInstance();
            loginFrame.setVisible(true);
            return;
        } catch (ClassNotFoundException e) {
            System.out.println("[INFO] [MainApp] Giao diện LoginForm chưa sẵn sàng; chuyển hướng sang màn hình trạng thái hệ thống.");
        } catch (Exception e) {
            System.err.println("[ERROR] [MainApp] Lỗi khởi chạy giao diện đăng nhập: " + e.getMessage());
            e.printStackTrace();
        }

        // Mở cửa sổ Tổng quan Hạ tầng (Fallback Window)
        showInfrastructureOverviewWindow();
    }

    /**
     * Màn hình Tổng quan cấu hình hệ thống.
     */
    private static void showInfrastructureOverviewWindow() {
        JFrame frame = new JFrame(APP_TITLE + " - " + APP_VERSION);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(720, 540);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tiêu đề
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JLabel titleLabel = new JLabel("HỆ THỐNG QUẢN LÝ THƯ VIỆN - TRẠNG THÁI HỆ THỐNG", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(24, 76, 120));
        
        JLabel subtitleLabel = new JLabel("Hệ thống quản lý thư viện - Trạng thái cấu hình", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.DARK_GRAY);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Vùng hiển thị trạng thái
        JTextArea statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        statusArea.setBackground(new Color(248, 249, 250));
        statusArea.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));

        StringBuilder sb = new StringBuilder();
        sb.append("BÁO CÁO TRẠNG THÁI HỆ THỐNG\n\n");
        
        boolean dbStatus = DBConnection.getInstance().testConnection(2);
        sb.append("1. TRẠNG THÁI KẾT NỐI CƠ SỞ DỮ LIỆU MYSQL:\n");
        if (dbStatus) {
            sb.append("   -> Trạng thái: [ KẾT NỐI THÀNH CÔNG ]\n");
            sb.append("   -> Cơ sở dữ liệu: quan_li_thu_vien\n");
            sb.append("   -> Driver: com.mysql.cj.jdbc.Driver (MySQL Connector/J 8.3.0)\n\n");
        } else {
            sb.append("   -> Trạng thái: [ KẾT NỐI THẤT BẠI - DỊCH VỤ CHƯA SẴN SÀNG HOẶC SAI THÔNG TIN XÁC THỰC ]\n");
            sb.append("   -> Hướng khắc phục: Khởi động dịch vụ MySQL và thực thi tệp cấu hình CSDL: setup_database.sql\n\n");
        }

        SessionManager session = SessionManager.getInstance();
        sb.append("2. TRẠNG THÁI PHIÊN LÀM VIỆC (SESSION MANAGER):\n");
        sb.append("   -> Trạng thái xác thực: ").append(session.isLoggedIn() ? "Đã đăng nhập" : "Chưa xác thực (Khách)").append("\n");
        sb.append("   -> Người dùng hiện tại: ").append(session.getDisplayNameWithRole()).append("\n\n");

        sb.append("3. DANH MỤC PHÂN HỆ CHỨC NĂNG HỆ THỐNG:\n");
        sb.append("   -> Xác thực tài khoản và phân quyền truy cập\n");
        sb.append("   -> Tra cứu thông tin danh mục sách\n");
        sb.append("   -> Lập và quản lý phiếu mượn tài liệu\n");
        sb.append("   -> Hoàn trả tài liệu và xử lý phí phạt vi phạm\n");

        statusArea.setText(sb.toString());
        mainPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        // Thanh công cụ nút bấm bên dưới
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        
        JButton testDbBtn = new JButton("Kiểm tra kết nối CSDL");
        testDbBtn.addActionListener(e -> {
            boolean test = DBConnection.getInstance().testConnection(3);
            if (test) {
                JOptionPane.showMessageDialog(frame, "Kết nối cơ sở dữ liệu MySQL thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Kết nối cơ sở dữ liệu MySQL thất bại. Vui lòng kiểm tra lại dịch vụ và tệp db.properties.", "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton simulateLoginBtn = new JButton("Giả lập Đăng nhập Thủ thư");
        simulateLoginBtn.addActionListener(e -> {
            session.login("thuthu01", SessionManager.ROLE_LIBRARIAN, "TT0001", "Trần Thị Mai", "thuthu01@thuvien.edu.vn");
            JOptionPane.showMessageDialog(frame, "Đã thiết lập phiên đăng nhập thử nghiệm:\n" + session.getDisplayNameWithRole(), "SessionManager", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton closeBtn = new JButton("Thoát");
        closeBtn.addActionListener(e -> System.exit(0));

        buttonPanel.add(testDbBtn);
        buttonPanel.add(simulateLoginBtn);
        buttonPanel.add(closeBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }
}
