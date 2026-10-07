package ui;

import controller.BorrowController;
import model.Book;
import model.BorrowResult;
import model.Student;
import service.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Giao diện Tạo phiếu mượn sách
 */
public class BorrowForm extends JFrame {

    // Bảng màu dùng chung với ReturnBookForm
    private static final Color COLOR_BACKGROUND = new Color(245, 246, 248);
    private static final Color COLOR_ERROR_BG = new Color(253, 237, 236);
    private static final Color COLOR_ERROR_BORDER = new Color(231, 76, 60);
    private static final Color COLOR_ERROR_TEXT = new Color(192, 57, 43);
    private static final Color COLOR_WARNING_BG = new Color(254, 245, 222);
    private static final Color COLOR_WARNING_BORDER = new Color(243, 156, 18);
    private static final Color COLOR_WARNING_TEXT = new Color(156, 100, 12);
    private static final Color COLOR_CONFIRM_ENABLED = new Color(39, 174, 96);
    private static final Color COLOR_CONFIRM_DISABLED = new Color(163, 217, 165);
    private static final Color COLOR_CANCEL = new Color(220, 222, 225);

    private static final String DELETE_BUTTON_TEXT = "Xóa";
    private static final int COLUMN_DELETE = 3;

    private final BorrowController borrowController;

    // Trạng thái dữ liệu trên giao diện
    private Student currentStudent = null;
    private String eligibilityWarning = null;           // Cảnh báo sắp đạt giới hạn mượn
    private final List<Book> pendingBooks = new ArrayList<>();

    // Header
    private JLabel lblLibrarianName;

    // Khu vực 1: Nhận diện sinh viên
    private JTextField txtStudentId;
    private JButton btnManualInput;
    private JLabel lblStudentNameValue;
    private JLabel lblBorrowedCountValue;
    private JLabel lblDebtAmountValue;

    // Khu vực 2: Banner cảnh báo
    private JPanel panelWarning;
    private JLabel lblWarningMessage;

    // Khu vực 3: Danh sách chờ mượn
    private JTextField txtBookId;
    private JButton btnAddBook;
    private JTable tblPendingBooks;
    private DefaultTableModel tableModel;
    private JLabel lblPendingCount;

    // Thanh nút
    private JButton btnConfirmBorrow;
    private JButton btnCancel;

    public BorrowForm() {
        this(new BorrowController());
    }

    public BorrowForm(BorrowController controller) {
        this.borrowController = controller;
        initComponents();
        onLoad();
    }

    // Khởi tạo giao diện
    private void initComponents() {
        setTitle("Hệ thống Quản lý Thư viện - Tạo phiếu mượn");
        setSize(960, 680);
        setMinimumSize(new Dimension(860, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainContainer = new JPanel(new BorderLayout(0, 10));
        mainContainer.setBorder(new EmptyBorder(12, 16, 12, 16));
        mainContainer.setBackground(COLOR_BACKGROUND);

        mainContainer.add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel(new BorderLayout(0, 8));
        bodyPanel.setOpaque(false);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.add(createArea1StudentIdentification());
        topPanel.add(Box.createVerticalStrut(8));
        topPanel.add(createArea2WarningBanner());

        bodyPanel.add(topPanel, BorderLayout.NORTH);
        bodyPanel.add(createArea3PendingList(), BorderLayout.CENTER);

        mainContainer.add(bodyPanel, BorderLayout.CENTER);
        mainContainer.add(createBottomActionPanel(), BorderLayout.SOUTH);

        setContentPane(mainContainer);
        updateActionStates();
    }

    /**
     * Header: tiêu đề bên trái, tên Thủ thư đang trực bên phải.
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(230, 232, 235));
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 203, 208), 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));

        JLabel lblHeaderTitle = new JLabel("Tạo phiếu mượn");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeaderTitle.setForeground(new Color(30, 30, 30));

        String librarianName = SessionManager.getInstance().isLoggedIn()
                ? SessionManager.getInstance().getFullName()
                : "Thủ thư";
        lblLibrarianName = new JLabel(librarianName, createDownChevronIcon(9, 6, new Color(80, 80, 80)), SwingConstants.LEFT);
        lblLibrarianName.setHorizontalTextPosition(SwingConstants.LEFT);
        lblLibrarianName.setIconTextGap(6);
        lblLibrarianName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLibrarianName.setForeground(new Color(60, 60, 60));

        panel.add(lblHeaderTitle, BorderLayout.WEST);
        panel.add(lblLibrarianName, BorderLayout.EAST);
        return panel;
    }

    private static Icon createDownChevronIcon(int width, int height, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int midX = x + width / 2;
                int topY = y + 2;
                int botY = y + height;
                g2.drawLine(x, topY, midX, botY);
                g2.drawLine(midX, botY, x + width, topY);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return width; }
            @Override
            public int getIconHeight() { return height + 4; }
        };
    }

    /**
     * Khu vực 1: Nhận diện sinh viên.
     */
    private JPanel createArea1StudentIdentification() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(createTitledSectionBorder("KHU VỰC 1 - NHẬN DIỆN SINH VIÊN"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Dòng 1: ô quét thẻ + nút nhập thủ công
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel lblStudentId = new JLabel("Mã thẻ Sinh viên (*)");
        lblStudentId.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lblStudentId, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        txtStudentId = new JTextField();
        txtStudentId.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtStudentId.setPreferredSize(new Dimension(300, 34));
        txtStudentId.setToolTipText("Quét / nhập mã thẻ sinh viên rồi nhấn Enter");
        txtStudentId.addActionListener(e -> onScanCardEnter());
        panel.add(txtStudentId, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        btnManualInput = new JButton("Nhập mã thủ công");
        btnManualInput.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnManualInput.setPreferredSize(new Dimension(160, 34));
        btnManualInput.setBackground(new Color(240, 242, 245));
        btnManualInput.setFocusPainted(false);
        btnManualInput.addActionListener(e -> onScanCardEnter());
        panel.add(btnManualInput, gbc);

        // Dòng 2: thông tin sinh viên sau khi kiểm tra điều kiện
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(createInfoLabel("Họ tên:"), gbc);
        gbc.gridx = 1;
        lblStudentNameValue = createValueLabel("—");
        panel.add(lblStudentNameValue, gbc);

        gbc.gridx = 2;
        panel.add(createInfoLabel("Đang mượn:"), gbc);
        gbc.gridx = 3;
        lblBorrowedCountValue = createValueLabel("—");
        panel.add(lblBorrowedCountValue, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(createInfoLabel("Nợ phạt:"), gbc);
        gbc.gridx = 1;
        lblDebtAmountValue = createValueLabel("—");
        panel.add(lblDebtAmountValue, gbc);

        return panel;
    }

    /**
     * Khu vực 2: Banner cảnh báo ẩn/hiện.
     */
    private JPanel createArea2WarningBanner() {
        panelWarning = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        lblWarningMessage = new JLabel(" ");
        lblWarningMessage.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panelWarning.add(lblWarningMessage);
        panelWarning.setVisible(false);

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(createTitledSectionBorder("KHU VỰC 2 - CẢNH BÁO (ẩn/hiện)"));
        container.add(panelWarning, BorderLayout.CENTER);
        return container;
    }

    /**
     * Khu vực 3: Danh sách chờ mượn.
     */
    private JPanel createArea3PendingList() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Color.WHITE);
        panel.setBorder(createTitledSectionBorder("KHU VỰC 3 - DANH SÁCH CHỜ MƯỢN"));

        // Ô quét mã sách
        JPanel scanPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        scanPanel.setOpaque(false);

        JLabel lblBookId = new JLabel("Mã sách (*)");
        lblBookId.setFont(new Font("Segoe UI", Font.BOLD, 14));

        txtBookId = new JTextField(22);
        txtBookId.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBookId.setPreferredSize(new Dimension(260, 34));
        txtBookId.setToolTipText("Quét / nhập mã sách rồi nhấn Enter");
        txtBookId.addActionListener(e -> onScanBookEnter());

        btnAddBook = new JButton("Thêm sách");
        btnAddBook.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnAddBook.setPreferredSize(new Dimension(120, 34));
        btnAddBook.setBackground(new Color(240, 242, 245));
        btnAddBook.setFocusPainted(false);
        btnAddBook.addActionListener(e -> onScanBookEnter());

        lblPendingCount = new JLabel("Số sách chờ mượn: 0");
        lblPendingCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPendingCount.setForeground(new Color(90, 95, 100));

        scanPanel.add(lblBookId);
        scanPanel.add(txtBookId);
        scanPanel.add(btnAddBook);
        scanPanel.add(Box.createHorizontalStrut(20));
        scanPanel.add(lblPendingCount);

        // Bảng sách chờ mượn
        String[] columnNames = {"Mã sách", "Tên sách", "Tác giả", "Thao tác"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == COLUMN_DELETE; // Chỉ cho phép bấm nút Xóa
            }
        };

        tblPendingBooks = new JTable(tableModel);
        tblPendingBooks.setRowHeight(36);
        tblPendingBooks.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblPendingBooks.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblPendingBooks.getTableHeader().setBackground(new Color(240, 242, 245));
        tblPendingBooks.getTableHeader().setReorderingAllowed(false);
        tblPendingBooks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblPendingBooks.getColumnModel().getColumn(0).setPreferredWidth(110);
        tblPendingBooks.getColumnModel().getColumn(1).setPreferredWidth(400);
        tblPendingBooks.getColumnModel().getColumn(2).setPreferredWidth(200);
        tblPendingBooks.getColumnModel().getColumn(3).setPreferredWidth(100);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblPendingBooks.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Nút "Xóa" trên từng dòng
        tblPendingBooks.getColumnModel().getColumn(COLUMN_DELETE).setCellRenderer(new DeleteButtonRenderer());
        tblPendingBooks.getColumnModel().getColumn(COLUMN_DELETE).setCellEditor(new DeleteButtonEditor());

        JScrollPane scrollPane = new JScrollPane(tblPendingBooks);
        scrollPane.setPreferredSize(new Dimension(880, 200));

        panel.add(scanPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Thanh nút phía dưới (căn phải): Hoàn tất mượn (xanh), Hủy (xám).
     */
    private JPanel createBottomActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        panel.setOpaque(false);

        btnConfirmBorrow = new JButton("Hoàn tất mượn");
        btnConfirmBorrow.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirmBorrow.setPreferredSize(new Dimension(170, 40));
        btnConfirmBorrow.setFocusPainted(false);
        btnConfirmBorrow.setContentAreaFilled(false);
        btnConfirmBorrow.setOpaque(true);
        btnConfirmBorrow.addActionListener(this::btnConfirmActionPerformed);

        btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.setBackground(COLOR_CANCEL);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(this::btnCancelActionPerformed);

        panel.add(btnConfirmBorrow);
        panel.add(btnCancel);
        return panel;
    }

    // Xử lý sự kiện

    /**
     * Sự kiện onLoad: Kiểm tra quyền truy cập và focus vào ô quét thẻ sinh viên.
     */
    private void onLoad() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLibrarian() && !session.isManager()) {
            JOptionPane.showMessageDialog(this,
                    "Bạn không có quyền truy cập chức năng này. Vui lòng đăng nhập với tài khoản Thủ thư hoặc Quản lý.",
                    "Từ chối truy cập", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(this::dispose);
            return;
        }
        SwingUtilities.invokeLater(() -> txtStudentId.requestFocusInWindow());
    }

    /**
     * Kiểm tra điều kiện mượn sách khi quét mã thẻ sinh viên.
     */
    private void onScanCardEnter() {
        String studentId = txtStudentId.getText().trim();
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng quét hoặc nhập mã thẻ sinh viên!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtStudentId.requestFocusInWindow();
            return;
        }

        // Đổi sang sinh viên khác khi danh sách chờ đang có sách -> hỏi lại trước khi xóa
        boolean isDifferentStudent = currentStudent != null
                && !currentStudent.getStudentId().equalsIgnoreCase(studentId);
        if (isDifferentStudent && !pendingBooks.isEmpty()) {
            int option = JOptionPane.showConfirmDialog(this,
                    "Danh sách chờ mượn của sinh viên " + currentStudent.getFullName()
                            + " sẽ bị xóa. Bạn có muốn tiếp tục?",
                    "Xác nhận đổi sinh viên", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (option != JOptionPane.YES_OPTION) {
                txtStudentId.setText(currentStudent.getStudentId());
                return;
            }
        }

        clearPendingBooks();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            BorrowResult result = borrowController.handleCheckEligibility(studentId);

            if (result.isSuccess()) {
                currentStudent = result.getStudent();
                eligibilityWarning = result.getWarning();
                showStudentInfo(currentStudent);
                restoreEligibilityBanner();
                txtBookId.setEnabled(true);
                btnAddBook.setEnabled(true);
                txtBookId.requestFocusInWindow();
            } else {
                currentStudent = null;
                eligibilityWarning = null;
                clearStudentInfo();
                showErrorBanner(result.getMessage());
                txtStudentId.selectAll();
                txtStudentId.requestFocusInWindow();
            }
        } finally {
            setCursor(Cursor.getDefaultCursor());
            updateActionStates();
        }
    }

    /**
     * Thêm đầu sách vào danh sách chờ mượn khi quét mã sách.
     */
    private void onScanBookEnter() {
        if (currentStudent == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng quét thẻ sinh viên hợp lệ trước khi quét sách!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtStudentId.requestFocusInWindow();
            return;
        }

        String bookId = txtBookId.getText().trim();
        if (bookId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng quét hoặc nhập mã sách!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtBookId.requestFocusInWindow();
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            BorrowResult result = borrowController.handleAddBook(
                    currentStudent.getStudentId(), bookId, getPendingBookIds());

            if (result.isSuccess()) {
                Book book = result.getBook();
                pendingBooks.add(book);
                tableModel.addRow(new Object[]{
                        book.getBookId(),
                        book.getTitle(),
                        book.getAuthor(),
                        DELETE_BUTTON_TEXT
                });
                restoreEligibilityBanner();
                txtBookId.setText("");
            } else {
                showErrorBanner(result.getMessage());
                txtBookId.selectAll();
            }
        } finally {
            setCursor(Cursor.getDefaultCursor());
            txtBookId.requestFocusInWindow();
            updateActionStates();
        }
    }

    /**
     * Gỡ một cuốn khỏi danh sách chờ mượn.
     */
    private void onRemoveBook(int row) {
        if (row < 0 || row >= pendingBooks.size()) {
            return;
        }
        pendingBooks.remove(row);
        tableModel.removeRow(row);
        restoreEligibilityBanner();
        updateActionStates();
        txtBookId.requestFocusInWindow();
    }

    /**
     * Xử lý xác nhận tạo phiếu mượn sách.
     */
    private void btnConfirmActionPerformed(ActionEvent event) {
        if (currentStudent == null || pendingBooks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng quét thẻ sinh viên và ít nhất một cuốn sách!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Xác nhận trước khi ghi vào CSDL
        StringBuilder confirmMessage = new StringBuilder();
        confirmMessage.append("Tạo phiếu mượn cho sinh viên: ")
                .append(currentStudent.getFullName())
                .append(" (").append(currentStudent.getStudentId()).append(")\n\n")
                .append("Danh sách ").append(pendingBooks.size()).append(" cuốn:\n");
        for (Book book : pendingBooks) {
            confirmMessage.append("  • ").append(book.getTitle()).append("\n");
        }
        confirmMessage.append("\nBạn có chắc chắn muốn hoàn tất mượn?");

        int option = JOptionPane.showConfirmDialog(this, confirmMessage.toString(),
                "Xác nhận mượn sách", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        btnConfirmBorrow.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            BorrowResult result = borrowController.handleBorrow(
                    currentStudent.getStudentId(), getPendingBookIds());

            if (result.isSuccess()) {
                showSuccessMessage(result);
                resetForm();
            } else {
                showErrorBanner(result.getMessage());
                showErrorMessage(result.getMessage());
            }
        } finally {
            setCursor(Cursor.getDefaultCursor());
            updateActionStates();
        }
    }

    /**
     * Sự kiện nhấn "Hủy": hỏi lại nếu danh sách chờ đang có sách, sau đó xóa trắng form.
     */
    private void btnCancelActionPerformed(ActionEvent event) {
        if (!pendingBooks.isEmpty()) {
            int option = JOptionPane.showConfirmDialog(this,
                    "Danh sách chờ mượn chưa được lưu. Bạn có chắc muốn hủy phiên này?",
                    "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }
        resetForm();
    }

    // Hiển thị kết quả

    /**
     * Hiển thị thông báo thành công.
     */
    private void showSuccessMessage(BorrowResult result) {
        String message = "TẠO PHIẾU MƯỢN THÀNH CÔNG!\n\n"
                + "Mã phiếu mượn: " + result.getSlipId() + "\n"
                + "Sinh viên: " + currentStudent.getFullName() + " (" + currentStudent.getStudentId() + ")\n"
                + "Số sách mượn: " + pendingBooks.size() + " cuốn\n\n"
                + "Kho sách đã được cập nhật tự động.";
        JOptionPane.showMessageDialog(this, message, "Kết quả mượn sách", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Hiển thị thông báo lỗi.
     */
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi mượn sách", JOptionPane.ERROR_MESSAGE);
    }

    // Hàm hỗ trợ giao diện

    private void showStudentInfo(Student student) {
        lblStudentNameValue.setText(student.getFullName() + " (" + student.getStudentId() + ")");
        lblBorrowedCountValue.setText(student.getBorrowedCount() + " cuốn");
        lblDebtAmountValue.setText(formatMoney(student.getDebtAmount()));
    }

    private void clearStudentInfo() {
        lblStudentNameValue.setText("—");
        lblBorrowedCountValue.setText("—");
        lblDebtAmountValue.setText("—");
    }

    /**
     * Banner đỏ: không đủ điều kiện / sách không hợp lệ.
     */
    private void showErrorBanner(String message) {
        panelWarning.setBackground(COLOR_ERROR_BG);
        panelWarning.setBorder(new CompoundBorder(
                new LineBorder(COLOR_ERROR_BORDER, 1, true), new EmptyBorder(4, 10, 4, 10)));
        lblWarningMessage.setForeground(COLOR_ERROR_TEXT);
        lblWarningMessage.setText("⚠ " + message);
        panelWarning.setVisible(true);
    }

    /**
     * Banner vàng: đủ điều kiện nhưng sắp đạt giới hạn mượn.
     */
    private void showWarningBanner(String message) {
        panelWarning.setBackground(COLOR_WARNING_BG);
        panelWarning.setBorder(new CompoundBorder(
                new LineBorder(COLOR_WARNING_BORDER, 1, true), new EmptyBorder(4, 10, 4, 10)));
        lblWarningMessage.setForeground(COLOR_WARNING_TEXT);
        lblWarningMessage.setText("⚠ " + message);
        panelWarning.setVisible(true);
    }

    /**
     * Trả banner về trạng thái theo kết quả kiểm tra điều kiện (vàng nếu có cảnh báo, ẩn nếu không).
     */
    private void restoreEligibilityBanner() {
        if (eligibilityWarning != null && !eligibilityWarning.isEmpty()) {
            showWarningBanner(eligibilityWarning);
        } else {
            panelWarning.setVisible(false);
        }
    }

    /**
     * Bật / tắt các ô nhập và nút theo trạng thái hiện tại.
     */
    private void updateActionStates() {
        boolean hasStudent = currentStudent != null;
        boolean hasBooks = !pendingBooks.isEmpty();

        txtBookId.setEnabled(hasStudent);
        btnAddBook.setEnabled(hasStudent);

        btnConfirmBorrow.setEnabled(hasStudent && hasBooks);
        if (hasStudent && hasBooks) {
            btnConfirmBorrow.setBackground(COLOR_CONFIRM_ENABLED);
            btnConfirmBorrow.setForeground(Color.WHITE);
        } else {
            btnConfirmBorrow.setBackground(COLOR_CONFIRM_DISABLED);
            btnConfirmBorrow.setForeground(new Color(20, 60, 20));
        }

        lblPendingCount.setText("Số sách chờ mượn: " + pendingBooks.size());
    }

    private List<String> getPendingBookIds() {
        List<String> bookIds = new ArrayList<>();
        for (Book book : pendingBooks) {
            bookIds.add(book.getBookId());
        }
        return bookIds;
    }

    private void clearPendingBooks() {
        if (tblPendingBooks.isEditing()) {
            tblPendingBooks.getCellEditor().stopCellEditing();
        }
        pendingBooks.clear();
        tableModel.setRowCount(0);
    }

    /**
     * Xóa trắng form để bắt đầu lượt mượn mới.
     */
    private void resetForm() {
        clearPendingBooks();
        currentStudent = null;
        eligibilityWarning = null;
        txtStudentId.setText("");
        txtBookId.setText("");
        clearStudentInfo();
        panelWarning.setVisible(false);
        updateActionStates();
        txtStudentId.requestFocusInWindow();
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(90, 95, 100));
        return label;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    private TitledBorder createTitledSectionBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                new LineBorder(new Color(210, 213, 218), 1, true), title);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 11));
        border.setTitleColor(new Color(110, 115, 122));
        return border;
    }

    private String formatMoney(double amount) {
        return String.format(Locale.forLanguageTag("vi-VN"), "%,.0f đ", amount);
    }

    // Nút "Xóa" trong bảng danh sách chờ mượn

    private class DeleteButtonRenderer extends JButton implements TableCellRenderer {
        DeleteButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setBackground(new Color(250, 235, 235));
            setForeground(COLOR_ERROR_TEXT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setText(DELETE_BUTTON_TEXT);
            return this;
        }
    }

    private class DeleteButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int editingRow;

        DeleteButtonEditor() {
            super(new JCheckBox());
            button = new JButton(DELETE_BUTTON_TEXT);
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            button.setBackground(new Color(250, 235, 235));
            button.setForeground(COLOR_ERROR_TEXT);
            button.addActionListener(e -> {
                int rowToRemove = editingRow;
                fireEditingStopped();
                // Xóa dòng sau khi JTable kết thúc chỉnh sửa để tránh lỗi chỉ số dòng
                SwingUtilities.invokeLater(() -> onRemoveBook(rowToRemove));
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            editingRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return DELETE_BUTTON_TEXT;
        }
    }

    /**
     * Chạy thử giao diện độc lập với tài khoản mẫu Thủ thư.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) {
            session.login("thuthu01", SessionManager.ROLE_LIBRARIAN, "TT0001",
                    "Trần Thị Mai", "thuthu01@thuvien.edu.vn");
        }

        SwingUtilities.invokeLater(() -> new BorrowForm().setVisible(true));
    }
}
