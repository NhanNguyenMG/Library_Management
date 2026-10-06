package ui;

import controller.ReturnController;
import model.BorrowingItemDTO;
import model.PriorityStudent;
import model.ReturnResult;
import model.Student;
import service.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Tầng: Presentation Layer (ui)
 * Use Case: UC-05 Xử lý trả sách, UC-06 Cập nhật tồn kho, UC-07 Tính tiền phạt
 * Thiết kế giao diện: Bám sát 100% Wireframe Hình 4 trong SRS
 * Kiến trúc: Closed 4-Layer Architecture + MVC (chỉ giao tiếp với ReturnController)
 */
public class ReturnBookForm extends JFrame {

    private final ReturnController returnController;

    // Quản lý trạng thái dữ liệu trên giao diện
    private Student currentStudent = null;
    private List<BorrowingItemDTO> currentItems = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    // Thành phần Header
    private JLabel lblHeaderTitle;
    private JLabel lblLibrarianName;

    // Khu vực 1: Nhận diện
    private JTextField txtStudentId;
    private JButton btnManualInput;

    // Khu vực 2: Danh sách sách đang mượn
    private JTable tblBooks;
    private DefaultTableModel tableModel;

    // Khu vực 3: Cảnh báo ẩn/hiện
    private JPanel panelWarning;
    private JLabel lblWarningMessage;

    // Khu vực 4: Thông tin phạt tự động tính
    private JLabel lblLateDaysValue;
    private JLabel lblTotalFineValue;

    // Nút hành động phía dưới
    private JButton btnConfirmReturn;
    private JButton btnCancel;

    public ReturnBookForm() {
        this.returnController = new ReturnController();
        initComponents();
        onLoad();
    }

    public ReturnBookForm(ReturnController controller) {
        this.returnController = controller;
        initComponents();
        onLoad();
    }

    /**
     * Khởi tạo giao diện đồ họa chuẩn Wireframe Hình 4
     */
    private void initComponents() {
        setTitle("Hệ thống Quản lý Thư viện - Xử lý trả sách (UC-05)");
        setSize(960, 680);
        setMinimumSize(new Dimension(860, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainContainer = new JPanel(new BorderLayout(0, 10));
        mainContainer.setBorder(new EmptyBorder(12, 16, 12, 16));
        mainContainer.setBackground(new Color(245, 246, 248));

        // 1. THANH HEADER
        mainContainer.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. THÂN CHÍNH (Chứa 4 khu vực theo Wireframe)
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setOpaque(false);

        bodyPanel.add(createArea1Identification());
        bodyPanel.add(Box.createVerticalStrut(10));
        bodyPanel.add(createArea2BorrowingList());
        bodyPanel.add(Box.createVerticalStrut(8));
        bodyPanel.add(createArea3WarningBanner());
        bodyPanel.add(Box.createVerticalStrut(8));
        bodyPanel.add(createArea4FineInformation());

        mainContainer.add(bodyPanel, BorderLayout.CENTER);

        // 3. THANH NÚT BẤM DƯỚI CÙNG
        mainContainer.add(createBottomActionPanel(), BorderLayout.SOUTH);

        setContentPane(mainContainer);
    }

    /**
     * 1. Thanh tiêu đề Header: Tiêu đề bên trái, tên Thủ thư bên phải
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(230, 232, 235));
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 203, 208), 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));

        lblHeaderTitle = new JLabel("Xử lý trả sách");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeaderTitle.setForeground(new Color(30, 30, 30));

        String librarianName = SessionManager.getInstance().isLoggedIn()
                ? SessionManager.getInstance().getFullName()
                : "Thủ thư";
        lblLibrarianName = new JLabel(librarianName + " ▾");
        lblLibrarianName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLibrarianName.setForeground(new Color(60, 60, 60));

        panel.add(lblHeaderTitle, BorderLayout.WEST);
        panel.add(lblLibrarianName, BorderLayout.EAST);
        return panel;
    }

    /**
     * 2. Khu vực 1: Nhận diện thẻ sinh viên
     */
    private JPanel createArea1Identification() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(createTitledSectionBorder("KHU VỰC 1 - NHẬN DIỆN"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label: Mã thẻ Sinh viên (*)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel lblId = new JLabel("Mã thẻ Sinh viên (*)");
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lblId, gbc);

        // Ô quét / nhập mã thẻ
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        txtStudentId = new JTextField();
        txtStudentId.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtStudentId.setPreferredSize(new Dimension(300, 34));
        txtStudentId.setToolTipText("Quét / nhập mã thẻ sinh viên rồi nhấn Enter");
        txtStudentId.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    xuLyTraCuuSinhVien();
                }
            }
        });
        panel.add(txtStudentId, gbc);

        // Nút: Nhập mã thủ công
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        btnManualInput = new JButton("Nhập mã thủ công");
        btnManualInput.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnManualInput.setPreferredSize(new Dimension(160, 34));
        btnManualInput.setBackground(new Color(240, 242, 245));
        btnManualInput.setFocusPainted(false);
        btnManualInput.addActionListener(e -> xuLyTraCuuSinhVien());
        panel.add(btnManualInput, gbc);

        return panel;
    }

    /**
     * 3. Khu vực 2: Danh sách sách đang mượn
     */
    private JPanel createArea2BorrowingList() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(createTitledSectionBorder("KHU VỰC 2 - DANH SÁCH SÁCH ĐANG MƯỢN"));

        String[] columnNames = {"Tên sách", "Hạn trả", "Trạng thái", "Tình trạng vật lý", "Thao tác"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4; // Chỉ cho phép sửa tình trạng vật lý và bấm nút thao tác
            }
        };

        tblBooks = new JTable(tableModel);
        tblBooks.setRowHeight(36);
        tblBooks.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblBooks.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblBooks.getTableHeader().setBackground(new Color(240, 242, 245));
        tblBooks.getTableHeader().setReorderingAllowed(false);

        // Canh chỉnh cột
        tblBooks.getColumnModel().getColumn(0).setPreferredWidth(280); // Tên sách
        tblBooks.getColumnModel().getColumn(1).setPreferredWidth(100); // Hạn trả
        tblBooks.getColumnModel().getColumn(2).setPreferredWidth(100); // Trạng thái
        tblBooks.getColumnModel().getColumn(3).setPreferredWidth(130); // Tình trạng vật lý
        tblBooks.getColumnModel().getColumn(4).setPreferredWidth(140); // Thao tác

        // Renderer căn giữa hạn trả và trạng thái
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblBooks.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        // Renderer tô màu cho trạng thái
        tblBooks.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                String val = (value != null) ? value.toString() : "";
                if ("Đã quét".equalsIgnoreCase(val)) {
                    c.setForeground(new Color(39, 174, 96));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(new Color(120, 120, 120));
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
                return c;
            }
        });

        // Editor & Renderer cho Dropdown "Tình trạng vật lý"
        JComboBox<String> comboCondition = new JComboBox<>(new String[]{"Tốt", "Hư hỏng", "Mất sách"});
        comboCondition.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboCondition.addActionListener(e -> capNhatPhiBoiThuong());
        tblBooks.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(comboCondition));

        // Editor & Renderer cho nút "Quét/Xác nhận trả"
        tblBooks.getColumnModel().getColumn(4).setCellRenderer(new TableButtonRenderer());
        tblBooks.getColumnModel().getColumn(4).setCellEditor(new TableButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(tblBooks);
        scrollPane.setPreferredSize(new Dimension(880, 160));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 4. Khu vực 3: Banner cảnh báo ẩn/hiện
     */
    private JPanel createArea3WarningBanner() {
        panelWarning = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        panelWarning.setBackground(new Color(253, 237, 236)); // Đỏ nhạt
        panelWarning.setBorder(new CompoundBorder(
                new LineBorder(new Color(231, 76, 60), 1, true), // Viền đỏ
                new EmptyBorder(4, 10, 4, 10)
        ));

        lblWarningMessage = new JLabel("⚠ Sách không khớp với thông tin phiếu mượn");
        lblWarningMessage.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblWarningMessage.setForeground(new Color(192, 57, 43)); // Chữ đỏ đậm

        panelWarning.add(lblWarningMessage);
        panelWarning.setVisible(false); // Mặc định ẩn theo Wireframe

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(createTitledSectionBorder("KHU VỰC 3 - CẢNH BÁO (ẩn/hiện)"));
        container.add(panelWarning, BorderLayout.CENTER);
        return container;
    }

    /**
     * 5. Khu vực 4: Thông tin phạt tự động tính
     */
    private JPanel createArea4FineInformation() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(createTitledSectionBorder("KHU VỰC 4 - THÔNG TIN PHẠT (tự động tính)"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 14, 6, 14);
        gbc.anchor = GridBagConstraints.WEST;

        // Dòng 1: Số ngày trễ hạn
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblLateDays = new JLabel("Số ngày trễ hạn:");
        lblLateDays.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(lblLateDays, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        lblLateDaysValue = new JLabel("0 ngày");
        lblLateDaysValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lblLateDaysValue, gbc);

        // Dòng 2: Tổng tiền phạt dự kiến
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblTotalFine = new JLabel("Tổng tiền phạt dự kiến:");
        lblTotalFine.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(lblTotalFine, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        lblTotalFineValue = new JLabel("0 đ");
        lblTotalFineValue.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalFineValue.setForeground(new Color(192, 57, 43));
        panel.add(lblTotalFineValue, gbc);

        return panel;
    }

    /**
     * 6. Thanh nút bấm phía dưới (Căn phải): Xác nhận trả (xanh), Hủy (xám)
     */
    private JPanel createBottomActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        panel.setOpaque(false);

        // Nút: Xác nhận trả (Màu xanh, mặc định disabled)
        btnConfirmReturn = new JButton("Xác nhận trả");
        btnConfirmReturn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirmReturn.setPreferredSize(new Dimension(150, 40));
        btnConfirmReturn.setBackground(new Color(163, 217, 165)); // Xanh nhạt Wireframe
        btnConfirmReturn.setForeground(new Color(20, 60, 20));
        btnConfirmReturn.setFocusPainted(false);
        btnConfirmReturn.setEnabled(false); // Khóa nếu chưa quét cuốn nào
        btnConfirmReturn.addActionListener(this::xuLyXacNhanTra);

        // Nút: Hủy (Màu xám)
        btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.setBackground(new Color(220, 222, 225));
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(this::xuLyHuy);

        panel.add(btnConfirmReturn);
        panel.add(btnCancel);
        return panel;
    }

    /**
     * Tạo đường viền tiêu đề cho từng khu vực
     */
    private TitledBorder createTitledSectionBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                new LineBorder(new Color(210, 213, 218), 1, true),
                title
        );
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 11));
        border.setTitleColor(new Color(110, 115, 122));
        return border;
    }

    // =========================================================================
    // XỬ LÝ SỰ KIỆN GIAO DIỆN (THEO BẢNG 14 & SEQUENCE DIAGRAM SRS)
    // =========================================================================

    /**
     * Sự kiện onLoad: Tự động focus vào ô nhập mã thẻ sinh viên
     */
    private void onLoad() {
        SwingUtilities.invokeLater(() -> txtStudentId.requestFocusInWindow());
    }

    /**
     * Sự kiện onScan_MaThe: Tra cứu danh sách sách đang mượn theo mã thẻ SV
     */
    private void xuLyTraCuuSinhVien() {
        String studentId = txtStudentId.getText().trim();
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng quét hoặc nhập mã thẻ sinh viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 1. Lấy thông tin sinh viên
            currentStudent = returnController.layThongTinSinhVien(studentId);

            // 2. Lấy danh sách sách đang mượn
            currentItems = returnController.laySachDangMuon(studentId);

            // 3. Hiển thị lên bảng
            tableModel.setRowCount(0);
            panelWarning.setVisible(false);

            if (currentItems.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Sinh viên " + currentStudent.getFullName() + " hiện không có sách nào đang mượn.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                resetThongTinPhat();
                return;
            }

            for (BorrowingItemDTO item : currentItems) {
                tableModel.addRow(new Object[]{
                        item.getBookTitle(),
                        dateFormat.format(item.getDueDate()),
                        item.getStatus(),
                        item.getPhysicalCondition(),
                        "Quét/Xác nhận trả"
                });
            }

            tinhToanLaiPhat();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi tra cứu", JOptionPane.ERROR_MESSAGE);
            tableModel.setRowCount(0);
            currentStudent = null;
            currentItems.clear();
            resetThongTinPhat();
        }
    }

    /**
     * Xử lý khi quét / xác nhận trả từng dòng sách
     */
    private void xuLyQuetDongSach(int row) {
        if (row < 0 || row >= currentItems.size()) {
            return;
        }

        BorrowingItemDTO item = currentItems.get(row);
        item.setStatus("Đã quét");
        tableModel.setValueAt("Đã quét", row, 2);

        // Ẩn cảnh báo vì sách khớp dòng
        panelWarning.setVisible(false);

        // Tính toán lại phạt và cập nhật trạng thái nút Xác nhận
        tinhToanLaiPhat();
    }

    /**
     * Cập nhật phí bồi thường khi thay đổi tình trạng vật lý (Dropdown)
     */
    private void capNhatPhiBoiThuong() {
        int selectedRow = tblBooks.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentItems.size()) {
            String condition = (String) tblBooks.getValueAt(selectedRow, 3);
            BorrowingItemDTO item = currentItems.get(selectedRow);
            item.setPhysicalCondition(condition);

            if ("Hư hỏng".equalsIgnoreCase(condition)) {
                item.setCompensationFee(50000.0); // Phí bồi thường hư hỏng mặc định 50.000 đ
            } else if ("Mất sách".equalsIgnoreCase(condition)) {
                item.setCompensationFee(100000.0); // Đền bù mất sách 100.000 đ
            } else {
                item.setCompensationFee(0.0);
            }
            tinhToanLaiPhat();
        }
    }

    /**
     * Tính toán số ngày trễ và tổng tiền phạt dự kiến
     */
    private void tinhToanLaiPhat() {
        if (currentItems.isEmpty() || currentStudent == null) {
            resetThongTinPhat();
            return;
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        long maxLateDays = 0;
        double totalCompFee = 0.0;
        boolean hasScanned = false;

        for (BorrowingItemDTO item : currentItems) {
            if (item.isScanned()) {
                hasScanned = true;
                long days = returnController.tinhSoNgayTre(item.getDueDate(), now);
                if (days > maxLateDays) {
                    maxLateDays = days;
                }
                totalCompFee += item.getCompensationFee();
            }
        }

        double totalFine = hasScanned
                ? returnController.tinhTienPhat(maxLateDays, currentStudent, totalCompFee)
                : 0.0;

        lblLateDaysValue.setText(maxLateDays + " ngày");
        lblTotalFineValue.setText(String.format("%,.0f đ", totalFine));

        if (currentStudent instanceof PriorityStudent ps) {
            lblTotalFineValue.setText(String.format("%,.0f đ (Đã giảm %.0f%% ưu tiên)", totalFine, ps.getDiscountRate()));
        }

        // Kích hoạt nút xác nhận nếu có ít nhất 1 cuốn đã quét
        btnConfirmReturn.setEnabled(hasScanned);
        if (hasScanned) {
            btnConfirmReturn.setBackground(new Color(39, 174, 96));
            btnConfirmReturn.setForeground(Color.WHITE);
        } else {
            btnConfirmReturn.setBackground(new Color(163, 217, 165));
            btnConfirmReturn.setForeground(new Color(20, 60, 20));
        }
    }

    private void resetThongTinPhat() {
        lblLateDaysValue.setText("0 ngày");
        lblTotalFineValue.setText("0 đ");
        btnConfirmReturn.setEnabled(false);
        btnConfirmReturn.setBackground(new Color(163, 217, 165));
        btnConfirmReturn.setForeground(new Color(20, 60, 20));
    }

    /**
     * Sự kiện onClick_XacNhanTra: Thực hiện Transaction trả sách và hiển thị kết quả
     */
    private void xuLyXacNhanTra(ActionEvent event) {
        if (currentItems.isEmpty() || currentStudent == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng quét mã thẻ sinh viên trước!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<BorrowingItemDTO> scannedList = currentItems.stream()
                .filter(BorrowingItemDTO::isScanned)
                .toList();

        if (scannedList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng quét ít nhất một cuốn sách trước khi xác nhận!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String slipId = scannedList.get(0).getSlipId();
        String librarianId = SessionManager.getInstance().getUserId();
        if (librarianId == null || librarianId.isEmpty()) {
            librarianId = "NV0001"; // Mặc định nếu chạy test chưa login
        }

        // Vô hiệu hóa nút trong lúc xử lý
        btnConfirmReturn.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            ReturnResult result = returnController.xacNhanTraSach(scannedList, slipId, currentStudent, librarianId);

            if (result.isSuccess()) {
                String message = "XÁC NHẬN TRẢ SÁCH THÀNH CÔNG!\n\n"
                        + "Mã phiếu mượn: " + result.getSlipId() + "\n"
                        + "Số ngày trễ: " + result.getLateDays() + " ngày\n"
                        + "Tiền phạt: " + String.format("%,.0f VNĐ", result.getFineAmount()) + "\n\n"
                        + "Kho sách đã được cập nhật (+1) tự động.";

                JOptionPane.showMessageDialog(this, message, "Kết quả trả sách", JOptionPane.INFORMATION_MESSAGE);

                // Tải lại dữ liệu sinh viên sau khi trả
                xuLyTraCuuSinhVien();

            } else {
                panelWarning.setVisible(true);
                lblWarningMessage.setText("⚠ " + result.getMessage());
                JOptionPane.showMessageDialog(this, result.getMessage(), "Lỗi trả sách", JOptionPane.ERROR_MESSAGE);
            }

        } finally {
            setCursor(Cursor.getDefaultCursor());
            btnConfirmReturn.setEnabled(!currentItems.isEmpty());
        }
    }

    /**
     * Sự kiện onClick_Huy: Kiểm tra nếu đã quét thì hỏi xác nhận trước khi hủy
     */
    private void xuLyHuy(ActionEvent event) {
        boolean hasScanned = currentItems.stream().anyMatch(BorrowingItemDTO::isScanned);
        if (hasScanned) {
            int opt = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn đã quét sách nhưng chưa xác nhận trả. Bạn có chắc muốn hủy phiên này?",
                    "Xác nhận hủy",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (opt != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Xóa trắng form
        txtStudentId.setText("");
        tableModel.setRowCount(0);
        currentItems.clear();
        currentStudent = null;
        panelWarning.setVisible(false);
        resetThongTinPhat();
        txtStudentId.requestFocusInWindow();
    }

    /**
     * Lớp hiển thị nút bấm trong cell của JTable
     */
    private class TableButtonRenderer extends JButton implements TableCellRenderer {
        public TableButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setBackground(new Color(245, 247, 250));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Quét/Xác nhận" : value.toString());
            return this;
        }
    }

    /**
     * Lớp xử lý sự kiện khi bấm nút trong cell của JTable
     */
    private class TableButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int selectedRow;

        public TableButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Quét/Xác nhận trả");
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            button.setBackground(new Color(230, 245, 235));
            button.addActionListener(e -> {
                fireEditingStopped();
                xuLyQuetDongSach(selectedRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            selectedRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Quét/Xác nhận trả";
        }
    }

    /**
     * Điểm chạy thử độc lập giao diện
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            ReturnBookForm form = new ReturnBookForm();
            form.setVisible(true);
        });
    }
}
