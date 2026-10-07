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

/**
 * =========================================================================
 * Tầng: Business Logic Layer (service/)
 * Use Case: UC-03 Tạo phiếu mượn sách «include» UC-04 Kiểm tra điều kiện mượn sách
 * Ánh xạ SRS: PhieuMuonService (Bảng 13, Hình 8) -> BorrowService (theo CODING_STANDARDS 2.2)
 * Quy tắc nghiệp vụ: tối đa 5 quyển (REQ-003), khóa mượn khi còn nợ phạt (REQ-015),
 *                    một phiếu nhiều đầu sách (REQ-020), lưu phiếu + chi tiết + trừ kho
 *                    + tăng số sách đang mượn trong MỘT giao dịch (NFR-Reliability UC-03).
 * =========================================================================
 *
 * @author Người số 4 (UC-03, UC-04 Mượn sách)
 */
public class BorrowService {

    public static final int MAX_BOOKS_ALLOWED = 5;          // Tối đa 5 cuốn / sinh viên (REQ-003)
    public static final int MAX_BORROW_DAYS = 14;           // 14 ngày mượn tối đa (CODING_STANDARDS 2.5)
    public static final int QUANTITY_PER_BOOK = 1;          // Mỗi đầu sách 1 cuốn / phiếu (UNIQUE ma_phieu, ma_dau_sach)

    // Thông báo hiển thị (theo SRS: UC-03, UC-04, Bảng 18)
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
     * =========================================================================
     * Use Case: UC-04 Kiểm tra điều kiện mượn sách (được «include» bởi UC-03 bước 2)
     * Sequence Diagram: Chưa có (Bảng 17 SRS) - ánh xạ SinhVien.kiemTraDieuKienMuon()
     * Luồng: bước 3 thẻ hợp lệ -> bước 4 hạn mức 5 quyển -> bước 5 nợ phạt
     * Requirement: REQ-003, REQ-015
     * Test Case tương ứng: TC-06 (đang mượn 4 - cảnh báo), TC-07 (đang mượn 5 - từ chối),
     *                      TC-08 (còn nợ phạt - chặn)
     * =========================================================================
     *
     * @param studentId mã số sinh viên quét từ thẻ
     * @return BorrowResult: success=true kèm student (và warning nếu sắp đạt hạn mức),
     *         success=false kèm lý do không đủ điều kiện
     */
    public BorrowResult checkBorrowEligibility(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return new BorrowResult(false, MSG_INVALID_CARD);
        }

        try {
            // Bước 2-3: thẻ tồn tại và không bị khóa (EF-1)
            Student student = studentRepository.findByStudentId(studentId);
            if (student == null || studentRepository.isAccountLocked(studentId)) {
                return new BorrowResult(false, MSG_INVALID_CARD);
            }

            // Bước 4: hạn mức mượn (EF-2)
            if (student.getBorrowedCount() >= MAX_BOOKS_ALLOWED) {
                return new BorrowResult(false, MSG_OVER_LIMIT);
            }

            // Bước 5: còn nợ phạt chưa thanh toán (EF-3, REQ-015)
            if (student.getDebtAmount() > 0) {
                return new BorrowResult(false, "Sinh viên còn nợ phạt " + formatMoney(student.getDebtAmount())
                        + ". Vui lòng thanh toán nợ trước khi mượn sách.");
            }

            // Bước 6: đủ điều kiện, cảnh báo nếu chỉ còn 1 suất mượn (AF-2)
            BorrowResult result = new BorrowResult(true, "Đủ điều kiện mượn sách");
            result.setStudent(student);
            if (student.getBorrowedCount() == MAX_BOOKS_ALLOWED - 1) {
                result.setWarning(MSG_NEAR_LIMIT);
            }
            return result;

        } catch (SQLException e) {
            // EF-4: mất kết nối CSDL
            return new BorrowResult(false, MSG_SYSTEM_ERROR);
        }
    }

    /**
     * =========================================================================
     * Use Case: UC-03 Tạo phiếu mượn sách - bước 4 (quét từng đầu sách vào danh sách chờ)
     * Sequence Diagram: sd MuonSach (Hình 8 SRS, mục 5.5.2)
     * Traceability Message: timTheoMaDauSach(maDauSach) -> bookRepository.findById()
     *                       alt [thongTinDauSach != null AND soLuongCon > 0]
     * Requirement: REQ-003, REQ-020
     * Test Case tương ứng: TC-06, TC-07
     * =========================================================================
     *
     * @param studentId      mã số sinh viên đã qua kiểm tra điều kiện
     * @param bookId         mã đầu sách vừa quét
     * @param pendingBookIds các mã đầu sách đã có trong danh sách chờ mượn
     * @return BorrowResult: success=true kèm book, hoặc success=false kèm lý do (EF-2, EF-3)
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

            // EF-2: đầu sách đã hết
            if (book.getStockQuantity() <= 0) {
                return new BorrowResult(false, MSG_OUT_OF_STOCK);
            }

            // EF-3: số đang mượn + sách chờ mượn + cuốn này vượt hạn mức
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
     * =========================================================================
     * Use Case: UC-03 Tạo phiếu mượn sách - bước 5 đến 8 («include» UC-04)
     * Sequence Diagram: sd MuonSach (Hình 8 SRS, mục 5.5.2)
     * Traceability Message: xuLyMuonSach(mssv, maDauSach) -> borrowService.createBorrowSlip()
     *                       luuPhieuMuon(duLieuPhieuMuon) -> borrowSlipRepository.save()
     *                                                        + borrowDetailRepository.saveAll()
     *                       capNhatSoLuong(maDauSach)     -> bookRepository.decreaseStock()
     *                       ketQua(true/false, thongBao)  -> BorrowResult
     * Requirement: REQ-003, REQ-015, REQ-020
     * Test Case tương ứng: TC-06 (tạo phiếu thành công), TC-07, TC-08 (không tạo phiếu)
     * =========================================================================
     *
     * @param borrowSlip    phiếu mượn đã có studentId và librarianId (Controller lấy từ SessionManager)
     * @param borrowDetails danh sách chi tiết, mỗi dòng đã có bookId
     * @return BorrowResult: success=true kèm slipId, hoặc success=false kèm lý do
     */
    public BorrowResult createBorrowSlip(BorrowSlip borrowSlip, List<BorrowDetail> borrowDetails) {
        if (borrowDetails == null || borrowDetails.isEmpty()) {
            return new BorrowResult(false, MSG_EMPTY_LIST);
        }

        // Bước 2: kiểm tra lại điều kiện mượn (UC-04) ngay trước khi lưu
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

        // EF-3: tổng số sách sau khi mượn không vượt hạn mức
        int newBookCount = borrowDetails.size() * QUANTITY_PER_BOOK;
        if (student.getBorrowedCount() + newBookCount > MAX_BOOKS_ALLOWED) {
            return new BorrowResult(false, MSG_OVER_LIMIT);
        }

        // EF-2: kiểm tra lại tồn kho từng đầu sách (có thể đã thay đổi từ lúc quét)
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

        // Bước 6-7: một giao dịch duy nhất (CODING_STANDARDS 4.3, UC-03 EF-4)
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // Bước 6a: lập phiếu mượn (mã phiếu, ngày mượn, hạn trả)
            Timestamp borrowDate = new Timestamp(System.currentTimeMillis());
            Timestamp dueDate = Timestamp.valueOf(borrowDate.toLocalDateTime().plusDays(MAX_BORROW_DAYS));
            String slipId = borrowSlipRepository.generateNextSlipId(conn);

            borrowSlip.setSlipId(slipId);
            borrowSlip.setBorrowDate(borrowDate);
            borrowSlip.setDueDate(dueDate);
            borrowSlip.setActualReturnDate(null);
            borrowSlipRepository.save(borrowSlip, conn);

            // Bước 6b: lưu chi tiết phiếu
            for (BorrowDetail borrowDetail : borrowDetails) {
                borrowDetail.setSlipId(slipId);
                borrowDetail.setQuantity(QUANTITY_PER_BOOK);
            }
            borrowDetailRepository.saveAll(borrowDetails, conn);

            // Bước 7a: giảm số lượng còn của từng đầu sách
            for (BorrowDetail borrowDetail : borrowDetails) {
                bookRepository.decreaseStock(borrowDetail.getBookId(), QUANTITY_PER_BOOK, conn);
            }

            // Bước 7b: tăng số sách đang mượn của sinh viên
            studentRepository.increaseBorrowedCount(borrowSlip.getStudentId(), newBookCount, conn);

            conn.commit(); // Thành công 100% -> Cam kết lưu vào DB

            // Bước 8: thông báo tạo phiếu thành công
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