import db.DBConnection;
import service.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Điểm khởi chạy chính (Main Entry Point) của ứng dụng Desktop Quản lý Thư viện.
 * 
 * Nhiệm vụ của MainApp:
 * 1. Thiết lập giao diện người dùng (System Look and Feel, làm mịn font chữ).
 * 2. Kiểm tra sức khỏe kết nối CSDL MySQL trước khi khởi động.
 * 3. Điều hướng mở màn hình Đăng nhập (LoginForm - UC-01 do Người 2 phụ trách).
 * 4. Cung cấp màn hình khởi động tạm thời (Fallback Dashboard) nếu LoginForm chưa hoàn thành.
 * 
 * @author Người số 5 (Hạ tầng chung & CSDL)
 */
public class MainApp {

    public static final String APP_TITLE = "LIBRARY MANAGEMENT SYSTEM - UNIVERSITY OF TECHNOLOGY";
    public static final String APP_VERSION = "v1.0.0 (2026)";

    public static void main(String[] args) {
        // Thiết lập thuộc tính làm mịn font chữ trên màn hình độ phân giải cao
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
            System.err.println("[MainApp] Failed to load System Look & Feel, falling back to Java default.");
        }
    }

    /**
     * Kiểm tra kết nối CSDL và khởi động form tương ứng.
     */
    private static void checkDatabaseAndLaunch() {
        System.out.println("=================================================================");
        System.out.println(" STARTING " + APP_TITLE);
        System.out.println(" Version: " + APP_VERSION);
        System.out.println("=================================================================");
        System.out.println("[MainApp] Checking connection to MySQL Server...");

        boolean isDbConnected = DBConnection.getInstance().testConnection(4);

        if (isDbConnected) {
            System.out.println("[MainApp] Database connection successful! Opening Login form...");
            openLoginForm();
        } else {
            System.err.println("[MainApp WARNING] Failed to connect to MySQL database!");
            
            String errorMessage = "<html><body style='width: 350px; font-family: sans-serif;'>"
                    + "<h3 style='color: #c0392b;'>Unable to connect to MySQL Database!</h3>"
                    + "<p>The application cannot connect to database <b>quan_li_thu_vien</b>.</p>"
                    + "<p><b>Please check the following steps:</b></p>"
                    + "<ol>"
                    + "<li>Is MySQL service or XAMPP turned ON?</li>"
                    + "<li>Have you executed script <code>setup_database.sql</code>?</li>"
                    + "<li>Verify MySQL credentials in <code>db.properties</code>.</li>"
                    + "</ol>"
                    + "<p>Do you want to continue launching in preview mode?</p>"
                    + "</body></html>";

            int option = JOptionPane.showConfirmDialog(
                    null,
                    errorMessage,
                    "Database Connection Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (option == JOptionPane.YES_OPTION) {
                openLoginForm();
            } else {
                System.out.println("[MainApp] User canceled application launch.");
                System.exit(0);
            }
        }
    }

    /**
     * Mở LoginForm (UC-01). 
     * Sử dụng Reflection an toàn: nếu LoginForm.java đã được compile thì mở,
     * nếu Người 2 chưa code xong thì hiển thị màn hình thông tin kiến trúc hạ tầng.
     */
    public static void openLoginForm() {
        try {
            // Thử khởi tạo ui.LoginForm bằng Reflection
            Class<?> loginFormClass = Class.forName("ui.LoginForm");
            JFrame loginFrame = (JFrame) loginFormClass.getDeclaredConstructor().newInstance();
            loginFrame.setVisible(true);
            return;
        } catch (ClassNotFoundException e) {
            System.out.println("[MainApp] ui.LoginForm is not ready, displaying Infrastructure Overview...");
        } catch (Exception e) {
            System.err.println("[MainApp ERROR launching LoginForm]: " + e.getMessage());
            e.printStackTrace();
        }

        // Mở cửa sổ Tổng quan Hạ tầng (Fallback Window)
        showInfrastructureOverviewWindow();
    }

    /**
     * Màn hình Tổng quan Hạ tầng chung do Người số 5 xây dựng.
     * Cung cấp nút Test kết nối DB, hiển thị trạng thái Session, và các thông tin cấu hình.
     */
    private static void showInfrastructureOverviewWindow() {
        JFrame frame = new JFrame(APP_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(680, 520);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tiêu đề
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JLabel titleLabel = new JLabel("LIBRARY MANAGEMENT SYSTEM - CORE INFRASTRUCTURE MODULE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(24, 76, 120));
        
        JLabel subtitleLabel = new JLabel("Assigned to: Person 5 (DBConnection, SessionManager, MainApp, MySQL DDL)", SwingConstants.CENTER);
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
        sb.append("=================================================================\n");
        sb.append(" SYSTEM INFRASTRUCTURE STATUS CHECK\n");
        sb.append("=================================================================\n\n");
        
        boolean dbStatus = DBConnection.getInstance().testConnection(2);
        sb.append("1. MYSQL DATABASE STATUS:\n");
        if (dbStatus) {
            sb.append("   -> CONNECTION: [ SUCCESS ]\n");
            sb.append("   -> Database: quan_li_thu_vien\n");
            sb.append("   -> Driver: com.mysql.cj.jdbc.Driver (MySQL Connector/J 8.3.0)\n\n");
        } else {
            sb.append("   -> CONNECTION: [ FAILED - MYSQL NOT RUNNING OR INVALID CREDENTIALS ]\n");
            sb.append("   -> Please start XAMPP/MySQL and execute: Library_Management/src/db/setup_database.sql\n\n");
        }

        SessionManager session = SessionManager.getInstance();
        sb.append("2. SESSION MANAGER STATUS:\n");
        sb.append("   -> Login status: ").append(session.isLoggedIn() ? "Logged In" : "Not Logged In (Guest)").append("\n");
        sb.append("   -> Current user: ").append(session.getDisplayNameWithRole()).append("\n\n");

        sb.append("3. 5-TIER ARCHITECTURE PROGRESS:\n");
        sb.append("   -> [x] Database Layer (db/): QLTV.sql, ThuVien_Indexes.sql, seed_data.sql\n");
        sb.append("   -> [x] Core Infrastructure: DBConnection.java, SessionManager.java, MainApp.java\n");
        sb.append("   -> [ ] Model Layer: Pending Person 6 (8 Entities + 2 DTOs)\n");
        sb.append("   -> [ ] UC-01 Authentication: Pending Person 2 (LoginForm, LoginService, AccountRepo)\n");
        sb.append("   -> [ ] UC-02 Search Books: Pending Person 3 (SearchBookForm, SearchService, BookRepo)\n");
        sb.append("   -> [ ] UC-03/04 Borrow Books: Pending Person 4 (BorrowForm, BorrowService, SlipRepo)\n");
        sb.append("   -> [ ] UC-05/06/07 Return Books: Pending Person 1 (ReturnBookForm, ReturnService, PaymentRepo)\n");

        statusArea.setText(sb.toString());
        mainPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        // Thanh công cụ nút bấm bên dưới
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        
        JButton testDbBtn = new JButton("Test DB Connection");
        testDbBtn.addActionListener(e -> {
            boolean test = DBConnection.getInstance().testConnection(3);
            if (test) {
                JOptionPane.showMessageDialog(frame, "MySQL database connection successful!", "Notification", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "MySQL database connection failed. Please check db.properties!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton simulateLoginBtn = new JButton("Simulate Librarian Login");
        simulateLoginBtn.addActionListener(e -> {
            session.login("thuthu01", "THU_THU", "TT0001", "Tran Thi Mai", "thuthu01@thuvien.edu.vn");
            JOptionPane.showMessageDialog(frame, "Simulated login session:\n" + session.getDisplayNameWithRole(), "SessionManager", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton closeBtn = new JButton("Exit");
        closeBtn.addActionListener(e -> System.exit(0));

        buttonPanel.add(testDbBtn);
        buttonPanel.add(simulateLoginBtn);
        buttonPanel.add(closeBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }
}
