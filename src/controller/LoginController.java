package controller;

import model.Account;
import service.LoginService;
import service.SessionManager;
import javax.swing.JOptionPane;

public class LoginController {

    private final LoginService loginService;

    public LoginController() {
        this.loginService = new LoginService();
    }

    /**
     * =========================================================================
     * Use Case: UC-01 Đăng nhập
     * Sequence Diagram: sd DangNhap (SRS)
     * Traceability Message: message #2 - loginController.login()
     * Test Case tương ứng: TC-01 (Main Flow), TC-02 (Exception Flow EF-1)
     * =========================================================================
     */
    public void login(String username, String password, java.awt.Component view) {
        try {
            // Bước 4 (Main Flow): Hệ thống kiểm tra thông tin thông qua Service
            Account loggedInAccount = loginService.login(username, password);

            String role = loggedInAccount.getRole();

            // Bước 5 & 6 (Main Flow): Chuyển hướng
            JOptionPane.showMessageDialog(view, "Đăng nhập thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            redirectByRole(role, view);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Đăng nhập thất bại!\nChi tiết: " + e.getMessage(), "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Phương thức hỗ trợ chuyển hướng giao diện dựa trên vai trò
     */
    private void redirectByRole(String role, java.awt.Component currentView) {
        currentView.setVisible(false);

        if ("QUAN_LI".equals(role)) {
            System.out.println("Mở giao diện Quản lí...");
        } else if ("THU_THU".equals(role)) {
            System.out.println("Mở giao diện Thủ thư...");
        } else if ("SINH_VIEN".equals(role)) {
            System.out.println("Mở giao diện Sinh viên...");
        }
    }
}