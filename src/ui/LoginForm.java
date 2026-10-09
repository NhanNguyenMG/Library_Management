package ui;

import controller.LoginController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginForm extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    // Tích hợp Controller để xử lý nghiệp vụ
    private LoginController loginController;

    public LoginForm() {
        loginController = new LoginController();

        // Cấu hình cửa sổ chính
        setTitle("Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 240);
        setLocationRelativeTo(null); // Căn giữa màn hình Desktop

        // Set font chữ in đậm
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Panel chứa layout với lề bao quanh (padding)
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Khoảng cách đều giữa các hàng/cột
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Nhãn & Ô nhập Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblUsername = new JLabel("Tên đăng nhập:");
        lblUsername.setFont(labelFont);
        panel.add(lblUsername, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        txtUsername = new JTextField(15);
        txtUsername.setFont(inputFont);
        panel.add(txtUsername, gbc);

        // 2. Nhãn & Ô nhập Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblPassword = new JLabel("Mật khẩu:");
        lblPassword.setFont(labelFont);
        panel.add(lblPassword, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(inputFont);
        panel.add(txtPassword, gbc);

        // 3. Nút Đăng nhập
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(20, 10, 5, 10);

        btnLogin = createStyledButton("Đăng nhập", new Color(37, 99, 235), Color.WHITE);
        btnLogin.setFont(labelFont);
        btnLogin.setPreferredSize(new Dimension(140, 36));
        panel.add(btnLogin, gbc);

        // 4. Panel tài khoản kiểm thử nhanh bên dưới
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 5, 5);

        JPanel testAccountsPanel = createTestAccountsPanel();
        panel.add(testAccountsPanel, gbc);

        add(panel);

        // Bắt sự kiện khi click nút Đăng nhập hoặc ấn phím Enter
        btnLogin.addActionListener(this::btnConfirmActionPerformed);
        txtPassword.addActionListener(this::btnConfirmActionPerformed);
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());
        setSize(480, 485);
        setLocationRelativeTo(null);
    }

    /**
     * Tạo bảng danh sách 4 tài khoản kiểm thử theo yêu cầu
     */
    private JPanel createTestAccountsPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(248, 250, 252));
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblTitle = new JLabel("Tài khoản kiểm thử nhanh (Click để tự động điền):");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(71, 85, 105));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(lblTitle);
        container.add(Box.createVerticalStrut(8));

        // Tài khoản mẫu
        container.add(createAccountRow("Quản lý:", "admin", "admin123", new Color(124, 58, 237)));
        container.add(Box.createVerticalStrut(5));
        container.add(createAccountRow("Thủ thư:", "thuthu01", "thuthu123", new Color(2, 132, 199)));
        container.add(Box.createVerticalStrut(5));
        container.add(createAccountRow("Sinh viên:", "sv001", "123456", new Color(16, 185, 129)));
        container.add(Box.createVerticalStrut(5));
        container.add(createAccountRow("SV có nợ:", "sv003", "123456", new Color(234, 88, 12)));

        return container;
    }

    private JPanel createAccountRow(String roleLabel, String username, String password, Color badgeColor) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Badge vai trò
        JLabel lblRole = new JLabel(roleLabel);
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRole.setForeground(badgeColor);
        lblRole.setPreferredSize(new Dimension(75, 18));

        // Thông tin tài khoản
        JLabel lblInfo = new JLabel(username + "  /  " + password);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(new Color(30, 41, 59));

        JLabel lblHint = new JLabel("Chọn >>");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint.setForeground(new Color(148, 163, 184));

        row.add(lblRole, BorderLayout.WEST);
        row.add(lblInfo, BorderLayout.CENTER);
        row.add(lblHint, BorderLayout.EAST);

        // Sự kiện click tự động điền
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                row.setBackground(new Color(241, 245, 249));
                lblHint.setForeground(badgeColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                row.setBackground(Color.WHITE);
                lblHint.setForeground(new Color(148, 163, 184));
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                txtUsername.setText(username);
                txtPassword.setText(password);
                txtPassword.requestFocusInWindow();
            }
        });

        return row;
    }

    private void btnConfirmActionPerformed(ActionEvent e) {
        // Thu thập dữ liệu
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        // Validate cơ bản ở giao diện (UI)
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Chuyển dữ liệu xuống tầng Controller xử lý
        loginController.login(username, password, this);
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg.equals(Color.WHITE) ? bg.darker() : new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        return btn;
    }

    public static void main(String[] args) {
        // Kích hoạt khử răng cưa chữ (Anti-Aliasing) trên toàn hệ thống Swing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Kích hoạt giao diện đồ họa giống với hệ điều hành đang dùng (Windows Look & Feel)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Chạy ứng dụng trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}