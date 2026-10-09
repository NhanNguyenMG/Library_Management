package ui;

import controller.DebtController;
import controller.SearchController;
import model.Book;
import model.DebtPaymentResult;
import model.Student;
import service.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

public class SearchBookForm extends JFrame {

    private JTextField txtTuKhoa;
    private JButton btnTimKiem;
    private JButton btnXoa;
    private JLabel lblThongKe;
    private JTable tblSach;
    private DefaultTableModel tableModel;

    private JLabel lblDebtInfo;
    private JButton btnPayDebt;

    private final SearchController searchController;

    // Lưu danh sách Book tương ứng với các dòng đang hiển thị
    private List<Book> danhSachSachHienTai;

    public SearchBookForm() {

        searchController = new SearchController();

        khoiTaoGiaoDien();
        ganSuKien();
        taiToanBoDanhSachSach(); // Tự động hiển thị toàn bộ sách trong thư viện ngay khi mở form
    }

    /**
     * Khởi tạo giao diện
     */
    private void khoiTaoGiaoDien() {

        setTitle("Tra cứu sách - Library Management System");

        setSize(900, 600);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (SessionManager.getInstance().isLoggedIn() && SessionManager.getInstance().isStudent()) {
                    SessionManager.getInstance().logout();
                    new LoginForm().setVisible(true);
                }
            }
        });

        // Panel chính
        JPanel panelChinh =
                new JPanel(new BorderLayout(10, 10));

        panelChinh.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        20,
                        15,
                        20
                )
        );

        // Header panel: gồm thanh sinh viên (nếu có) và tiêu đề
        JPanel panelHeader = new JPanel(new BorderLayout(0, 12));
        panelHeader.setOpaque(false);

        JPanel panelStudentBar = taoThanhThongTinSinhVien();
        if (panelStudentBar != null) {
            panelHeader.add(panelStudentBar, BorderLayout.NORTH);
        }

        // Tiêu đề
        JLabel lblTieuDe =
                new JLabel("TRA CỨU SÁCH");

        lblTieuDe.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        24
                )
        );

        lblTieuDe.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        panelHeader.add(lblTieuDe, BorderLayout.CENTER);

        panelChinh.add(
                panelHeader,
                BorderLayout.NORTH
        );

        // Panel nội dung
        JPanel panelNoiDung =
                new JPanel(
                        new BorderLayout(10, 10)
                );

        // Panel tìm kiếm

        JPanel panelTimKiem =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                10
                        )
                );

        JLabel lblTuKhoa =
                new JLabel("Từ khóa:");

        lblTuKhoa.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        txtTuKhoa =
                new JTextField(26);

        txtTuKhoa.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );
        txtTuKhoa.setPreferredSize(new Dimension(280, 34));

        btnTimKiem = createStyledButton("Tìm kiếm", new Color(37, 99, 235), Color.WHITE);
        btnTimKiem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTimKiem.setPreferredSize(new Dimension(110, 34));

        btnXoa = createStyledButton("Tất cả sách / Xóa lọc", new Color(241, 245, 249), new Color(30, 41, 59));
        btnXoa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnXoa.setPreferredSize(new Dimension(170, 34));

        lblThongKe = new JLabel("Đang tải dữ liệu...");
        lblThongKe.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblThongKe.setForeground(new Color(70, 70, 70));

        JButton btnDong = createStyledButton("Đóng", new Color(241, 245, 249), new Color(30, 41, 59));
        btnDong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnDong.setPreferredSize(new Dimension(80, 34));
        btnDong.addActionListener(e -> dispose());
        btnDong.setVisible(!SessionManager.getInstance().isStudent());

        panelTimKiem.add(lblTuKhoa);
        panelTimKiem.add(txtTuKhoa);
        panelTimKiem.add(btnTimKiem);
        panelTimKiem.add(btnXoa);
        panelTimKiem.add(btnDong);
        panelTimKiem.add(Box.createHorizontalStrut(15));
        panelTimKiem.add(lblThongKe);

        panelNoiDung.add(
                panelTimKiem,
                BorderLayout.NORTH
        );

        // Bảng sách
        String[] tenCot = {
                "Mã sách",
                "Tên sách",
                "Tác giả",
                "Số lượng còn"
        };

        tableModel =
                new DefaultTableModel(
                        tenCot,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        tblSach =
                new JTable(tableModel);

        tblSach.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        tblSach.setRowHeight(30);

        tblSach.getTableHeader().setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );
        tblSach.getTableHeader().setBackground(new Color(240, 242, 245));

        tblSach.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane =
                new JScrollPane(tblSach);

        JPanel panelBang =
                new JPanel(new BorderLayout());

        panelBang.setBorder(
                BorderFactory.createTitledBorder(
                        "Danh sách sách trong thư viện (Nhấn đúp chuột vào cuốn sách để xem chi tiết mô tả)"
                )
        );

        panelBang.add(
                scrollPane,
                BorderLayout.CENTER
        );

        panelNoiDung.add(
                panelBang,
                BorderLayout.CENTER
        );

        // Thêm nội dung vào panel chính
        panelChinh.add(
                panelNoiDung,
                BorderLayout.CENTER
        );

        add(panelChinh);
    }

    /**
     * Gán sự kiện cho giao diện
     */
    private void ganSuKien() {

        // Nút Tìm kiếm
        btnTimKiem.addActionListener(
                e -> xuLyTimKiem()
        );

        // Nhấn Enter trong ô tìm kiếm
        txtTuKhoa.addActionListener(
                e -> xuLyTimKiem()
        );

        // Nút Xóa
        btnXoa.addActionListener(
                e -> xuLyXoa()
        );

        // Double-click vào dòng sách để xem chi tiết
        tblSach.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent e
                    ) {

                        if (e.getClickCount() == 2
                                && SwingUtilities
                                .isLeftMouseButton(e)) {

                            xuLyXemChiTiet();
                        }
                    }
                }
        );
    }

    /**
     * Tải và hiển thị toàn bộ danh sách sách có trong thư viện
     */
    public void taiToanBoDanhSachSach() {
        try {
            List<Book> danhSach = searchController.getAllBooks();
            hienThiDanhSachSach(danhSach);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải danh sách sách từ cơ sở dữ liệu.\nChi tiết: " + e.getMessage(),
                    "Lỗi cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Hiển thị danh sách sách lên bảng và cập nhật nhãn số lượng
     */
    private void hienThiDanhSachSach(List<Book> danhSach) {
        danhSachSachHienTai = danhSach;
        xoaDuLieuBang();

        if (danhSach != null) {
            for (Book book : danhSach) {
                tableModel.addRow(
                        new Object[]{
                                book.getBookId(),
                                book.getTitle(),
                                book.getAuthor(),
                                book.getStockQuantity()
                        }
                );
            }
            if (lblThongKe != null) {
                lblThongKe.setText("Tổng số: " + danhSach.size() + " đầu sách");
            }
        } else {
            if (lblThongKe != null) {
                lblThongKe.setText("Tổng số: 0 đầu sách");
            }
        }
    }

    /**
     * Xử lý tìm kiếm sách
     */
    private void xuLyTimKiem() {

        String tuKhoa =
                txtTuKhoa.getText().trim();

        // Nếu từ khóa rỗng -> Tải lại toàn bộ sách
        if (tuKhoa.isEmpty()) {
            taiToanBoDanhSachSach();
            return;
        }

        try {
            List<Book> danhSachSach =
                    searchController.searchBooks(
                            tuKhoa
                    );

            hienThiDanhSachSach(danhSachSach);

            // Không tìm thấy kết quả
            if (danhSachSach.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Không tìm thấy sách phù hợp với từ khóa: \""
                                + tuKhoa + "\"",
                        "Kết quả tìm kiếm",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (IllegalArgumentException e) {

            taiToanBoDanhSachSach();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Không thể thực hiện tìm kiếm.\n"
                            + "Vui lòng kiểm tra kết nối cơ sở dữ liệu.\n\n"
                            + "Chi tiết: "
                            + e.getMessage(),
                    "Lỗi cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Xử lý nút Xóa bộ lọc / Hiển thị tất cả sách
     */
    private void xuLyXoa() {

        txtTuKhoa.setText("");

        taiToanBoDanhSachSach(); // Tải lại toàn bộ sách thay vì làm bảng trống trơn

        txtTuKhoa.requestFocus();
    }

    /**
     * Xóa dữ liệu trong bảng
     */
    private void xoaDuLieuBang() {

        tableModel.setRowCount(0);
    }

    /**
     * Xem chi tiết sách khi double-click
     */
    private void xuLyXemChiTiet() {

        int dongDuocChon =
                tblSach.getSelectedRow();

        // Không có dòng nào được chọn
        if (dongDuocChon < 0) {
            return;
        }

        // Kiểm tra danh sách dữ liệu hiện tại
        if (danhSachSachHienTai == null
                || dongDuocChon
                >= danhSachSachHienTai.size()) {

            return;
        }

        // Lấy Book tương ứng với dòng được chọn
        Book book =
                danhSachSachHienTai.get(
                        dongDuocChon
                );

        // Lấy mô tả
        String moTa =
                book.getDescription();

        if (moTa == null
                || moTa.trim().isEmpty()) {

            moTa =
                    "Chưa có mô tả cho sách này.";
        }

        // Tạo vùng hiển thị mô tả
        JTextArea txtMoTa =
                new JTextArea(moTa);

        txtMoTa.setLineWrap(true);

        txtMoTa.setWrapStyleWord(true);

        txtMoTa.setEditable(false);

        txtMoTa.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        JScrollPane scrollPane =
                new JScrollPane(txtMoTa);

        scrollPane.setPreferredSize(
                new Dimension(
                        450,
                        180
                )
        );

        // Tạo panel chi tiết
        JPanel panelChiTiet =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        JLabel lblThongTin =
                new JLabel(
                        "<html>"
                                + "<b>Mã sách:</b> "
                                + book.getBookId()
                                + "<br>"
                                + "<b>Tên sách:</b> "
                                + book.getTitle()
                                + "<br>"
                                + "<b>Tác giả:</b> "
                                + book.getAuthor()
                                + "<br>"
                                + "<b>Số lượng còn:</b> "
                                + book.getStockQuantity()
                                + "<br><br>"
                                + "<b>Mô tả:</b>"
                                + "</html>"
                );

        panelChiTiet.add(
                lblThongTin,
                BorderLayout.NORTH
        );

        panelChiTiet.add(
                scrollPane,
                BorderLayout.CENTER
        );

        // Hiển thị cửa sổ chi tiết
        JOptionPane.showMessageDialog(
                this,
                panelChiTiet,
                "Chi tiết sách",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Hiển thị một kết quả vào bảng
     */
    public void hienThiKetQua(
            String maSach,
            String tenSach,
            String tacGia,
            int soLuong
    ) {

        tableModel.addRow(
            new Object[]{
                    maSach,
                    tenSach,
                    tacGia,
                    soLuong
            }
        );
    }

    /**
     * Xóa kết quả cũ
     */
    public void xoaKetQuaCu() {

        tableModel.setRowCount(0);

        danhSachSachHienTai = null;
    }

    private JPanel taoThanhThongTinSinhVien() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || !session.isStudent()) {
            return null;
        }

        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(new Color(248, 250, 252));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));

        // Bên trái: Tên sinh viên, MSSV và Trạng thái nợ
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        infoPanel.setOpaque(false);

        JLabel lblName = new JLabel("Sinh viên: " + session.getFullName() + " (" + session.getUserId() + ")");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(new Color(30, 41, 59));

        lblDebtInfo = new JLabel();
        lblDebtInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));

        infoPanel.add(lblName);
        infoPanel.add(lblDebtInfo);

        // Bên phải: Nút thanh toán nợ và Nút Đăng xuất
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        btnPayDebt = createStyledButton("Thanh toán tiền phạt", new Color(39, 174, 96), Color.WHITE);
        btnPayDebt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPayDebt.setPreferredSize(new Dimension(190, 32));
        btnPayDebt.addActionListener(e -> xuLyThanhToanNoOnline());

        JButton btnLogout = createStyledButton("Đăng xuất", new Color(241, 245, 249), new Color(30, 41, 59));
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLogout.setPreferredSize(new Dimension(95, 32));
        btnLogout.addActionListener(e -> xuLyDangXuat());

        actionPanel.add(btnPayDebt);
        actionPanel.add(btnLogout);

        bar.add(infoPanel, BorderLayout.WEST);
        bar.add(actionPanel, BorderLayout.EAST);

        capNhatDuLieuNoSinhVien();

        return bar;
    }

    private void capNhatDuLieuNoSinhVien() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || !session.isStudent() || lblDebtInfo == null) return;

        try {
            DebtController debtController = new DebtController();
            Student student = debtController.layThongTinNoSinhVien(session.getUserId());
            double debt = (student != null) ? student.getDebtAmount() : 0.0;

            if (debt > 0) {
                lblDebtInfo.setText("|  Nợ phạt: " + String.format("%,.0f VNĐ", debt));
                lblDebtInfo.setForeground(new Color(220, 38, 38));
                btnPayDebt.setText("Thanh toán nợ (" + String.format("%,.0f đ", debt) + ")");
                btnPayDebt.setVisible(true);
            } else {
                lblDebtInfo.setText("|  Nợ phạt: 0 VNĐ (Không nợ)");
                lblDebtInfo.setForeground(new Color(22, 101, 52));
                btnPayDebt.setVisible(false);
            }
        } catch (Exception e) {
            lblDebtInfo.setText("|  Nợ phạt: 0 VNĐ");
            btnPayDebt.setVisible(false);
        }
    }

    private void xuLyThanhToanNoOnline() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || !session.isStudent()) return;

        try {
            DebtController debtController = new DebtController();
            Student student = debtController.layThongTinNoSinhVien(session.getUserId());
            if (student == null || student.getDebtAmount() <= 0) {
                JOptionPane.showMessageDialog(this, "Bạn không có nợ tiền phạt cần thanh toán.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            double debt = student.getDebtAmount();
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Xác nhận thanh toán toàn bộ " + String.format("%,.0f VNĐ", debt) + " tiền phạt qua cổng trực tuyến?",
                    "Xác nhận thanh toán online",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                DebtPaymentResult result = debtController.thanhToanNo(
                        student.getStudentId(),
                        debt,
                        "ONLINE",
                        student.getStudentId()
                );

                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Thanh toán tiền phạt trực tuyến thành công!\n"
                                    + "Số nợ hiện tại của bạn: 0 VNĐ.\n"
                                    + "Tài khoản hiện đã đủ điều kiện mượn sách tại thư viện.",
                            "Thanh toán thành công",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    capNhatDuLieuNoSinhVien();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Lỗi khi thực hiện thanh toán: " + result.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xử lý thanh toán: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyDangXuat() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn đăng xuất?",
                "Đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        }
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

    /**
     * Chạy thử giao diện
     */
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(
                () -> {

                    SearchBookForm form =
                            new SearchBookForm();

                    form.setVisible(true);
                }
        );
    }
}