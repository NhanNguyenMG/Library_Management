package service;

import db.DBConnection;
import repository.BookRepository;
import repository.BorrowDetailRepository;
import repository.BorrowSlipRepository;
import repository.PaymentRepository;
import repository.StudentRepository;
import dto.BorrowingItemDTO;
import dto.ReturnResultDTO;
import exception.BusinessException;
import exception.RepositoryException;
import model.Book;
import model.BorrowDetail;
import model.BorrowSlip;
import model.PriorityStudent;
import model.Student;
import util.ValidationUtil;

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
     */
    public Student getStudentInfo(String studentId) {
        ValidationUtil.validateStudentId(studentId);
        try {
            Student student = studentRepo.findByStudentId(studentId.trim());
            if (student == null) {
                throw new BusinessException("Không tìm thấy thông tin sinh viên với mã: " + studentId);
            }
            return student;
        } catch (SQLException e) {
            throw new RepositoryException("Lỗi truy vấn CSDL hồ sơ sinh viên: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách các đầu sách mà sinh viên đang mượn (thuộc các phiếu chưa trả).
     *
     * @param studentId mã số sinh viên
     * @return danh sách BorrowingItemDTO hiển thị lên bảng
     */
    public List<BorrowingItemDTO> getBorrowingDetailsByStudent(String studentId) {
        ValidationUtil.validateStudentId(studentId);

        try {
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
        } catch (SQLException e) {
            throw new RepositoryException("Lỗi truy vấn CSDL danh sách mượn sách: " + e.getMessage(), e);
        }
    }

    /**
     * Tính số ngày trễ hạn dựa vào hạn trả và ngày trả thực tế.
     *
     * @param dueDate hạn trả quy định
     * @param actualReturnDate ngày trả thực tế
     * @return số ngày trễ (0 nếu trả đúng hạn hoặc sớm hơn)
     */
    public long calculateLateDays(Timestamp dueDate, Timestamp actualReturnDate) {
        if (dueDate == null || actualReturnDate == null) {
            return 0;
        }

        long diffMillis = actualReturnDate.getTime() - dueDate.getTime();
        if (diffMillis <= 0) {
            return 0; // Trả đúng hạn hoặc sớm hạn
        }

        // Chuyển đổi mili-giây sang ngày (làm tròn lên)
        return (long) Math.ceil((double) diffMillis / (1000 * 60 * 60 * 24));
    }

    /**
     * Tính tiền phạt trả trễ và bồi thường thiệt hại (nếu có).
     * Áp dụng chính sách ưu đãi giảm trừ 20% phí trễ hạn cho đối tượng PriorityStudent.
     *
     * @param lateDays số ngày quá hạn
     * @param student đối tượng sinh viên
     * @param compensationFees phí bồi thường hư hỏng hoặc mất sách
     * @return tổng tiền phạt phát sinh (VNĐ)
     */
    public double calculateFine(long lateDays, Student student, double compensationFees) {
        double lateFine = 0.0;
        if (lateDays > 0) {
            lateFine = lateDays * FINE_PER_DAY;

            // Kiểm tra chính sách giảm trừ cho Sinh viên ưu tiên (Ủy quyền cho PriorityStudent)
            if (student instanceof PriorityStudent priorityStudent) {
                lateFine = priorityStudent.calculateDiscountedFine(lateFine);
            }
        }

        double totalFine = lateFine + Math.max(0.0, compensationFees);
        return Math.round(totalFine);
    }

    /**
     * Tính tổng số tiền phạt phát sinh cho danh sách các cuốn sách được quét.
     *
     * @param items danh sách các cuốn sách được quét
     * @param student đối tượng sinh viên
     * @param actualReturnDate thời điểm trả thực tế
     * @return tổng tiền phạt (VNĐ)
     */
    public double calculateTotalFineForItems(List<BorrowingItemDTO> items, Student student, Timestamp actualReturnDate) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (BorrowingItemDTO item : items) {
            if (item.isScanned()) {
                long lateDays = calculateLateDays(item.getDueDate(), actualReturnDate);
                double compensation = item.getCompensationFee();
                total += calculateFine(lateDays, student, compensation);
            }
        }
        return total;
    }

    /**
     * Tính số ngày trễ lớn nhất trong danh sách các cuốn sách được quét.
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
    public ReturnResultDTO confirmReturn(List<BorrowingItemDTO> scannedItems,
                                           String slipId,
                                           Student student,
                                           String librarianId) {
        return confirmReturn(scannedItems, slipId, student, librarianId, false);
    }

    /**
     * Xác nhận trả sách với giao dịch Database Transaction.
     * Hỗ trợ trả sách từng phần (Partial Return), quét sách từ nhiều phiếu khác nhau,
     * tính phạt riêng theo từng cuốn sách và phương thức xử lý phạt (thu trực tiếp hoặc ghi nhận nợ).
     *
     * @param scannedItems danh sách các cuốn sách đã quét
     * @param slipId mã phiếu mượn mặc định (nếu item không chỉ định)
     * @param student đối tượng sinh viên
     * @param librarianId mã thủ thư đang thực hiện
     * @param isPaidNow true nếu thu tiền mặt trực tiếp tại quầy, false nếu ghi nợ vào tài khoản để trả sau
     * @return ReturnResultDTO kết quả trả sách
     */
    public ReturnResultDTO confirmReturn(List<BorrowingItemDTO> scannedItems,
                                           String slipId,
                                           Student student,
                                           String librarianId,
                                           boolean isPaidNow) {

        if (scannedItems == null || scannedItems.isEmpty()) {
            return new ReturnResultDTO(false, "Vui lòng quét ít nhất một cuốn sách trước khi xác nhận trả sách.", slipId, 0, 0);
        }

        List<BorrowingItemDTO> validScanned = scannedItems.stream()
                .filter(BorrowingItemDTO::isScanned)
                .toList();

        if (validScanned.isEmpty()) {
            return new ReturnResultDTO(false, "Không có tài liệu nào trong danh sách ở trạng thái 'Đã quét'.", slipId, 0, 0);
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
                            throw new SQLException("Lỗi cập nhật số lượng tồn kho cho đầu sách: " + item.getBookId());
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

            // 6. Xử lý phí phạt: Nếu chưa thu tiền mặt ngay (ghi nợ) -> Cộng nợ vào sinh_vien.so_tien_no
            // Nếu đã thu tiền mặt ngay -> KHÔNG cộng nợ vào sinh_vien
            if (totalFine > 0 && !isPaidNow) {
                studentRepo.updateDebtAmount(student.getStudentId(), totalFine, conn);
            }

            // 7. Cập nhật giảm số sách đang mượn của sinh viên
            studentRepo.decreaseBorrowedCount(student.getStudentId(), totalReturnedCount, conn);

            // Commit thành công toàn bộ giao dịch
            conn.commit();

            String slipsSummary = String.join(", ", itemsBySlip.keySet());
            String successMsg = "Xác nhận hoàn tất thủ tục trả sách thành công.";
            if (totalFine > 0) {
                if (isPaidNow) {
                    successMsg += String.format(" Đã thu tiền mặt tại quầy: %,.0f VNĐ (Số ngày quá hạn tối đa: %d ngày).", totalFine, maxLateDays);
                } else {
                    successMsg += String.format(" Đã ghi nợ vào tài khoản sinh viên: %,.0f VNĐ (Số ngày quá hạn tối đa: %d ngày).", totalFine, maxLateDays);
                }
            }

            return new ReturnResultDTO(true, successMsg, slipsSummary, totalFine, maxLateDays);

        } catch (SQLException e) {
            // Rollback giao dịch nếu gặp bất kỳ lỗi CSDL nào
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return new ReturnResultDTO(false, "Lỗi cơ sở dữ liệu trong quá trình lưu dữ liệu trả sách. Giao dịch đã được khôi phục nguyên trạng. Chi tiết: " + e.getMessage(), slipId, 0, 0);

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return new ReturnResultDTO(false, "Lỗi hệ thống trong quá trình xử lý trả sách: " + e.getMessage(), slipId, 0, 0);

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
