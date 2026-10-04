package ui;

import controller.SearchController;
import model.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class SearchBookForm extends JFrame {

    private JTextField txtTuKhoa;
    private JButton btnTimKiem;
    private JButton btnXoa;
    private JTable tblSach;
    private DefaultTableModel tableModel;

    private final SearchController searchController;

    public SearchBookForm() {

        searchController = new SearchController();

        khoiTaoGiaoDien();
        ganSuKien();
    }

    /**
     * =========================================================================
     * Khởi tạo giao diện
     * =========================================================================
     */
    private void khoiTaoGiaoDien() {

        setTitle("Tra cứu sách - Library Management System");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // =========================================================
        // PANEL CHÍNH
        // =========================================================

        JPanel panelChinh = new JPanel(new BorderLayout(10, 10));

        panelChinh.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        // =========================================================
        // TIÊU ĐỀ
        // =========================================================

        JLabel lblTieuDe = new JLabel("TRA CỨU SÁCH");

        lblTieuDe.setFont(
                new Font("Arial", Font.BOLD, 26)
        );

        lblTieuDe.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        panelChinh.add(
                lblTieuDe,
                BorderLayout.NORTH
        );

        // =========================================================
        // PANEL NỘI DUNG
        // =========================================================

        JPanel panelNoiDung = new JPanel(
                new BorderLayout(10, 10)
        );

        // =========================================================
        // PANEL TÌM KIẾM
        // =========================================================

        JPanel panelTimKiem = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        10
                )
        );

        JLabel lblTuKhoa = new JLabel("Từ khóa:");

        lblTuKhoa.setFont(
                new Font("Arial", Font.PLAIN, 16)
        );

        txtTuKhoa = new JTextField(30);

        txtTuKhoa.setFont(
                new Font("Arial", Font.PLAIN, 16)
        );

        btnTimKiem = new JButton("Tìm kiếm");

        btnTimKiem.setFont(
                new Font("Arial", Font.BOLD, 14)
        );

        btnXoa = new JButton("Xóa");

        btnXoa.setFont(
                new Font("Arial", Font.BOLD, 14)
        );

        panelTimKiem.add(lblTuKhoa);
        panelTimKiem.add(txtTuKhoa);
        panelTimKiem.add(btnTimKiem);
        panelTimKiem.add(btnXoa);

        panelNoiDung.add(
                panelTimKiem,
                BorderLayout.NORTH
        );

        // =========================================================
        // BẢNG DANH SÁCH SÁCH
        // =========================================================

        String[] tenCot = {
                "Mã sách",
                "Tên sách",
                "Tác giả",
                "Số lượng còn"
        };

        tableModel = new DefaultTableModel(
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

        tblSach = new JTable(tableModel);

        tblSach.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );

        tblSach.setRowHeight(30);

        tblSach.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 14)
        );

        tblSach.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane = new JScrollPane(
                tblSach
        );

        JPanel panelBang = new JPanel(
                new BorderLayout()
        );

        panelBang.setBorder(
                BorderFactory.createTitledBorder(
                        "Danh sách sách"
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

        // =========================================================
        // THÊM PANEL NỘI DUNG VÀO PANEL CHÍNH
        // =========================================================

        panelChinh.add(
                panelNoiDung,
                BorderLayout.CENTER
        );

        add(panelChinh);
    }

    /**
     * =========================================================================
     * Gán sự kiện cho các nút
     * =========================================================================
     */
    private void ganSuKien() {

        // Nút Tìm kiếm
        btnTimKiem.addActionListener(e -> xuLyTimKiem());

        // Nhấn Enter trong ô tìm kiếm
        txtTuKhoa.addActionListener(e -> xuLyTimKiem());

        // Nút Xóa
        btnXoa.addActionListener(e -> xuLyXoa());
    }

    /**
     * =========================================================================
     * Xử lý chức năng tìm kiếm
     * =========================================================================
     */
    private void xuLyTimKiem() {

        String tuKhoa = txtTuKhoa.getText().trim();

        // Kiểm tra từ khóa
        if (tuKhoa.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập từ khóa cần tìm.",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );

            txtTuKhoa.requestFocus();

            return;
        }

        try {

            // =====================================================
            // UI → CONTROLLER
            // =====================================================

            List<Book> danhSachSach =
                    searchController.searchBooks(tuKhoa);

            // Xóa kết quả cũ
            xoaDuLieuBang();

            // =====================================================
            // Hiển thị kết quả
            // =====================================================

            for (Book book : danhSachSach) {

                tableModel.addRow(
                        new Object[]{
                                book.getBookId(),
                                book.getTitle(),
                                book.getAuthor(),
                                book.getStockQuantity()
                        }
                );
            }

            // =====================================================
            // Không tìm thấy
            // =====================================================

            if (danhSachSach.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Không tìm thấy sách phù hợp với từ khóa: "
                                + tuKhoa,
                        "Kết quả tìm kiếm",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Lỗi dữ liệu",
                    JOptionPane.WARNING_MESSAGE
            );

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
     * =========================================================================
     * Xử lý nút Xóa
     * =========================================================================
     */
    private void xuLyXoa() {

        txtTuKhoa.setText("");

        xoaDuLieuBang();

        txtTuKhoa.requestFocus();
    }

    /**
     * =========================================================================
     * Xóa toàn bộ dữ liệu trong bảng
     * =========================================================================
     */
    private void xoaDuLieuBang() {

        tableModel.setRowCount(0);
    }

    /**
     * =========================================================================
     * Hiển thị một sách vào bảng
     * =========================================================================
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
     * =========================================================================
     * Xóa kết quả tìm kiếm
     * =========================================================================
     */
    public void xoaKetQuaCu() {

        tableModel.setRowCount(0);
    }

    /**
     * =========================================================================
     * Chạy thử giao diện
     * =========================================================================
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            SearchBookForm form =
                    new SearchBookForm();

            form.setVisible(true);
        });
    }
}