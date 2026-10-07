package controller;

import model.BorrowDetail;
import model.BorrowResult;
import model.BorrowSlip;
import service.BorrowService;
import service.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class BorrowController {

    private final BorrowService borrowService = new BorrowService();

    /**
     * Kiểm tra điều kiện mượn sách của sinh viên
     *
     * @param studentId mã số sinh viên quét từ thẻ
     * @return BorrowResult kèm thông tin sinh viên và cảnh báo (nếu có)
     */
    public BorrowResult handleCheckEligibility(String studentId) {
        return borrowService.checkBorrowEligibility(studentId);
    }

    /**
     * Kiểm tra và thêm đầu sách vào danh sách chờ mượn
     *
     * @param studentId      mã số sinh viên
     * @param bookId         mã đầu sách
     * @param pendingBookIds các mã đầu sách đã có trong danh sách chờ mượn
     * @return BorrowResult kèm thông tin đầu sách nếu hợp lệ
     */
    public BorrowResult handleAddBook(String studentId, String bookId, List<String> pendingBookIds) {
        return borrowService.checkBookAvailability(studentId, bookId, pendingBookIds);
    }

    /**
     * Xử lý hoàn tất lập phiếu mượn sách
     *
     * @param studentId mã số sinh viên
     * @param bookIds   danh sách mã đầu sách trong danh sách chờ mượn
     * @return BorrowResult kèm mã phiếu nếu thành công
     */
    public BorrowResult handleBorrow(String studentId, List<String> bookIds) {
        // Kiểm tra quyền hạn trước khi lập phiếu
        SessionManager session = SessionManager.getInstance();
        if (!session.isLibrarian() && !session.isManager()) {
            return new BorrowResult(false, "Vui lòng đăng nhập bằng tài khoản Thủ thư hoặc Quản lý để tạo phiếu mượn");
        }

        BorrowSlip borrowSlip = new BorrowSlip();
        borrowSlip.setStudentId(studentId);

        // Bảng phieu_muon có khóa ngoại ma_nhan_vien REFERENCES thu_thu(ma_nhan_vien).
        // Nếu Quản lý lập phiếu thay, gán mã thủ thư mặc định 'TT0001' để đảm bảo toàn vẹn dữ liệu CSDL.
        String staffId = session.getUserId();
        if (session.isManager() || staffId == null || staffId.startsWith("QL")) {
            staffId = "TT0001";
        }
        borrowSlip.setLibrarianId(staffId);

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