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

    public static final String APP_TITLE = "HỆ THỐNG QUẢN LÝ THƯ VIỆN - ĐẠI HỌC CÔNG NGHỆ KỸ THUẬT";
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
            System.err.println("[MainApp] Không thể nạp System Look & Feel, sử dụng mặc định của Java.");
        }
    }

    /**
     * Kiểm tra kết nối CSDL và khởi động form tương ứng.
     */
    private static void checkDatabaseAndLaunch() {
        System.out.println("=================================================================");
        System.out.println(" KHỞI ĐỘNG " + APP_TITLE);
        System.out.println(" Phiên bản: " + APP_VERSION);
        System.out.println("=================================================================");
        System.out.println("[MainApp] Đang kiểm tra kết nối tới MySQL Server...");

        boolean isDbConnected = DBConnection.getInstance().testConnection(4);

        if (isDbConnected) {
            System.out.println("[MainApp] Kết nối CSDL thành công! Đang mở giao diện Đăng nhập...");
            openLoginForm();
        } else {
            System.err.println("[MainApp CẢNH BÁO] Không thể kết nối tới cơ sở dữ liệu MySQL!");
            
            String errorMessage = "<html><body style='width: 350px; font-family: sans-serif;'>"
                    + "<h3 style='color: #c0392b;'>Không thể kết nối Cơ sở dữ liệu MySQL!</h3>"
                    + "<p>Ứng dụng không thể kết nối tới database <b>quan_li_thu_vien</b>.</p>"
                    + "<p><b>Vui lòng kiểm tra các bước sau:</b></p>"
                    + "<ol>"
                    + "<li>MySQL Service hoặc XAMPP đã được BẬT chưa?</li>"
                    + "<li>Bạn đã chạy file script <code>setup_database.sql</code> chưa?</li>"
                    + "<li>Kiểm tra mật khẩu MySQL trong file <code>db.properties</code>.</li>"
                    + "</ol>"
                    + "<p>Bạn có muốn tiếp tục mở ứng dụng ở chế độ kiểm tra giao diện không?</p>"
                    + "</body></html>";

            int option = JOptionPane.showConfirmDialog(
                    null,
                    errorMessage,
                    "Cảnh báo kết nối Database",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (option == JOptionPane.YES_OPTION) {
                openLoginForm();
            } else {
                System.out.println("[MainApp] Người dùng đã hủy khởi động ứng dụng.");
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
            System.out.println("[MainApp] ui.LoginForm chưa sẵn sàng, đang mở màn hình Tổng quan Hạ tầng chung...");
        } catch (Exception e) {
            System.err.println("[MainApp LỖI khi mở LoginForm]: " + e.getMessage());
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
        JLabel titleLabel = new JLabel("HỆ THỐNG QUẢN LÝ THƯ VIỆN - MODULE HẠ TẦNG CHUNG", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(24, 76, 120));
        
        JLabel subtitleLabel = new JLabel("Phụ trách: Người số 5 (DBConnection, SessionManager, MainApp, MySQL DDL)", SwingConstants.CENTER);
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
        sb.append(" KIỂM TRA TRẠNG THÁI HẠ TẦNG HỆ THỐNG (SYSTEM STATUS)\n");
        sb.append("=================================================================\n\n");
        
        boolean dbStatus = DBConnection.getInstance().testConnection(2);
        sb.append("1. TRẠNG THÁI CƠ SỞ DỮ LIỆU MYSQL:\n");
        if (dbStatus) {
            sb.append("   -> KẾT NỐI: [ THÀNH CÔNG ]\n");
            sb.append("   -> Database: quan_li_thu_vien\n");
            sb.append("   -> Driver: com.mysql.cj.jdbc.Driver (MySQL Connector/J 8.3.0)\n\n");
        } else {
            sb.append("   -> KẾT NỐI: [ THẤT BẠI - CHƯA BẬT MYSQL HOẶC SAI MẬT KHẨU ]\n");
            sb.append("   -> Vui lòng mở XAMPP/MySQL và chạy file: LibraryManagement/src/db/setup_database.sql\n\n");
        }

        SessionManager session = SessionManager.getInstance();
        sb.append("2. TRẠNG THÁI PHIÊN LÀM VIỆC (SESSION MANAGER):\n");
        sb.append("   -> Trạng thái đăng nhập: ").append(session.isLoggedIn() ? "Đã đăng nhập" : "Chưa đăng nhập (Khách)").append("\n");
        sb.append("   -> Người dùng hiện tại: ").append(session.getDisplayNameWithRole()).append("\n\n");

        sb.append("3. TIẾN TRÌNH CÁC TẦNG MÃ NGUỒN (KIẾN TRÚC 5 TẦNG):\n");
        sb.append("   -> [x] Tầng Cơ sở dữ liệu (db/): QLTV.sql, ThuVien_Indexes.sql, seed_data.sql\n");
        sb.append("   -> [x] Tầng Hạ tầng chung: DBConnection.java, SessionManager.java, MainApp.java\n");
        sb.append("   -> [ ] Tầng Model: Chờ Người 6 bàn giao 8 Entity + 2 DTO\n");
        sb.append("   -> [ ] UC-01 Đăng nhập: Chờ Người 2 (LoginForm, LoginService, AccountRepo)\n");
        sb.append("   -> [ ] UC-02 Tra cứu: Chờ Người 3 (SearchBookForm, SearchService, BookRepo)\n");
        sb.append("   -> [ ] UC-03/04 Mượn sách: Chờ Người 4 (BorrowForm, BorrowService, SlipRepo)\n");
        sb.append("   -> [ ] UC-05/06/07 Trả sách: Chờ Người 1 (ReturnBookForm, ReturnService, PaymentRepo)\n");

        statusArea.setText(sb.toString());
        mainPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        // Thanh công cụ nút bấm bên dưới
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        
        JButton testDbBtn = new JButton("Kiểm tra lại kết nối DB");
        testDbBtn.addActionListener(e -> {
            boolean test = DBConnection.getInstance().testConnection(3);
            if (test) {
                JOptionPane.showMessageDialog(frame, "Kết nối CSDL MySQL thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Kết nối CSDL MySQL thất bại. Hãy kiểm tra db.properties!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton simulateLoginBtn = new JButton("Giả lập Đăng nhập Thủ thư");
        simulateLoginBtn.addActionListener(e -> {
            session.login("thuthu01", "THU_THU", "TT0001", "Trần Thị Mai", "thuthu01@thuvien.edu.vn");
            JOptionPane.showMessageDialog(frame, "Đã giả lập phiên đăng nhập:\n" + session.getDisplayNameWithRole(), "SessionManager", JOptionPane.INFORMATION_MESSAGE);
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
