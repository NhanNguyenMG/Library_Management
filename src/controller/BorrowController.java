package controller;

import model.BorrowDetail;
import model.BorrowResult;
import model.BorrowSlip;
import service.BorrowService;
import service.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * =========================================================================
 * Tầng: Presentation Layer - Controller (controller/)
 * Use Case: UC-03 Tạo phiếu mượn sách «include» UC-04 Kiểm tra điều kiện mượn sách
 * Ánh xạ SRS: PhieuMuonController (Bảng 13, Hình 8) -> BorrowController (theo CODING_STANDARDS 2.2)
 * Nhiệm vụ: nhận dữ liệu từ BorrowForm, gọi BorrowService, trả BorrowResult về Form.
 *           Controller chỉ gọi tầng Service (Closed 4-Layer Architecture).
 * =========================================================================
 *
 * @author Người số 4 (UC-03, UC-04 Mượn sách)
 */
public class BorrowController {

    private final BorrowService borrowService = new BorrowService();

    /**
     * =========================================================================
     * Use Case: UC-04 Kiểm tra điều kiện mượn sách (UC-03 bước 1-3: quét thẻ sinh viên)
     * Sequence Diagram: Chưa có (Bảng 17 SRS)
     * Traceability Message: borrowController.handleCheckEligibility() -> borrowService.checkBorrowEligibility()
     * Test Case tương ứng: TC-06, TC-07, TC-08
     * =========================================================================
     *
     * @param studentId mã số sinh viên quét từ thẻ (Form đã kiểm tra không rỗng)
     * @return BorrowResult kèm thông tin sinh viên và cảnh báo (nếu có) để Form hiển thị
     */
    public BorrowResult handleCheckEligibility(String studentId) {
        return borrowService.checkBorrowEligibility(studentId);
    }

    /**
     * =========================================================================
     * Use Case: UC-03 Tạo phiếu mượn sách - bước 4 (quét từng đầu sách)
     * Sequence Diagram: sd MuonSach (Hình 8 SRS, mục 5.5.2)
     * Traceability Message: timTheoMaDauSach(maDauSach) qua borrowService.checkBookAvailability()
     * Test Case tương ứng: TC-06, TC-07
     * =========================================================================
     *
     * @param studentId      mã số sinh viên đang lập phiếu
     * @param bookId         mã đầu sách vừa quét
     * @param pendingBookIds các mã đầu sách đã có trong danh sách chờ mượn trên Form
     * @return BorrowResult kèm thông tin đầu sách nếu hợp lệ
     */
    public BorrowResult handleAddBook(String studentId, String bookId, List<String> pendingBookIds) {
        return borrowService.checkBookAvailability(studentId, bookId, pendingBookIds);
    }

    /**
     * =========================================================================
     * Use Case: UC-03 Tạo phiếu mượn sách - bước 5 (Thủ thư nhấn "Hoàn tất mượn")
     * Sequence Diagram: sd MuonSach (Hình 8 SRS, mục 5.5.2)
     * Traceability Message: taoPhieuMuon(mssv, maDauSach) -> borrowController.handleBorrow()
     *                       xuLyMuonSach(mssv, maDauSach) -> borrowService.createBorrowSlip()
     * Requirement: REQ-003, REQ-020
     * Test Case tương ứng: TC-06, TC-07, TC-08
     * =========================================================================
     *
     * @param studentId mã số sinh viên
     * @param bookIds   danh sách mã đầu sách trong danh sách chờ mượn
     * @return BorrowResult kèm mã phiếu nếu thành công, hoặc lý do thất bại
     */
    public BorrowResult handleBorrow(String studentId, List<String> bookIds) {
        // Pre-condition UC-03: Thủ thư đã đăng nhập (UC-01); mã Thủ thư được ghi vào phiếu
        SessionManager session = SessionManager.getInstance();
        if (!session.isLibrarian()) {
            return new BorrowResult(false, "Vui lòng đăng nhập bằng tài khoản Thủ thư để tạo phiếu mượn");
        }

        BorrowSlip borrowSlip = new BorrowSlip();
        borrowSlip.setStudentId(studentId);
        borrowSlip.setLibrarianId(session.getUserId());

        List<BorrowDetail> borrowDetails = new ArrayList<>();
        if (bookIds != null) {
            for (String bookId : bookIds) {
                BorrowDetail borrowDetail = new BorrowDetail();
                borrowDetail.setBookId(bookId);
                borrowDetails.add(borrowDetail);
            }
        }

        return borrowService.createBorrowSlip(borrowSlip, borrowDetails);
    }
}