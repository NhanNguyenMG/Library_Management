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

        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(labelFont);
        btnLogin.setPreferredSize(new Dimension(140, 35));
        panel.add(btnLogin, gbc);

        add(panel);

        // Bắt sự kiện khi click nút Đăng nhập
        btnLogin.addActionListener(this::btnConfirmActionPerformed);
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

    public static void main(String[] args) {
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