# CODING STANDARDS & CONVENTIONS
## HỆ THỐNG QUẢN LÝ THƯ VIỆN (LIBRARY MANAGEMENT SYSTEM) — NHÓM 9

> **MỤC ĐÍCH CỦA TÀI LIỆU NÀY:**  
> Tài liệu này là **bộ quy chuẩn chung bắt buộc** cho toàn bộ 6 thành viên trong Nhóm 9. Mọi code trước khi commit vào nhánh `dev` phải tuân thủ nghiêm ngặt các quy tắc dưới đây nhằm tránh xung đột code, đảm bảo tính nhất quán của hệ thống và đạt điểm tối đa ở phần đánh giá **Traceability (Truy vết thiết kế)** môn Công nghệ Phần mềm.

---

## 1. NGUYÊN TẮC CỐT LÕI (CORE PRINCIPLES)

1. **Ngôn ngữ sử dụng:**
   - **100% TÊN CLASS, INTERFACE, METHOD, BIẾN, CONSTANT, PACKAGE viết bằng TIẾNG ANH**.
   - **COMMENT VÀ DỮ LIỆU HIỂN THỊ (GIAO DIỆN / THÔNG BÁO) viết bằng TIẾNG VIỆT CÓ DẤU**.
2. **Kiến trúc phân tầng đóng 4 tầng (Closed 4-Layer Architecture):**
   ```
   [Presentation Layer: ui/ + controller/]
             ↓ (chỉ được gọi tầng liền kề bên dưới)
   [Business Logic Layer: service/]
             ↓ (chỉ được gọi tầng liền kề bên dưới)
   [Data Access Layer: repository/]
             ↓
   [Database Layer: MySQL]
   ```
   - **QUY TẮC BẤT DI BẤT DỊCH:**
     - Tầng `ui` CHỈ ĐƯỢC GỌI tầng `controller`.
     - Tầng `controller` CHỈ ĐƯỢC GỌI tầng `service`.
     - Tầng `service` CHỈ ĐƯỢC GỌI tầng `repository`.
     - **TUYỆT ĐỐI KHÔNG NHẢY TẦNG** (VD: `ui` gọi thẳng `service` hay `repository` là **VI PHẠM NẶNG**).
     - **TUYỆT ĐỐI KHÔNG GỌI NGƯỢC CHIỀU** (VD: `repository` gọi `service`, `service` gọi `controller`).
3. **Mô hình MVC trong Presentation:**
   - View (`ui/`) và Model (`model/`) không được giao tiếp trực tiếp, mọi xử lý luồng đi qua `controller/`.
4. **Công nghệ:**
   - Dùng **Java Swing thuần** cho giao diện.
   - Dùng **JDBC thuần (`PreparedStatement`)**, KHÔNG dùng Hibernate/ORM.
   - KHÔNG dùng Spring Boot, KHÔNG dùng Maven/Gradle.

---

## 2. QUY CHUẨN ĐẶT TÊN (NAMING CONVENTIONS)

### 2.1. Cấu trúc Package
- Tên package viết **chữ thường toàn bộ (lowercase)**, danh từ số ít:
  - `ui`: Chứa các màn hình Swing JFrame / JPanel.
  - `controller`: Chứa các lớp điều khiển luồng dữ liệu giữa UI và Service.
  - `service`: Chứa logic nghiệp vụ, tính toán, kiểm tra điều kiện, quản lý Transaction.
  - `repository`: Chứa các hàm truy vấn CSDL (`SELECT`, `INSERT`, `UPDATE`, `DELETE`).
  - `model`: Chứa các lớp Entity (ánh xạ bảng) và DTO.
  - `db`: Chứa `DBConnection` và các file script SQL.

---

### 2.2. Đặt tên Lớp (Classes & Interfaces) — `PascalCase`
Mỗi lớp phải có hậu tố thể hiện rõ tầng kiến trúc của nó:

| Tầng | Định dạng tên | Ví dụ thực tế |
|---|---|---|
| **View (UI)** | `[ChứcNăng]Form.java` | `LoginForm.java`, `SearchBookForm.java`, `BorrowForm.java`, `ReturnBookForm.java` |
| **Controller** | `[ChứcNăng]Controller.java` | `LoginController.java`, `SearchController.java`, `BorrowController.java`, `ReturnController.java` |
| **Service** | `[ChứcNăng]Service.java` | `LoginService.java`, `SearchService.java`, `BorrowService.java`, `ReturnService.java` |
| **Repository** | `[Entity]Repository.java` | `AccountRepository.java`, `BookRepository.java`, `BorrowSlipRepository.java`, `BorrowDetailRepository.java`, `PaymentRepository.java` |
| **Entity Model** | `[TênThựcThể].java` | `Account.java`, `Student.java`, `PriorityStudent.java`, `Book.java`, `BorrowSlip.java`, `Payment.java` |
| **DTO** | `[Tên]DTO.java` hoặc `[Tên]Result.java` | `ReturnResult.java`, `FineInvoiceDTO.java` |

---

### 2.3. Đặt tên Phương thức (Methods) — `camelCase`
- Phương thức phải bắt đầu bằng một **ĐỘNG TỪ**:
  - **Getter / Setter**: `getXxx()`, `setXxx()`.
  - **Phương thức Boolean**: Bắt đầu bằng `isXxx()`, `hasXxx()`, `canXxx()` (VD: `isValid()`, `hasFine()`, `isLate()`).
  - **Repository (CRUD)**:
    - `findById(String id)`
    - `findAll()`
    - `save(Entity entity)`
    - `update(Entity entity)`
    - `deleteById(String id)`
  - **Service (Nghiệp vụ)**:
    - `authenticate(String username, String password)`
    - `searchBooks(String keyword, String searchType)`
    - `checkBorrowEligibility(String studentId)`
    - `createBorrowSlip(BorrowSlip slip, List<BorrowDetail> details)`
    - `verifyMatchingSlip(String slipId, String bookId)`
    - `calculateLateDaysAndFine(Timestamp dueDate, Timestamp returnDate, Student student)`
    - `confirmReturn(List<String> returnedBookIds, String slipId, String librarianId)`
  - **Controller**:
    - Điều phối: `login(...)`, `search(...)`, `handleBorrow(...)`, `confirmReturn(...)`
  - **UI Events (Form)**:
    - Bắt sự kiện: `onScanSuccess()`, `onScanCardEnter()`, `btnConfirmActionPerformed()`

---

### 2.4. Đặt tên Biến và Tham số (Variables & Parameters) — `camelCase`
- Tên biến phải **rõ ràng, có ý nghĩa nghiệp vụ**, không viết tắt khó hiểu:
  - ✅ **ĐÚNG:** `studentId`, `bookId`, `borrowSlipId`, `fineAmount`, `dueDate`, `returnDate`, `stockQuantity`.
  - ❌ **SAI:** `id`, `sId`, `b`, `a`, `temp`, `bien1`, `soLuong`.
- Biến danh sách (List, Collection) phải dùng số nhiều hoặc có tiền tố `list`:
  - ✅ **ĐÚNG:** `bookList`, `borrowDetails`, `returnedBookIds`.

---

### 2.5. Đặt tên Hằng số (Constants) — `UPPER_SNAKE_CASE`
- Các giá trị cấu hình, định mức cố định phải khai báo `public static final`:
  ```java
  public static final double FINE_RATE_PER_DAY = 5000.0; // 5.000 VNĐ / ngày trễ (REQ-014)
  public static final int MAX_BORROW_DAYS = 14;          // 14 ngày mượn tối đa
  public static final int MAX_BOOKS_ALLOWED = 5;         // Tối đa 5 cuốn / sinh viên (REQ-003)
  ```

---

## 3. BẢNG TỪ ĐIỂN THỐNG NHẤT THUẬT NGỮ (GLOSSARY MAPPING)

Để tránh trường hợp người gọi là `idSach`, người gọi là `bookCode`, người gọi `maDauSach`, toàn bộ 6 thành viên **bắt buộc dùng chung bảng ánh xạ sau**:

| Khái niệm (SRS) | Tên Class (Model) | Tên biến / Tham số trong code | Tên cột trong CSDL (MySQL) |
|---|---|---|---|
| Tài khoản | `Account` | `account`, `username`, `password`, `role` | `tai_khoan` (`ten_dang_nhap`, `mat_khau`, `quyen_truy_cap`) |
| Quản lí | `Manager` | `manager`, `managerId`, `fullName` | `quan_li` (`ma_quan_li`, `ho_ten`, `email`, `chuc_vu`) |
| Thủ thư | `Librarian` | `librarian`, `librarianId`, `shift` | `thu_thu` (`ma_nhan_vien`, `ho_ten`, `ca_truc`) |
| Sinh viên | `Student` | `student`, `studentId` (hoặc `mssv`), `borrowedCount`, `debtAmount` | `sinh_vien` (`mssv`, `ho_ten`, `so_sach_dang_muon`, `so_tien_no`) |
| Sinh viên ưu tiên | `PriorityStudent` | `discountRate`, `priorityReason` | `sinh_vien` (`muc_giam_gia`, `li_do`, `loai`) |
| Đầu sách | `Book` | `book`, `bookId`, `title`, `author`, `stockQuantity` | `dau_sach` (`ma_dau_sach`, `tac_sach` hoặc `ten_sach`, `tac_gia`, `so_luong_con`) |
| Phiếu mượn | `BorrowSlip` | `borrowSlip`, `slipId`, `borrowDate`, `dueDate`, `actualReturnDate` | `phieu_muon` (`ma_phieu`, `ngay_muon`, `han_tra`, `ngay_tra_thuc_te`) |
| Chi tiết phiếu mượn | `BorrowDetail` | `borrowDetail`, `detailId`, `quantity`, `note` | `chi_tiet_phieu_muon` (`id`, `ma_phieu`, `ma_dau_sach`, `so_luong`, `ghi_chu`) |
| Thanh toán / Khoản phạt | `Payment` | `payment`, `transactionId`, `amount`, `paymentMethod`, `status` | `thanh_toan` (`ma_giao_dich`, `ma_phieu`, `so_tien`, `phuong_thuc`, `trang_thai`) |
| Kết quả trả sách (DTO) | `ReturnResult` | `returnResult`, `isSuccess`, `message`, `fineAmount` | *(Không ánh xạ bảng)* |
| Hóa đơn phạt (DTO) | `FineInvoiceDTO` | `fineInvoiceDTO`, `slipId`, `studentName`, `totalFine` | *(Không ánh xạ bảng)* |

---

## 4. QUY TẮC PHÂN CHIA TRÁCH NHIỆM & XỬ LÝ LỖI

### 4.1. Validate định dạng (Form) vs Validate nghiệp vụ (Service)
> **Quy tắc vàng phân biệt:** *"Có cần truy vấn dữ liệu đã lưu trong hệ thống để kiểm tra không?"*
- **KHÔNG CẦN:** Làm ở **Form (UI)**:
  - Kiểm tra ô nhập có bị rỗng không (`trim().isEmpty()`).
  - Kiểm tra định dạng số, độ dài chuỗi ký tự.
- **CÓ CẦN:** Làm ở **Service**:
  - Mã sách quét có nằm trong phiếu mượn của sinh viên không?
  - Số lượng sách mượn có vượt quá hạn mức 5 cuốn không?
  - Sinh viên có đang bị nợ phạt chưa thanh toán không?
  - Tính toán số ngày trễ và tiền phạt.

---

### 4.2. Chuẩn JDBC trong Repository
1. **Luôn dùng `PreparedStatement`** và truyền tham số bằng dấu `?` để ngăn chặn hoàn toàn tấn công **SQL Injection (TC-04)**:
   ```java
   // ✅ ĐÚNG:
   String sql = "SELECT * FROM dau_sach WHERE ma_dau_sach = ?";
   try (PreparedStatement ps = conn.prepareStatement(sql)) {
       ps.setString(1, bookId);
       try (ResultSet rs = ps.executeQuery()) { ... }
   }
   // ❌ SAI NGHIÊM TRỌNG (Dễ bị SQL Injection):
   String sql = "SELECT * FROM dau_sach WHERE ma_dau_sach = '" + bookId + "'";
   ```
2. **Tự động đóng tài nguyên:** Sử dụng cấu trúc `try-with-resources` để `Connection`, `PreparedStatement` và `ResultSet` luôn được giải phóng an toàn.

---

### 4.3. Quản lý Giao dịch (Database Transaction) ở tầng Service
Khi một nghiệp vụ cần thay đổi trên **nhiều bảng CSDL** (ví dụ: Trả sách phải vừa cập nhật phiếu mượn, vừa tăng tồn kho đầu sách, vừa tạo bản ghi phạt):
- Tầng **Service** chịu trách nhiệm điều khiển Transaction:
  ```java
  Connection conn = null;
  try {
      conn = DBConnection.getInstance().getConnection();
      conn.setAutoCommit(false); // Bắt đầu Transaction

      // Bước 1: Gọi Repository cập nhật phiếu
      borrowSlipRepo.updateReturnDate(slipId, returnDate, conn);

      // Bước 2: Gọi Repository tăng tồn kho từng cuốn
      for (String bookId : returnedBookIds) {
          bookRepo.increaseStock(bookId, 1, conn);
      }

      // Bước 3: Nếu trễ hạn -> gọi Repository tạo bản ghi phạt
      if (fineAmount > 0) {
          paymentRepo.createFineRecord(slipId, fineAmount, "CASH", "PENDING", conn);
      }

      conn.commit(); // Thành công 100% -> Cam kết lưu vào DB
      return new ReturnResult(true, "Trả sách thành công!", fineAmount);
  } catch (Exception e) {
      if (conn != null) {
          try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } // Lỗi -> Hoàn tác toàn bộ
      }
      return new ReturnResult(false, "Lỗi hệ thống: " + e.getMessage(), 0);
  } finally {
      if (conn != null) {
          try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
      }
  }
  ```

---

## 5. QUY TẮC COMMENT & TRACEABILITY (BẮT BUỘC ĐỂ CHẤM ĐIỂM)

Đề bài thực hành `C6-D1` yêu cầu: **"Chỉ ra ít nhất 4 điểm cụ thể trong code tương ứng với element trong tài liệu thiết kế"**.  
Do đó, trước mỗi phương thức nghiệp vụ quan trọng trong Service và Controller, bạn **BẮT BUỘC** phải gắn khối comment chuẩn sau:

```java
/**
 * =========================================================================
 * Use Case: UC-05 Xử lý trả sách
 * Sequence Diagram: sd XuLyTraSach (Hình 7 SRS, mục 5.5.1)
 * Traceability Message: message #4 - returnService.confirmReturn()
 * Test Case tương ứng: TC-09 (Đúng hạn), TC-10 (Trễ 3 ngày), TC-11 (Trễ 1 ngày)
 * =========================================================================
 */
public ReturnResult confirmReturn(List<String> returnedBookIds, String slipId, String librarianId) {
    // ...
}
```

---

## 6. QUY TẮC GIT & COMMIT MESSAGE

- **Nhánh tích hợp chung:** `dev`
- **Nhánh cá nhân làm tính năng:** `feature/uc[SốUC]-[tên_ngắn]`
  - Ví dụ: `feature/uc05-return-book`, `feature/uc01-login`, `feature/uc03-borrow`
- **Quy chuẩn thông điệp Commit:**
  `feat(scope): mô tả công việc vừa làm #UC-XX`
  - Ví dụ:
    - `feat(return): cai dat ham tinh tien phat tre han #UC-07`
    - `feat(borrow): kiem tra han muc toi da 5 cuon sach #UC-04`
    - `feat(login): xu ly xac thuc tai khoan va phan quyen #UC-01`
    - `fix(return): sua loi khong rollback khi mat ket noi #UC-05`
