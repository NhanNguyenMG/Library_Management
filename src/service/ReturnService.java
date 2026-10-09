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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



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
            List<BorrowDetail> details = borrowDetailRepo.findActiveBySlipId(slip.getSlipId());
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
     * Tính toán tổng số tiền phạt dựa trên từng cuốn sách được quét.
     * Mỗi cuốn tính trễ riêng theo hạn trả của cuốn đó và nhân với số lượng sách (SRS 2.3.7 bước 4):
     * Tiền phạt = (tổng số ngày trễ * FINE_PER_DAY * số lượng) - (chiết khấu sinh viên ưu tiên nếu có) + phí hư hỏng/mất sách.
     *
     * @param items danh sách các cuốn sách được quét
     * @param student đối tượng sinh viên
     * @param actualReturnDate thời điểm trả thực tế
     * @return tổng số tiền phạt phát sinh
     */
    public double calculateTotalFineForItems(List<BorrowingItemDTO> items, Student student, Timestamp actualReturnDate) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }
        double totalOriginalLateFine = 0.0;
        double totalCompFee = 0.0;
        for (BorrowingItemDTO item : items) {
            if (item.isScanned()) {
                long days = calculateLateDays(item.getDueDate(), actualReturnDate);
                totalOriginalLateFine += Math.max(0, days) * FINE_PER_DAY * Math.max(1, item.getQuantity());
                totalCompFee += Math.max(0, item.getCompensationFee());
            }
        }
        double actualLateFine = totalOriginalLateFine;
        if (student instanceof PriorityStudent ps) {
            actualLateFine = ps.calculateDiscountedFine(totalOriginalLateFine);
        }
        return actualLateFine + totalCompFee;
    }

    /**
     * Lấy số ngày trễ lớn nhất trong các cuốn sách được quét.
     *
     * @param items danh sách các cuốn sách được quét
     * @param actualReturnDate thời điểm trả thực tế
     * @return số ngày trễ lớn nhất (>= 0)
     */
    public long calculateMaxLateDays(List<BorrowingItemDTO> items, Timestamp actualReturnDate) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        long maxDays = 0;
        for (BorrowingItemDTO item : items) {
            if (item.isScanned()) {
                long days = calculateLateDays(item.getDueDate(), actualReturnDate);
                if (days > maxDays) {
                    maxDays = days;
                }
            }
        }
        return maxDays;
    }

    /**
     * Xác nhận trả sách với giao dịch Database Transaction (mặc định chưa thu tiền mặt ngay).
     */
    public ReturnResult xacNhanTraSach(List<BorrowingItemDTO> scannedItems,
                                       String slipId,
                                       Student student,
                                       String librarianId) {
        return xacNhanTraSach(scannedItems, slipId, student, librarianId, false);
    }

    /**
     * Xác nhận trả sách với giao dịch Database Transaction.
     * Hỗ trợ trả sách từng phần (Partial Return), quét sách từ nhiều phiếu khác nhau,
     * tính phạt riêng theo từng cuốn sách và tuỳ chọn thu tiền mặt ngay hoặc ghi nợ (Điểm chạm 2).
     *
     * @param scannedItems danh sách các cuốn sách đã quét
     * @param slipId mã phiếu mượn mặc định (nếu item không chỉ định)
     * @param student đối tượng sinh viên
     * @param librarianId mã thủ thư đang thực hiện
     * @param isPaidNow true nếu đã thu tiền mặt tại quầy (Điểm chạm 2), false nếu ghi nợ để trả sau
     * @return ReturnResult kết quả trả sách
     */
    public ReturnResult xacNhanTraSach(List<BorrowingItemDTO> scannedItems,
                                       String slipId,
                                       Student student,
                                       String librarianId,
                                       boolean isPaidNow) {

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

            // Gom nhóm sách theo từng mã phiếu mượn (để hỗ trợ trường hợp quét sách thuộc các phiếu khác nhau)
            Map<String, List<BorrowingItemDTO>> itemsBySlip = validScanned.stream()
                    .collect(Collectors.groupingBy(
                            item -> (item.getSlipId() != null && !item.getSlipId().isEmpty()) ? item.getSlipId() : slipId,
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            int totalReturnedCount = 0;

            for (Map.Entry<String, List<BorrowingItemDTO>> entry : itemsBySlip.entrySet()) {
                String currentSlipId = entry.getKey();
                List<BorrowingItemDTO> slipItems = entry.getValue();

                for (BorrowingItemDTO item : slipItems) {
                    // 1. Đánh dấu sách đã trả trong chi tiết phiếu mượn (ghi_chu = 'DA_TRA')
                    borrowDetailRepo.markAsReturned(currentSlipId, item.getBookId(), conn);

                    // 2. Cập nhật tồn kho cho các đầu sách được trả (nếu không mất)
                    if (!"Mất sách".equalsIgnoreCase(item.getPhysicalCondition())) {
                        boolean stockUpdated = bookRepo.increaseStock(item.getBookId(), item.getQuantity(), conn);
                        if (!stockUpdated) {
                            throw new SQLException("Lỗi cập nhật tồn kho cho đầu sách: " + item.getBookId());
                        }
                    }
                    totalReturnedCount += item.getQuantity();
                }

                // 3. Kiểm tra xem phiếu mượn còn cuốn sách nào chưa trả không
                int remainingUnreturned = borrowDetailRepo.countUnreturnedDetails(currentSlipId, conn);
                if (remainingUnreturned == 0) {
                    // Nếu đã trả hết toàn bộ sách trong phiếu -> Cập nhật ngày trả thực tế để đóng phiếu
                    boolean slipUpdated = borrowSlipRepo.updateReturnDate(currentSlipId, now, conn);
                    if (!slipUpdated) {
                        throw new SQLException("Không thể cập nhật ngày trả cho phiếu mượn: " + currentSlipId);
                    }
                }

                // 4. Tính tiền phạt phát sinh cho riêng phiếu này và ghi nhận vào bảng thanh_toan
                double slipFine = calculateTotalFineForItems(slipItems, student, now);
                if (slipFine > 0) {
                    String method = "TIEN_MAT";
                    String status = isPaidNow ? "DA_THANH_TOAN" : "CHUA_THANH_TOAN";
                    paymentRepo.saveOrUpdateFineRecord(currentSlipId, slipFine, method, status, conn);
                }
            }

            // 5. Tính toán tổng tiền phạt và số ngày trễ tối đa của toàn bộ lượt quét
            double totalFine = calculateTotalFineForItems(validScanned, student, now);
            long maxLateDays = calculateMaxLateDays(validScanned, now);

            // 6. Điểm chạm 2: Nếu chưa thu tiền mặt ngay (Ghi nợ) -> Cộng nợ vào sinh_vien.so_tien_no
            // Nếu đã thu tiền mặt ngay -> KHÔNG cộng nợ vào sinh_vien
            if (totalFine > 0 && !isPaidNow) {
                studentRepo.updateDebtAmount(student.getStudentId(), totalFine, conn);
            }

            // 7. Cập nhật giảm số sách đang mượn của sinh viên
            studentRepo.decreaseBorrowedCount(student.getStudentId(), totalReturnedCount, conn);

            // Commit thành công toàn bộ giao dịch
            conn.commit();

            String slipsSummary = String.join(", ", itemsBySlip.keySet());
            String successMsg = "Trả sách thành công!";
            if (totalFine > 0) {
                if (isPaidNow) {
                    successMsg += String.format(" Đã thu tiền mặt: %,.0f VNĐ (Trễ tối đa %d ngày).", totalFine, maxLateDays);
                } else {
                    successMsg += String.format(" Đã ghi nợ vào tài khoản sinh viên: %,.0f VNĐ (Trễ tối đa %d ngày).", totalFine, maxLateDays);
                }
            }

            return new ReturnResult(true, successMsg, slipsSummary, totalFine, maxLateDays);

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
