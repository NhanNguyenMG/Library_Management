package ui;

import controller.DebtController;
import model.DebtPaymentResult;
import model.Student;
import service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Màn hình Dashboard (Bảng điều khiển trung tâm)
 */
public class DashboardForm extends JFrame {

    // Bảng màu giao diện chuẩn hiện đại
    private static final Color COLOR_SIDEBAR = new Color(26, 37, 54);          // #1A2536
    private static final Color COLOR_SIDEBAR_HOVER = new Color(38, 52, 75);    // #26344B
    private static final Color COLOR_SIDEBAR_TEXT = new Color(203, 213, 225);   // #CBD5E1
    private static final Color COLOR_MUTED = new Color(108, 125, 147);          // #6C7D93
    private static final Color COLOR_BG = new Color(248, 250, 252);             // #F8FAFC
    private static final Color COLOR_CARD_BORDER = new Color(226, 232, 240);    // #E2E8F0
    private static final Color COLOR_TEXT_MAIN = new Color(30, 41, 59);         // #1E293B
    private static final Color COLOR_TEXT_SUB = new Color(100, 116, 139);       // #64748B
    private static final Color COLOR_SUCCESS = new Color(16, 185, 129);         // #10B981
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);          // #2563EB

    private JPanel sessionStatusCard;
    private SidebarMenuItem navSessionItem;

    public DashboardForm() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Hệ thống quản lý thư viện");
        setSize(1040, 660);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(COLOR_BG);

        // 1. Sidebar bên trái (West)
        JPanel sidebarPanel = createSidebarPanel();
        rootPanel.add(sidebarPanel, BorderLayout.WEST);

        // 2. Khu vực bên phải gồm Header (North) và Nội dung chính (Center)
        JPanel mainAreaPanel = new JPanel(new BorderLayout());
        mainAreaPanel.setBackground(COLOR_BG);

        JPanel headerPanel = createHeaderPanel();
        mainAreaPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = createContentPanel();
        mainAreaPanel.add(contentPanel, BorderLayout.CENTER);

        rootPanel.add(mainAreaPanel, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    /**
     * Tạo Sidebar thanh điều hướng bên trái
     */
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(COLOR_SIDEBAR);

        // Phần trên của Sidebar
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(COLOR_SIDEBAR);
        topContainer.setBorder(new EmptyBorder(25, 18, 20, 18));

        // Logo thương hiệu
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoPanel.setBackground(COLOR_SIDEBAR);
        logoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Icon vuông kép biểu tượng hệ thống (Vẽ Vector 2D, chống lỗi font)
        JComponent logoIcon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawRoundRect(2, 2, 26, 26, 6, 6);
                g2.drawRoundRect(8, 8, 14, 14, 3, 3);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(32, 32);
            }
        };

        JPanel logoTextPanel = new JPanel();
        logoTextPanel.setLayout(new BoxLayout(logoTextPanel, BoxLayout.Y_AXIS));
        logoTextPanel.setBackground(COLOR_SIDEBAR);
        logoTextPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblBrandTitle = new JLabel("LIBRARY");
        lblBrandTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblBrandTitle.setForeground(Color.WHITE);
        lblBrandTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblBrandSubtitle = new JLabel("MANAGEMENT SYSTEM");
        lblBrandSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 8));
        lblBrandSubtitle.setForeground(new Color(140, 160, 185));
        lblBrandSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoTextPanel.add(lblBrandTitle);
        logoTextPanel.add(Box.createVerticalStrut(2));
        logoTextPanel.add(lblBrandSubtitle);

        logoPanel.add(logoIcon);
        logoPanel.add(logoTextPanel);
        topContainer.add(logoPanel);

        topContainer.add(createStrut(28));

        // Tiêu đề nhóm "CHỨC NĂNG"
        JLabel lblSectionFeatures = new JLabel("CHỨC NĂNG");
        lblSectionFeatures.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSectionFeatures.setForeground(COLOR_MUTED);
        lblSectionFeatures.setBorder(new EmptyBorder(0, 10, 0, 0));
        lblSectionFeatures.setAlignmentX(Component.LEFT_ALIGNMENT);
        topContainer.add(lblSectionFeatures);

        topContainer.add(createStrut(10));

        SessionManager session = SessionManager.getInstance();

        // Nút Tra cứu sách (Sử dụng vector icon Search)
        SidebarMenuItem btnNavSearch = new SidebarMenuItem(
                createSearchIcon(16, COLOR_SIDEBAR_TEXT),
                "Tra cứu sách",
                this::openSearchBook
        );
        topContainer.add(btnNavSearch);
        topContainer.add(createStrut(4));

        // Chỉ hiển thị Mượn sách và Trả sách đối với Quản lý và Thủ thư
        if (!session.isStudent()) {
            // Nút Mượn sách (Sử dụng vector icon Plus)
            SidebarMenuItem btnNavBorrow = new SidebarMenuItem(
                    createPlusIcon(16, COLOR_SIDEBAR_TEXT),
                    "Mượn sách",
                    this::openBorrowBook
            );
            topContainer.add(btnNavBorrow);
            topContainer.add(createStrut(4));

            // Nút Trả sách (Sử dụng vector icon Return)
            SidebarMenuItem btnNavReturn = new SidebarMenuItem(
                    createReturnIcon(16, COLOR_SIDEBAR_TEXT),
                    "Trả sách",
                    this::openReturnBook
            );
            topContainer.add(btnNavReturn);
            topContainer.add(createStrut(4));
        }

        topContainer.add(createStrut(24));

        // Tiêu đề nhóm "TÀI KHOẢN"
        JLabel lblSectionAccount = new JLabel("TÀI KHOẢN");
        lblSectionAccount.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSectionAccount.setForeground(COLOR_MUTED);
        lblSectionAccount.setBorder(new EmptyBorder(0, 10, 0, 0));
        lblSectionAccount.setAlignmentX(Component.LEFT_ALIGNMENT);
        topContainer.add(lblSectionAccount);

        topContainer.add(createStrut(10));

        // Nút Thông tin phiên (Sử dụng vector icon Dot, có phản hồi khi click)
        navSessionItem = new SidebarMenuItem(
                createDotIcon(14, COLOR_SUCCESS),
                "Thông tin phiên",
                this::showSessionDetails
        );
        navSessionItem.setActive(true);
        topContainer.add(navSessionItem);

        sidebar.add(topContainer, BorderLayout.NORTH);

        // Phần dưới của Sidebar: Nút Đăng xuất
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(COLOR_SIDEBAR);
        bottomContainer.setBorder(new EmptyBorder(15, 18, 20, 18));

        JPanel btnLogout = createLogoutButton();
        bottomContainer.add(btnLogout, BorderLayout.CENTER);

        sidebar.add(bottomContainer, BorderLayout.SOUTH);
        return sidebar;
    }

    /**
     * Tạo Header thanh công cụ phía trên
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 75));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_CARD_BORDER),
                new EmptyBorder(14, 28, 14, 28)
        ));

        SessionManager session = SessionManager.getInstance();
        String fullName = session.isLoggedIn() ? session.getFullName() : "Người quản trị";
        String displayNameWithRole = session.isLoggedIn() ? session.getDisplayNameWithRole() : "Chưa đăng nhập";

        // Bên trái: Lời chào người dùng
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);

        JLabel lblGreeting = new JLabel("Xin chào, " + fullName);
        lblGreeting.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblGreeting.setForeground(COLOR_TEXT_MAIN);

        JLabel lblSubInfo = new JLabel(displayNameWithRole);
        lblSubInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubInfo.setForeground(COLOR_TEXT_SUB);

        leftPanel.add(lblGreeting);
        leftPanel.add(Box.createVerticalStrut(2));
        leftPanel.add(lblSubInfo);

        // Bên phải: Trạng thái hệ thống đang hoạt động
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 12));
        rightPanel.setBackground(Color.WHITE);

        JLabel lblStatusDot = new JLabel(createDotIcon(10, COLOR_SUCCESS));
        JLabel lblStatusText = new JLabel("Hệ thống đang hoạt động");
        lblStatusText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatusText.setForeground(COLOR_SUCCESS);

        rightPanel.add(lblStatusDot);
        rightPanel.add(lblStatusText);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    /**
     * Tạo khu vực nội dung chính ở giữa (phân chia theo vai trò Sinh viên hoặc Cán bộ thư viện)
     */
    private JPanel createContentPanel() {
        SessionManager session = SessionManager.getInstance();
        if (session.isStudent()) {
            return createStudentContentPanel();
        }
        return createStaffContentPanel();
    }

    /**
     * Panel dành cho Cán bộ thư viện (Thủ thư / Quản lý)
     */
    private JPanel createStaffContentPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(COLOR_BG);
        content.setBorder(new EmptyBorder(25, 28, 25, 28));

        // 1. Tiêu đề tổng quan
        JLabel lblTitle = new JLabel("Tổng quan thư viện");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_TEXT_MAIN);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Chọn một chức năng để bắt đầu làm việc.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(COLOR_TEXT_SUB);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(lblTitle);
        content.add(Box.createVerticalStrut(4));
        content.add(lblSubtitle);
        content.add(Box.createVerticalStrut(22));

        // 2. Hàng 3 thẻ chức năng nhanh (TRA CỨU, MƯỢN SÁCH, TRẢ SÁCH)
        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 18, 0));
        cardsRow.setBackground(COLOR_BG);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Thẻ 1: Tra cứu (Vector Icon kính lúp)
        JPanel cardSearch = createActionCard(
                "TRA CỨU",
                "Tra cứu sách theo mã, tên hoặc tác giả",
                new Color(239, 246, 255), // Nền xanh dương nhạt #EFF6FF
                new Color(37, 99, 235),   // Chữ / icon xanh dương #2563EB
                createSearchIcon(22, new Color(37, 99, 235)),
                this::openSearchBook
        );

        // Thẻ 2: Mượn sách (Vector Icon dấu cộng)
        JPanel cardBorrow = createActionCard(
                "MƯỢN SÁCH",
                "Quản lý quy trình mượn tài liệu",
                new Color(236, 253, 245), // Nền xanh lá nhạt #ECFDF5
                new Color(5, 150, 105),   // Chữ / icon xanh lá #059669
                createPlusIcon(22, new Color(5, 150, 105)),
                this::openBorrowBook
        );

        // Thẻ 3: Trả sách (Vector Icon mũi tên quay lại)
        JPanel cardReturn = createActionCard(
                "TRẢ SÁCH",
                "Trả sách và xử lý tiền phạt",
                new Color(254, 243, 199), // Nền cam vàng nhạt #FEF3C7
                new Color(217, 119, 6),   // Chữ / icon cam vàng #D97706
                createReturnIcon(22, new Color(217, 119, 6)),
                this::openReturnBook
        );

        cardsRow.add(cardSearch);
        cardsRow.add(cardBorrow);
        cardsRow.add(cardReturn);
        content.add(cardsRow);

        content.add(Box.createVerticalStrut(25));

        // 3. Khung Trạng thái phiên làm việc
        sessionStatusCard = createSessionStatusCard();
        sessionStatusCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sessionStatusCard);

        return content;
    }

    /**
     * Panel dành riêng cho Sinh viên: Xem thông tin mượn và thanh toán nợ phạt trực tuyến
     */
    private JPanel createStudentContentPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(COLOR_BG);
        content.setBorder(new EmptyBorder(25, 28, 25, 28));

        // 1. Tiêu đề
        JLabel lblTitle = new JLabel("Cổng thông tin Sinh viên");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_TEXT_MAIN);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Tra cứu tài liệu thư viện & Quản lý thông tin mượn trả, nợ phạt.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(COLOR_TEXT_SUB);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(lblTitle);
        content.add(Box.createVerticalStrut(4));
        content.add(lblSubtitle);
        content.add(Box.createVerticalStrut(22));

        // 2. Tải dữ liệu sinh viên từ Database
        Student student = loadCurrentStudentData();
        int borrowedCount = (student != null) ? student.getBorrowedCount() : 0;
        double debt = (student != null) ? student.getDebtAmount() : 0.0;

        // 3. Hàng thẻ tóm tắt
        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 18, 0));
        cardsRow.setBackground(COLOR_BG);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Thẻ 1: Tra cứu sách
        JPanel cardSearch = createActionCard(
                "TRA CỨU SÁCH",
                "Tìm kiếm sách & tài liệu trong kho",
                new Color(239, 246, 255),
                new Color(37, 99, 235),
                createSearchIcon(22, new Color(37, 99, 235)),
                this::openSearchBook
        );

        // Thẻ 2: Số sách đang mượn
        JPanel cardBorrow = createStatCard(
                "SÁCH ĐANG MƯỢN",
                borrowedCount + " cuốn",
                new Color(236, 253, 245),
                new Color(5, 150, 105),
                createPlusIcon(20, new Color(5, 150, 105))
        );

        // Thẻ 3: Tiền phạt đang nợ
        JPanel cardDebt = createStatCard(
                "TIỀN PHẠT ĐANG NỢ",
                String.format("%,.0f VNĐ", debt),
                debt > 0 ? new Color(254, 242, 242) : new Color(240, 253, 244),
                debt > 0 ? new Color(220, 38, 38) : new Color(22, 163, 74),
                createPaymentIcon(20, debt > 0 ? new Color(220, 38, 38) : new Color(22, 163, 74))
        );

        cardsRow.add(cardSearch);
        cardsRow.add(cardBorrow);
        cardsRow.add(cardDebt);
        content.add(cardsRow);

        content.add(Box.createVerticalStrut(20));

        // 4. Khung Thanh toán nợ phạt trực tuyến
        JPanel debtPayCard = createStudentDebtPaymentCard(student);
        debtPayCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(debtPayCard);

        content.add(Box.createVerticalStrut(20));

        // 5. Khung Trạng thái phiên làm việc
        sessionStatusCard = createSessionStatusCard();
        sessionStatusCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sessionStatusCard);

        return content;
    }

    private JPanel createStatCard(String title, String value, Color iconBgColor, Color textColor, Icon icon) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_CARD_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JPanel iconBox = new JPanel(new GridBagLayout());
        iconBox.setPreferredSize(new Dimension(48, 48));
        iconBox.setBackground(iconBgColor);
        iconBox.setBorder(new LineBorder(iconBgColor, 1, true));
        if (icon != null) {
            iconBox.add(new JLabel(icon));
        }

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(COLOR_TEXT_SUB);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValue.setForeground(textColor);

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(lblValue);
        textPanel.add(Box.createVerticalGlue());

        card.add(iconBox, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createStudentDebtPaymentCard(Student student) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_CARD_BORDER, 1, true),
                new EmptyBorder(20, 22, 20, 22)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 175));

        // Header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);

        JLabel lblHeaderTitle = new JLabel("Thanh toán tiền phạt trực tuyến (Demo)");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeaderTitle.setForeground(COLOR_TEXT_MAIN);

        double debt = (student != null) ? student.getDebtAmount() : 0.0;
        JLabel lblStatusBadge = new JLabel(debt > 0 ? "● Đang có nợ phạt" : "● Không có nợ phạt");
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatusBadge.setForeground(debt > 0 ? new Color(220, 38, 38) : COLOR_SUCCESS);

        headerRow.add(lblHeaderTitle, BorderLayout.WEST);
        headerRow.add(lblStatusBadge, BorderLayout.EAST);
        card.add(headerRow);

        card.add(Box.createVerticalStrut(14));

        String studentId = (student != null) ? student.getStudentId() : "—";
        String studentName = (student != null) ? student.getFullName() : "—";
        card.add(createSessionInfoRow("Sinh viên: ", studentId + " - " + studentName));
        card.add(Box.createVerticalStrut(6));
        card.add(createSessionInfoRow("Tiền phạt chưa thanh toán: ", String.format("%,.0f VNĐ", debt)));
        card.add(Box.createVerticalStrut(14));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionRow.setBackground(Color.WHITE);

        if (debt > 0) {
            JButton btnPay = createStyledButton("Thanh toán tiền phạt trực tuyến (" + String.format("%,.0f VNĐ", debt) + ")", new Color(39, 174, 96), Color.WHITE);
            btnPay.setPreferredSize(new Dimension(340, 38));
            btnPay.addActionListener(e -> handleStudentOnlinePayment(student));
            actionRow.add(btnPay);
        } else {
            JButton btnPaid = createStyledButton("Đã thanh toán hết nợ phạt (0 đ)", new Color(241, 245, 249), new Color(100, 116, 139));
            btnPaid.setPreferredSize(new Dimension(260, 38));
            btnPaid.setEnabled(false);
            actionRow.add(btnPaid);
        }

        card.add(actionRow);
        return card;
    }

    private Student loadCurrentStudentData() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || !session.isStudent()) {
            return null;
        }
        String studentId = session.getUserId();
        if (studentId == null || studentId.trim().isEmpty()) {
            return null;
        }
        try {
            DebtController debtController = new DebtController();
            return debtController.layThongTinNoSinhVien(studentId.trim());
        } catch (Exception e) {
            System.err.println("[DashboardForm] Lỗi tải dữ liệu sinh viên: " + e.getMessage());
            return null;
        }
    }

    private void handleStudentOnlinePayment(Student student) {
        if (student == null) return;
        double debt = student.getDebtAmount();
        if (debt <= 0) {
            JOptionPane.showMessageDialog(this, "Bạn không có nợ tiền phạt cần thanh toán.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận thanh toán toàn bộ " + String.format("%,.0f VNĐ", debt) + " tiền phạt qua cổng trực tuyến?",
                "Xác nhận thanh toán online",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            DebtController debtController = new DebtController();
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
                refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Lỗi khi thực hiện thanh toán: " + result.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void refreshDashboard() {
        getContentPane().removeAll();
        initComponents();
        revalidate();
        repaint();
    }

    /**
     * Tạo từng thẻ chức năng nhanh (Quick Action Card) với Icon Vector chuẩn
     */
    private JPanel createActionCard(String title, String subtitle, Color iconBgColor, Color iconFgColor, Icon icon, Runnable onClickAction) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_CARD_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon hộp vuông màu sắc bên trái
        JPanel iconBox = new JPanel(new GridBagLayout());
        iconBox.setPreferredSize(new Dimension(48, 48));
        iconBox.setBackground(iconBgColor);
        iconBox.setBorder(new LineBorder(iconBgColor, 1, true));

        JLabel lblIcon = new JLabel(icon);
        iconBox.add(lblIcon);

        // Nội dung tiêu đề và mô tả bên phải
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        JLabel lblCardTitle = new JLabel(title);
        lblCardTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCardTitle.setForeground(COLOR_TEXT_MAIN);

        JLabel lblCardDesc = new JLabel(subtitle);
        lblCardDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCardDesc.setForeground(COLOR_TEXT_SUB);

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(lblCardTitle);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(lblCardDesc);
        textPanel.add(Box.createVerticalGlue());

        card.add(iconBox, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        // Hiệu ứng hover và sự kiện click
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(iconFgColor, 1, true),
                        new EmptyBorder(16, 16, 16, 16)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(COLOR_CARD_BORDER, 1, true),
                        new EmptyBorder(16, 16, 16, 16)
                ));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onClickAction.run();
            }
        });

        return card;
    }

    /**
     * Tạo khối hiển thị "Trạng thái phiên làm việc"
     */
    private JPanel createSessionStatusCard() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_CARD_BORDER, 1, true),
                new EmptyBorder(20, 22, 20, 22)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Header kèm nút xem chi tiết
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);

        JLabel lblCardHeader = new JLabel("Trạng thái phiên làm việc");
        lblCardHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCardHeader.setForeground(COLOR_TEXT_MAIN);

        JLabel lblDetailsHint = new JLabel("Nhấn để xem chi tiết >>");
        lblDetailsHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetailsHint.setForeground(COLOR_PRIMARY);

        headerRow.add(lblCardHeader, BorderLayout.WEST);
        headerRow.add(lblDetailsHint, BorderLayout.EAST);
        panel.add(headerRow);

        panel.add(Box.createVerticalStrut(14));

        SessionManager session = SessionManager.getInstance();
        String user = session.isLoggedIn() ? session.getFullName() : "Chưa đăng nhập";
        String role = session.isLoggedIn() ? session.getDisplayNameWithRole() : "Không có";
        String loginTime = session.isLoggedIn() && session.getLoginTime() != null
                ? new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT+07:00' yyyy").format(session.getLoginTime())
                : "Không xác định";

        panel.add(createSessionInfoRow("Người dùng: ", user));
        panel.add(Box.createVerticalStrut(6));
        panel.add(createSessionInfoRow("Vai trò: ", role));
        panel.add(Box.createVerticalStrut(6));
        panel.add(createSessionInfoRow("Đăng nhập lúc: ", loginTime));

        // Click vào card cũng mở chi tiết phiên
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(COLOR_PRIMARY, 1, true),
                        new EmptyBorder(20, 22, 20, 22)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(COLOR_CARD_BORDER, 1, true),
                        new EmptyBorder(20, 22, 20, 22)
                ));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showSessionDetails();
            }
        });

        return panel;
    }

    private JPanel createSessionInfoRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setBackground(Color.WHITE);

        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKey.setForeground(COLOR_TEXT_SUB);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblVal.setForeground(COLOR_TEXT_MAIN);

        row.add(lblKey);
        row.add(lblVal);
        return row;
    }

    /**
     * Tạo nút Đăng xuất dưới chân Sidebar (Hoàn toàn bằng JPanel, không bị lỗi màu Windows button)
     */
    private JPanel createLogoutButton() {
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        btn.setBackground(new Color(30, 42, 60));
        btn.setBorder(new LineBorder(new Color(44, 62, 85), 1, true));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 38));

        JLabel lblIcon = new JLabel(createLogoutIcon(15, Color.WHITE));
        JLabel lblText = new JLabel("Đăng xuất");
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblText.setForeground(Color.WHITE);

        btn.add(lblIcon);
        btn.add(lblText);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(220, 38, 38)); // Màu đỏ cảnh báo khi hover
                btn.setBorder(new LineBorder(new Color(239, 68, 68), 1, true));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(30, 42, 60));
                btn.setBorder(new LineBorder(new Color(44, 62, 85), 1, true));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleLogout();
            }
        });

        return btn;
    }

    private static Component createStrut(int height) {
        Component strut = Box.createVerticalStrut(height);
        if (strut instanceof JComponent jc) {
            jc.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        return strut;
    }

    // Lớp thành phần menu sidebar (SidebarMenuItem)
    private static class SidebarMenuItem extends JPanel {
        private boolean active = false;
        private final JLabel lblIcon;
        private final JLabel lblText;
        private final Runnable clickAction;

        public SidebarMenuItem(Icon icon, String text, Runnable action) {
            this.clickAction = action;
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
            setBackground(COLOR_SIDEBAR);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            lblIcon = new JLabel(icon);
            lblText = new JLabel(text);
            lblText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblText.setForeground(COLOR_SIDEBAR_TEXT);

            add(lblIcon);
            add(lblText);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!active) {
                        setBackground(COLOR_SIDEBAR_HOVER);
                        lblText.setForeground(Color.WHITE);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!active) {
                        setBackground(COLOR_SIDEBAR);
                        lblText.setForeground(COLOR_SIDEBAR_TEXT);
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (clickAction != null) {
                        clickAction.run();
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            if (active) {
                setBackground(COLOR_SIDEBAR_HOVER);
                lblText.setForeground(Color.WHITE);
                lblText.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                setBackground(COLOR_SIDEBAR);
                lblText.setForeground(COLOR_SIDEBAR_TEXT);
                lblText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
            repaint();
        }
    }

    // Điều hướng các chức năng
    private void openSearchBook() {
        SwingUtilities.invokeLater(() -> {
            SearchBookForm form = new SearchBookForm();
            form.setVisible(true);
        });
    }

    private void openBorrowBook() {
        SwingUtilities.invokeLater(() -> {
            BorrowForm form = new BorrowForm();
            form.setVisible(true);
        });
    }

    private void openReturnBook() {
        SwingUtilities.invokeLater(() -> {
            ReturnBookForm form = new ReturnBookForm();
            form.setVisible(true);
        });
    }

    /**
     * Hiển thị phản hồi khi click "Thông tin phiên":
     * 1. Hiệu ứng viền nổi bật tại Card trạng thái
     * 2. Mở Hộp thoại chi tiết phiên làm việc
     */
    private void showSessionDetails() {
        // 1. Hiệu ứng nhấp nháy làm nổi bật card trên giao diện
        if (sessionStatusCard != null) {
            sessionStatusCard.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_PRIMARY, 2, true),
                    new EmptyBorder(19, 21, 19, 21)
            ));
            Timer timer = new Timer(1200, evt -> {
                sessionStatusCard.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(COLOR_CARD_BORDER, 1, true),
                        new EmptyBorder(20, 22, 20, 22)
                ));
            });
            timer.setRepeats(false);
            timer.start();
        }

        // 2. Mở Modal Dialog thông tin chi tiết phiên
        JDialog dialog = new JDialog(this, "Chi tiết phiên làm việc", true);
        dialog.setSize(480, 430);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header Dialog
        JLabel lblDlgTitle = new JLabel("THÔNG TIN PHIÊN ĐĂNG NHẬP");
        lblDlgTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblDlgTitle.setForeground(COLOR_TEXT_MAIN);
        lblDlgTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblDlgTitle);

        panel.add(Box.createVerticalStrut(15));

        SessionManager session = SessionManager.getInstance();
        String user = session.isLoggedIn() ? session.getFullName() : "Chưa đăng nhập";
        String username = session.isLoggedIn() ? session.getUsername() : "Chưa đăng nhập";
        String userId = session.isLoggedIn() && session.getUserId() != null ? session.getUserId() : "N/A";
        String role = session.isLoggedIn() ? session.getDisplayNameWithRole() : "Chưa có";
        String email = session.isLoggedIn() && session.getEmail() != null ? session.getEmail() : "Chưa cập nhật";
        String loginTime = session.isLoggedIn() && session.getLoginTime() != null
                ? new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy").format(session.getLoginTime())
                : "Không xác định";

        String permissionDesc = session.isManager()
                ? "Toàn quyền quản trị, lập phiếu mượn, xử lý trả sách, tra cứu tài liệu."
                : (session.isLibrarian()
                ? "Lập phiếu mượn sách, xử lý trả sách & tính tiền phạt, tra cứu tài liệu."
                : "Tra cứu tài liệu trực tuyến.");

        panel.add(createModalRow("Họ và tên:", user));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createModalRow("Tên đăng nhập:", username));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createModalRow("Mã định danh:", userId));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createModalRow("Vai trò:", role));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createModalRow("Email:", email));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createModalRow("Thời gian đăng nhập:", loginTime));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createModalRow("Trạng thái:", "● Đang hoạt động (ACTIVE)"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createModalRow("Quyền hạn:", permissionDesc));

        panel.add(Box.createVerticalStrut(20));

        JButton btnClose = createStyledButton("Đóng", new Color(241, 245, 249), new Color(30, 41, 59));
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.setMaximumSize(new Dimension(100, 35));
        btnClose.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClose.addActionListener(e -> dialog.dispose());
        panel.add(btnClose);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private JPanel createModalRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblKey.setForeground(COLOR_TEXT_SUB);
        lblKey.setPreferredSize(new Dimension(140, 20));

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblVal.setForeground(label.contains("Trạng thái") ? COLOR_SUCCESS : COLOR_TEXT_MAIN);

        row.add(lblKey, BorderLayout.WEST);
        row.add(lblVal, BorderLayout.CENTER);
        return row;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginForm loginForm = new LoginForm();
                loginForm.setVisible(true);
            });
        }
    }

    // Các hàm tạo vector icon tự vẽ bằng Graphics2D
    public static Icon createSearchIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(size >= 20 ? 2.3f : 1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int d = (int) (size * 0.55);
                int ox = x + (int) (size * 0.12);
                int oy = y + (int) (size * 0.12);
                g2.drawOval(ox, oy, d, d);

                int hx1 = ox + (int) (d * 0.82);
                int hy1 = oy + (int) (d * 0.82);
                int hx2 = x + size - 3;
                int hy2 = y + size - 3;
                g2.drawLine(hx1, hy1, hx2, hy2);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }

    public static Icon createPlusIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(size >= 20 ? 2.6f : 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int midX = x + size / 2;
                int midY = y + size / 2;
                int pad = (int) (size * 0.22);

                g2.drawLine(x + pad, midY, x + size - pad, midY);
                g2.drawLine(midX, y + pad, midX, y + size - pad);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }

    public static Icon createReturnIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(size >= 20 ? 2.2f : 1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int left = x + (int) (size * 0.18);
                int right = x + (int) (size * 0.78);
                int top = y + (int) (size * 0.28);
                int bottom = y + (int) (size * 0.74);

                // Đường cong quay đầu
                Path2D path = new Path2D.Float();
                path.moveTo(right, bottom);
                path.lineTo(right, top + (bottom - top) / 2);
                path.quadTo(right, top, right - (right - left) / 2, top);
                path.lineTo(left + 2, top);
                g2.draw(path);

                // Mũi tên chỉ sang trái
                g2.drawLine(left, top, left + (int) (size * 0.24), top - (int) (size * 0.20));
                g2.drawLine(left, top, left + (int) (size * 0.24), top + (int) (size * 0.20));

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }

    public static Icon createDotIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                int d = (int) (size * 0.55);
                g2.fillOval(x + (size - d) / 2, y + (size - d) / 2, d, d);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }

    public static Icon createPaymentIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(size >= 20 ? 2.0f : 1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int w = (int) (size * 0.82);
                int h = (int) (size * 0.60);
                int startX = x + (size - w) / 2;
                int startY = y + (size - h) / 2;

                // Thẻ ngân hàng / Tiền mặt
                g2.drawRoundRect(startX, startY, w, h, 3, 3);
                g2.drawLine(startX, startY + (int) (h * 0.38), startX + w, startY + (int) (h * 0.38));
                g2.fillOval(startX + (int) (w * 0.18), startY + (int) (h * 0.62), (int)(size * 0.15), (int)(size * 0.15));
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }

    public static Icon createLogoutIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int midX = x + size / 2;
                int midY = y + size / 2;
                int r = (int) (size * 0.35);

                // Vẽ cung tròn biểu tượng Power / Exit
                g2.drawArc(midX - r, midY - r, r * 2, r * 2, -55, 290);
                // Vạch đứng trên cùng
                g2.drawLine(midX, midY - r - 2, midX, midY - 1);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
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
     * Điểm chạy thử độc lập Dashboard
     */
    public static void main(String[] args) {
        // Kích hoạt khử răng cưa chữ (Anti-Aliasing) trên toàn hệ thống Swing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Mock session nếu khởi chạy trực tiếp
        if (!SessionManager.getInstance().isLoggedIn()) {
            SessionManager.getInstance().login(
                    "admin",
                    SessionManager.ROLE_MANAGER,
                    "QL0001",
                    "Nguyễn Văn Quản Trị",
                    "quantri@thuvien.edu.vn"
            );
        }

        SwingUtilities.invokeLater(() -> {
            DashboardForm dashboard = new DashboardForm();
            dashboard.setVisible(true);
        });
    }
}
