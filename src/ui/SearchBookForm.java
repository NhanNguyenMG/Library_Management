package ui;

import controller.SearchController;
import model.Book;

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
    private JTable tblSach;
    private DefaultTableModel tableModel;

    private final SearchController searchController;

    // Lưu danh sách Book tương ứng với các dòng đang hiển thị
    private List<Book> danhSachSachHienTai;

    public SearchBookForm() {

        searchController = new SearchController();

        khoiTaoGiaoDien();
        ganSuKien();
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

        // Panel chính
        JPanel panelChinh =
                new JPanel(new BorderLayout(10, 10));

        panelChinh.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        // Tiêu đề
        JLabel lblTieuDe =
                new JLabel("TRA CỨU SÁCH");

        lblTieuDe.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        26
                )
        );

        lblTieuDe.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        panelChinh.add(
                lblTieuDe,
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
                        "Arial",
                        Font.PLAIN,
                        16
                )
        );

        txtTuKhoa =
                new JTextField(30);

        txtTuKhoa.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        16
                )
        );

        btnTimKiem =
                new JButton("Tìm kiếm");

        btnTimKiem.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );

        btnXoa =
                new JButton("Xóa");

        btnXoa.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );

        panelTimKiem.add(lblTuKhoa);
        panelTimKiem.add(txtTuKhoa);
        panelTimKiem.add(btnTimKiem);
        panelTimKiem.add(btnXoa);

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
                        "Arial",
                        Font.PLAIN,
                        14
                )
        );

        tblSach.setRowHeight(30);

        tblSach.getTableHeader().setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );

        tblSach.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane =
                new JScrollPane(tblSach);

        JPanel panelBang =
                new JPanel(new BorderLayout());

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
     * Xử lý tìm kiếm sách
     */
    private void xuLyTimKiem() {

        String tuKhoa =
                txtTuKhoa.getText().trim();

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
            List<Book> danhSachSach =
                    searchController.searchBooks(
                            tuKhoa
                    );

            // Lưu danh sách sách hiện tại
            danhSachSachHienTai =
                    danhSachSach;

            // Xóa dữ liệu cũ
            xoaDuLieuBang();

            // Hiển thị kết quả
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

            // Không tìm thấy kết quả
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
     * Xử lý nút Xóa
     */
    private void xuLyXoa() {

        txtTuKhoa.setText("");

        xoaDuLieuBang();

        danhSachSachHienTai = null;

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
                        "Arial",
                        Font.PLAIN,
                        14
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

    /**
     * Chạy thử giao diện
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                () -> {

                    SearchBookForm form =
                            new SearchBookForm();

                    form.setVisible(true);
                }
        );
    }
}