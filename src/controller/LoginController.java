package controller;

import model.Account;
import service.LoginService;
import service.SessionManager;
import ui.DashboardForm;
import ui.LoginForm;
import ui.SearchBookForm;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class LoginController {

    private final LoginService loginService;

    public LoginController() {
        this.loginService = new LoginService();
    }

    /**
     * Xử lý đăng nhập tài khoản người dùng
     */
    public void login(String username, String password, java.awt.Component view) {
        try {
            Account loggedInAccount = loginService.login(username, password);
            String role = loggedInAccount.getRole();

            JOptionPane.showMessageDialog(view, "Đăng nhập thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            redirectByRole(role, view);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Đăng nhập thất bại!\nChi tiết: " + e.getMessage(), "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Phương thức hỗ trợ chuyển hướng giao diện dựa trên vai trò
     */
    private void redirectByRole(String role, java.awt.Component currentView) {
        if (currentView instanceof JFrame frame) {
            frame.dispose();
        } else if (currentView != null) {
            currentView.setVisible(false);
        }

        if (SessionManager.ROLE_STUDENT.equalsIgnoreCase(role)) {
            SwingUtilities.invokeLater(() -> {
                SearchBookForm searchBookForm = new SearchBookForm();
                searchBookForm.setVisible(true);
            });
        } else if (SessionManager.ROLE_LIBRARIAN.equalsIgnoreCase(role)
                || SessionManager.ROLE_MANAGER.equalsIgnoreCase(role)) {
            SwingUtilities.invokeLater(() -> {
                DashboardForm dashboardForm = new DashboardForm();
                dashboardForm.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(null, "Vai trò không hợp lệ: " + role, "Lỗi phân quyền", JOptionPane.ERROR_MESSAGE);
            if (currentView != null) {
                currentView.setVisible(true);
            }
        }
    }
}