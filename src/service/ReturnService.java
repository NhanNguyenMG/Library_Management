package service;

import db.DBConnection;
import model.Book;
import model.BorrowDetail;
import model.BorrowSlip;
import model.BorrowingItemDTO;
import model.PriorityStudent;
import model.ReturnResult;
import model.Student;
import repository.BookRepository;
import repository.BorrowDetailRepository;
import repository.BorrowSlipRepository;
import repository.PaymentRepository;
import repository.StudentRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;



public class ReturnService {

    public static final double FINE_PER_DAY = 5000.0; // Phí phạt trễ hạn mỗi ngày (5.000 đ)

    private final BorrowSlipRepository borrowSlipRepo;
    private final BorrowDetailRepository borrowDetailRepo;
    private final BookRepository bookRepo;
    private final PaymentRepository paymentRepo;
    private final StudentRepository studentRepo;

    public ReturnService() {
        this.borrowSlipRepo = new BorrowSlipRepository();
        this.borrowDetailRepo = new BorrowDetailRepository();
        this.bookRepo = new BookRepository();
        this.paymentRepo = new PaymentRepository();
        this.studentRepo = new StudentRepository();
    }

    public ReturnService(BorrowSlipRepository borrowSlipRepo,
                         BorrowDetailRepository borrowDetailRepo,
                         BookRepository bookRepo,
                         PaymentRepository paymentRepo,
                         StudentRepository studentRepo) {
        this.borrowSlipRepo = borrowSlipRepo;
        this.borrowDetailRepo = borrowDetailRepo;
        this.bookRepo = bookRepo;
        this.paymentRepo = paymentRepo;
        this.studentRepo = studentRepo;
    }

    /**
     * Tra cứu thông tin hồ sơ sinh viên theo MSSV.
     *
     * @param studentId mã số sinh viên
     * @return đối tượng Student hoặc PriorityStudent
     * @throws Exception nếu mã rỗng hoặc không tìm thấy sinh viên
     */
    public Student getStudentInfo(String studentId) throws Exception {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã thẻ sinh viên không được để trống.");
        }
        Student student = studentRepo.findByStudentId(studentId.trim());
        if (student == null) {
            throw new Exception("Không tìm thấy thông tin sinh viên với mã: " + studentId);
        }
        return student;
    }

    /**
     * Lấy danh sách các đầu sách mà sinh viên đang mượn (thuộc các phiếu chưa trả).
     *
     * @param studentId mã số sinh viên
     * @return danh sách BorrowingItemDTO hiển thị lên bảng
     * @throws Exception nếu xảy ra lỗi truy vấn
     */
    public List<BorrowingItemDTO> getBorrowingDetailsByStudent(String studentId) throws Exception {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã thẻ sinh viên không được để trống.");
        }

        List<BorrowSlip> activeSlips = borrowSlipRepo.findActiveSlipsByStudentId(studentId.trim());
        List<BorrowingItemDTO> items = new ArrayList<>();

        for (BorrowSlip slip : activeSlips) {
            List<BorrowDetail> details = borrowDetailRepo.findBySlipId(slip.getSlipId());
            for (BorrowDetail detail : details) {
                Book book = bookRepo.findById(detail.getBookId());
                String bookTitle = (book != null) ? book.getTitle() : ("Sách " + detail.getBookId());

                BorrowingItemDTO item = new BorrowingItemDTO(
                        slip.getSlipId(),
                        detail.getBookId(),
                        bookTitle,
                        slip.getDueDate(),
                        detail.getQuantity()
                );
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Tính số ngày trễ hạn dựa trên hạn trả và thời điểm trả thực tế.
     *
     * @param dueDate hạn trả của phiếu mượn
     * @param actualReturnDate thời điểm trả thực tế (hoặc thời điểm hiện tại)
     * @return số ngày trễ hạn (>= 0)
     */
    public long calculateLateDays(Timestamp dueDate, Timestamp actualReturnDate) {
        if (dueDate == null || actualReturnDate == null) {
            return 0;
        }
        if (actualReturnDate.after(dueDate)) {
            long diffMillis = actualReturnDate.getTime() - dueDate.getTime();
            long days = (long) Math.ceil((double) diffMillis / (1000L * 60L * 60L * 24L));
            return Math.max(0, days);
        }
        return 0;
    }

    /**
     * Tính toán tổng số tiền phạt dựa trên số ngày trễ và diện đối tượng sinh viên.
     * Tự động áp dụng giảm giá nếu sinh viên thuộc diện ưu tiên (PriorityStudent).
     *
     * @param lateDays số ngày trễ
     * @param student đối tượng sinh viên
     * @param compensationFees phí đền bù hư hỏng/mất sách
     * @return tổng số tiền phạt phát sinh
     */
    public double calculateFine(long lateDays, Student student, double compensationFees) {
        double originalLateFine = Math.max(0, lateDays) * FINE_PER_DAY;
        double actualLateFine = originalLateFine;

        if (student instanceof PriorityStudent ps) {
            actualLateFine = ps.calculateDiscountedFine(originalLateFine);
        }

        return actualLateFine + Math.max(0, compensationFees);
    }

    /**
     * Xác nhận trả sách với giao dịch Database Transaction.
     * Thực hiện:
     * 1. Cập nhật ngày trả thực tế cho phiếu mượn.
     * 2. Tăng số lượng tồn kho đầu sách được trả.
     * 3. Tạo bản ghi thanh toán phạt và cộng nợ sinh viên nếu có trễ hạn/hỏng.
     * 4. Giảm số lượng sách đang mượn của sinh viên.
     *
     * @param scannedItems danh sách các cuốn sách đã quét
     * @param slipId mã phiếu mượn
     * @param student đối tượng sinh viên
     * @param librarianId mã thủ thư đang thực hiện
     * @return ReturnResult kết quả trả sách
     */
    public ReturnResult xacNhanTraSach(List<BorrowingItemDTO> scannedItems,
                                       String slipId,
                                       Student student,
                                       String librarianId) {

        if (scannedItems == null || scannedItems.isEmpty()) {
            return new ReturnResult(false, "Vui lòng quét ít nhất một cuốn sách trước khi xác nhận trả.", slipId, 0, 0);
        }

        List<BorrowingItemDTO> validScanned = scannedItems.stream()
                .filter(BorrowingItemDTO::isScanned)
                .toList();

        if (validScanned.isEmpty()) {
            return new ReturnResult(false, "Không có cuốn sách nào ở trạng thái 'Đã quét'.", slipId, 0, 0);
        }

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            Timestamp now = new Timestamp(System.currentTimeMillis());

            // 1. Cập nhật ngày trả thực tế của phiếu mượn
            boolean slipUpdated = borrowSlipRepo.updateReturnDate(slipId, now, conn);
            if (!slipUpdated) {
                throw new SQLException("Không thể cập nhật ngày trả cho phiếu mượn: " + slipId);
            }

            // 2. Cập nhật tồn kho cho các đầu sách được trả
            int totalReturnedCount = 0;
            for (BorrowingItemDTO item : validScanned) {
                // Nếu sách bị mất thì không tăng lại vào kho, nếu tốt hoặc hư hỏng thì trả về kho
                if (!"Mất sách".equalsIgnoreCase(item.getPhysicalCondition())) {
                    boolean stockUpdated = bookRepo.increaseStock(item.getBookId(), item.getQuantity(), conn);
                    if (!stockUpdated) {
                        throw new SQLException("Lỗi cập nhật tồn kho cho đầu sách: " + item.getBookId());
                    }
                }
                totalReturnedCount += item.getQuantity();
            }

            // 3. Tính toán trễ hạn & tiền phạt
            Timestamp dueDate = validScanned.get(0).getDueDate();
            long lateDays = calculateLateDays(dueDate, now);
            double totalCompFee = validScanned.stream()
                    .mapToDouble(BorrowingItemDTO::getCompensationFee)
                    .sum();
            double totalFine = calculateFine(lateDays, student, totalCompFee);

            // 4. Nếu phát sinh tiền phạt -> Lưu bảng thanh_toan và cộng nợ sinh viên
            if (totalFine > 0) {
                paymentRepo.createFineRecord(slipId, totalFine, "TIEN_MAT", "CHUA_THANH_TOAN", conn);
                studentRepo.updateDebtAmount(student.getStudentId(), totalFine, conn);
            }

            // 5. Cập nhật giảm số sách đang mượn của sinh viên
            studentRepo.decreaseBorrowedCount(student.getStudentId(), totalReturnedCount, conn);

            // Commit thành công toàn bộ giao dịch
            conn.commit();

            String successMsg = "Trả sách thành công!";
            if (totalFine > 0) {
                successMsg += String.format(" Phát sinh phạt trễ/hỏng: %,.0f VNĐ (Trễ %d ngày).", totalFine, lateDays);
            }

            return new ReturnResult(true, successMsg, slipId, totalFine, lateDays);

        } catch (SQLException e) {
            // Rollback giao dịch nếu gặp bất kỳ lỗi CSDL nào
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return new ReturnResult(false, "Mất kết nối CSDL lúc lưu. Phiếu mượn chưa đóng. Chi tiết: " + e.getMessage(), slipId, 0, 0);

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return new ReturnResult(false, "Hệ thống gặp sự cố: " + e.getMessage(), slipId, 0, 0);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
