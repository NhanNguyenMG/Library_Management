package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;

/**
 * Cửa sổ hiển thị trạng thái hạ tầng kỹ thuật và chẩn đoán kết nối CSDL (Infrastructure Status Frame).
 * Tách biệt khỏi MainApp
 */
public class InfrastructureStatusFrame extends JFrame {

    private final JTextArea statusArea;

    public InfrastructureStatusFrame(String appTitle, String appVersion) {
        super(appTitle + " - " + appVersion);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 520);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tiêu đề
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 3, 3));
        JLabel titleLabel = new JLabel("TRẠNG THÁI HẠ TẦNG HỆ THỐNG", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(24, 76, 120));

        JLabel subtitleLabel = new JLabel(appTitle + " (" + appVersion + ")", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.DARK_GRAY);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Vùng hiển thị thông tin kỹ thuật
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        statusArea.setBackground(new Color(248, 249, 250));
        statusArea.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));

        updateStatusText();
        mainPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        // Thanh điều khiển
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton refreshBtn = new JButton("Làm mới trạng thái");
        refreshBtn.addActionListener(e -> {
            updateStatusText();
            JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái hệ thống.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton testDbBtn = new JButton("Kiểm tra kết nối CSDL");
        testDbBtn.addActionListener(e -> {
            boolean test = DBConnection.getInstance().testConnection(3);
            updateStatusText();
            if (test) {
                JOptionPane.showMessageDialog(this, "Kết nối cơ sở dữ liệu MySQL thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Kết nối cơ sở dữ liệu MySQL thất bại. Vui lòng kiểm tra lại dịch vụ MySQL.", "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton closeBtn = new JButton("Thoát");
        closeBtn.addActionListener(e -> System.exit(0));

        buttonPanel.add(refreshBtn);
        buttonPanel.add(testDbBtn);
        buttonPanel.add(closeBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * Tạo nội dung báo cáo trạng thái kỹ thuật hệ thống.
     */
    private void updateStatusText() {
        StringBuilder sb = new StringBuilder();
        sb.append("BÁO CÁO TRẠNG THÁI HẠ TẦNG KỸ THUẬT\n");
        sb.append("-----------------------------------------------------------------\n\n");

        boolean dbStatus = DBConnection.getInstance().testConnection(2);
        sb.append("1. TRẠNG THÁI KẾT NỐI CƠ SỞ DỮ LIỆU MYSQL:\n");
        if (dbStatus) {
            sb.append("Trạng thái: KẾT NỐI THÀNH CÔNG\n");
            sb.append("Database:   quan_li_thu_vien\n");
            sb.append("Driver:     com.mysql.cj.jdbc.Driver\n");
        } else {
            sb.append("Trạng thái: KHÔNG THỂ KẾT NỐI\n");
            sb.append("Nguyên nhân có thể: Dịch vụ MySQL chưa bật hoặc sai cổng/thông tin xác thực.\n");
        }

        sb.append("\n2. MÔI TRƯỜNG THỰC THI (JVM / RUNTIME):\n");
        sb.append("Java Version:  ").append(System.getProperty("java.version")).append("\n");
        sb.append("Java Home:     ").append(System.getProperty("java.home")).append("\n");
        sb.append("Hệ điều hành:  ").append(System.getProperty("os.name")).append(" (").append(System.getProperty("os.arch")).append(")\n");

        sb.append("\n3. BỘ NHỚ HỆ THỐNG (HEAP MEMORY):\n");
        long maxMem = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        long totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        sb.append("Bộ nhớ tối đa (Max):    ").append(maxMem).append(" MB\n");
        sb.append("Bộ nhớ cấp phát (Total): ").append(totalMem).append(" MB\n");
        sb.append("Bộ nhớ còn trống (Free): ").append(freeMem).append(" MB\n");

        statusArea.setText(sb.toString());
        statusArea.setCaretPosition(0);
    }
}
