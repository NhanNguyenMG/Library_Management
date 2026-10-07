package service;

import db.DBConnection;
import model.Book;
import model.BorrowDetail;
import model.BorrowResult;
import model.BorrowSlip;
import model.Student;
import repository.BookRepository;
import repository.BorrowDetailRepository;
import repository.BorrowSlipRepository;
import repository.StudentRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BorrowService {

    public static final int MAX_BOOKS_ALLOWED = 5;          // Tối đa 5 cuốn / sinh viên
    public static final int MAX_BORROW_DAYS = 14;           // 14 ngày mượn tối đa
    public static final int QUANTITY_PER_BOOK = 1;          // Mỗi đầu sách 1 cuốn / phiếu

    // Thông báo hiển thị
    private static final String MSG_INVALID_CARD = "Thẻ không hợp lệ";
    private static final String MSG_OVER_LIMIT = "Vượt quá giới hạn mượn";
    private static final String MSG_NEAR_LIMIT = "Sắp đạt giới hạn mượn";
    private static final String MSG_OUT_OF_STOCK = "Sách đã hết";
    private static final String MSG_DUPLICATE_BOOK = "Đầu sách đã có trong danh sách chờ mượn";
    private static final String MSG_EMPTY_LIST = "Danh sách sách chờ mượn đang trống";
    private static final String MSG_SYSTEM_ERROR = "Không xác định được, lỗi hệ thống";

    private final StudentRepository studentRepository = new StudentRepository();
    private final BookRepository bookRepository = new BookRepository();
    private final BorrowSlipRepository borrowSlipRepository = new BorrowSlipRepository();
    private final BorrowDetailRepository borrowDetailRepository = new BorrowDetailRepository();

    /**
     * Kiểm tra điều kiện mượn sách của sinh viên (hạn mức, nợ phạt, trạng thái thẻ)
     *
     * @param studentId mã số sinh viên quét từ thẻ
     * @return BorrowResult kết quả kiểm tra
     */
    public BorrowResult checkBorrowEligibility(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return new BorrowResult(false, MSG_INVALID_CARD);
        }

        try {
            // Kiểm tra thẻ tồn tại và không bị khóa
            Student student = studentRepository.findByStudentId(studentId);
            if (student == null || studentRepository.isAccountLocked(studentId)) {
                return new BorrowResult(false, MSG_INVALID_CARD);
            }

            // Kiểm tra hạn mức mượn
            if (student.getBorrowedCount() >= MAX_BOOKS_ALLOWED) {
                return new BorrowResult(false, MSG_OVER_LIMIT);
            }

            // Kiểm tra nợ phạt chưa thanh toán
            if (student.getDebtAmount() > 0) {
                return new BorrowResult(false, "Sinh viên còn nợ phạt " + formatMoney(student.getDebtAmount())
                        + ". Vui lòng thanh toán nợ trước khi mượn sách.");
            }

            // Đủ điều kiện, cảnh báo nếu chỉ còn 1 suất mượn
            BorrowResult result = new BorrowResult(true, "Đủ điều kiện mượn sách");
            result.setStudent(student);
            if (student.getBorrowedCount() == MAX_BOOKS_ALLOWED - 1) {
                result.setWarning(MSG_NEAR_LIMIT);
            }
            return result;

        } catch (SQLException e) {
            return new BorrowResult(false, MSG_SYSTEM_ERROR);
        }
    }

    /**
     * Kiểm tra tính khả dụng của đầu sách khi thêm vào danh sách chờ mượn
     *
     * @param studentId      mã số sinh viên đã qua kiểm tra điều kiện
     * @param bookId         mã đầu sách vừa quét
     * @param pendingBookIds các mã đầu sách đã có trong danh sách chờ mượn
     * @return BorrowResult kết quả kiểm tra
     */
    public BorrowResult checkBookAvailability(String studentId, String bookId, List<String> pendingBookIds) {
        if (bookId == null || bookId.trim().isEmpty()) {
            return new BorrowResult(false, "Mã sách không được để trống");
        }

        String scannedBookId = bookId.trim();
        if (pendingBookIds != null && pendingBookIds.contains(scannedBookId)) {
            return new BorrowResult(false, MSG_DUPLICATE_BOOK);
        }

        try {
            Book book = bookRepository.findById(scannedBookId);
            if (book == null) {
                return new BorrowResult(false, "Không tìm thấy đầu sách " + scannedBookId);
            }

            // Kiểm tra tồn kho đầu sách
            if (book.getStockQuantity() <= 0) {
                return new BorrowResult(false, MSG_OUT_OF_STOCK);
            }

            // Kiểm tra tổng số sách vượt hạn mức
            Student student = studentRepository.findByStudentId(studentId);
            if (student == null) {
                return new BorrowResult(false, MSG_INVALID_CARD);
            }
            int pendingCount = (pendingBookIds == null) ? 0 : pendingBookIds.size();
            if (student.getBorrowedCount() + pendingCount + QUANTITY_PER_BOOK > MAX_BOOKS_ALLOWED) {
                return new BorrowResult(false, MSG_OVER_LIMIT);
            }

            BorrowResult result = new BorrowResult(true, "Đã thêm sách vào danh sách chờ mượn");
            result.setBook(book);
            return result;

        } catch (SQLException e) {
            return new BorrowResult(false, MSG_SYSTEM_ERROR);
        }
    }

    /**
     * Tạo phiếu mượn sách và lưu vào cơ sở dữ liệu
     *
     * @param borrowSlip    thông tin phiếu mượn
     * @param borrowDetails danh sách chi tiết, mỗi dòng đã có bookId
     * @return BorrowResult kết quả tạo phiếu
     */
    public BorrowResult createBorrowSlip(BorrowSlip borrowSlip, List<BorrowDetail> borrowDetails) {
        if (borrowDetails == null || borrowDetails.isEmpty()) {
            return new BorrowResult(false, MSG_EMPTY_LIST);
        }

        // Kiểm tra lại điều kiện mượn ngay trước khi lưu
        BorrowResult eligibility = checkBorrowEligibility(borrowSlip.getStudentId());
        if (!eligibility.isSuccess()) {
            return eligibility;
        }
        Student student = eligibility.getStudent();

        // Không cho trùng đầu sách trong cùng một phiếu (ràng buộc UNIQUE ma_phieu, ma_dau_sach)
        Set<String> uniqueBookIds = new HashSet<>();
        for (BorrowDetail borrowDetail : borrowDetails) {
            if (!uniqueBookIds.add(borrowDetail.getBookId())) {
                return new BorrowResult(false, MSG_DUPLICATE_BOOK + ": " + borrowDetail.getBookId());
            }
        }

        // Kiểm tra tổng số sách sau khi mượn không vượt hạn mức
        int newBookCount = borrowDetails.size() * QUANTITY_PER_BOOK;
        if (student.getBorrowedCount() + newBookCount > MAX_BOOKS_ALLOWED) {
            return new BorrowResult(false, MSG_OVER_LIMIT);
        }

        // Kiểm tra lại tồn kho từng đầu sách
        try {
            for (BorrowDetail borrowDetail : borrowDetails) {
                Book book = bookRepository.findById(borrowDetail.getBookId());
                if (book == null || book.getStockQuantity() < QUANTITY_PER_BOOK) {
                    return new BorrowResult(false, MSG_OUT_OF_STOCK + ": " + borrowDetail.getBookId());
                }
            }
        } catch (SQLException e) {
            return new BorrowResult(false, MSG_SYSTEM_ERROR);
        }

        // Thực hiện lưu giao dịch trong Transaction
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Lập phiếu mượn
            Timestamp borrowDate = new Timestamp(System.currentTimeMillis());
            Timestamp dueDate = Timestamp.valueOf(borrowDate.toLocalDateTime().plusDays(MAX_BORROW_DAYS));
            String slipId = borrowSlipRepository.generateNextSlipId(conn);

            borrowSlip.setSlipId(slipId);
            borrowSlip.setBorrowDate(borrowDate);
            borrowSlip.setDueDate(dueDate);
            borrowSlip.setActualReturnDate(null);
            borrowSlipRepository.save(borrowSlip, conn);

            // 2. Lưu chi tiết phiếu mượn
            for (BorrowDetail borrowDetail : borrowDetails) {
                borrowDetail.setSlipId(slipId);
                borrowDetail.setQuantity(QUANTITY_PER_BOOK);
            }
            borrowDetailRepository.saveAll(borrowDetails, conn);

            // 3. Giảm số lượng tồn kho của từng đầu sách
            for (BorrowDetail borrowDetail : borrowDetails) {
                bookRepository.decreaseStock(borrowDetail.getBookId(), QUANTITY_PER_BOOK, conn);
            }

            // 4. Tăng số sách đang mượn của sinh viên
            studentRepository.increaseBorrowedCount(borrowSlip.getStudentId(), newBookCount, conn);

            conn.commit(); // Cam kết lưu vào DB

            // Thông báo kết quả tạo phiếu thành công
            BorrowResult result = new BorrowResult(true, "Tạo phiếu mượn " + slipId + " thành công!");
            result.setSlipId(slipId);
            result.setStudent(student);
            return result;

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } // Lỗi -> Hoàn tác toàn bộ
            }
            return new BorrowResult(false, "Lỗi hệ thống: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    /**
     * Định dạng số tiền theo kiểu Việt Nam, ví dụ 30000 -> "30.000 đ".
     */
    private String formatMoney(double amount) {
        return String.format(Locale.forLanguageTag("vi-VN"), "%,.0f đ", amount);
    }
}