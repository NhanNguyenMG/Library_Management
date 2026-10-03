# HƯỚNG DẪN CÀI ĐẶT CƠ SỞ DỮ LIỆU MYSQL (QUẢN LÝ THƯ VIỆN)
> Dành cho tất cả các thành viên trong nhóm 9  
> Người phụ trách: **Người số 5 (Hạ tầng chung & CSDL)**

---

## 1. TỔNG QUAN FILE CSDL
Trong thư mục `LibraryManagement/src/db/` gồm các file sau:
1. `setup_database.sql`: **File chạy 1-click duy nhất**, tự động tạo DB, tạo 8 bảng, tạo 12 index và chèn dữ liệu mẫu test đầy đủ. (👉 **Khuyên dùng file này**).
2. `QLTV.sql`: DDL định nghĩa 8 bảng và ràng buộc khóa ngoại.
3. `ThuVien_Indexes.sql`: Định nghĩa các Index tối ưu hóa truy vấn.
4. `seed_data.sql`: Dữ liệu kiểm thử mẫu cho sinh viên, sách, tài khoản, phiếu mượn.

---

## 2. CÁCH CHẠY SCRIPT TẠO DATABASE

### Cách 1: Chạy bằng MySQL Workbench (Đơn giản nhất)
1. Mở **MySQL Workbench** và kết nối tới Local MySQL Instance (thường là `root` / mật khẩu của bạn).
2. Chọn menu **File** -> **Open SQL Script...** (hoặc phím tắt `Ctrl + Shift + O`).
3. Trỏ tới file: `LibraryManagement/src/db/setup_database.sql`.
4. Nhấn nút **Execute** (biểu tượng tia sét vàng ⚡) hoặc phím tắt `Ctrl + Shift + Enter`.
5. Tab **Output** bên dưới báo xanh toàn bộ và có dòng `CÀI ĐẶT CSDL QUẢN LÝ THƯ VIỆN THÀNH CÔNG!` là xong.

---

### Cách 2: Chạy bằng phpMyAdmin (Nếu dùng XAMPP / WampServer)
1. Bật **Apache** và **MySQL** trong XAMPP Control Panel.
2. Mở trình duyệt vào `http://localhost/phpmyadmin`.
3. Nhấp vào tab **Import** (Nhập).
4. Nhấn **Choose File** và chọn file `setup_database.sql`.
5. Cuộn xuống dưới cùng và nhấn **Import** (Thực hiện).

---

### Cách 3: Chạy bằng dòng lệnh (Terminal / Command Prompt / PowerShell)
Mở terminal tại thư mục chứa file và gõ:
```bash
mysql -u root -p < setup_database.sql
```
*(Sau đó nhập mật khẩu root MySQL của máy bạn).*

---

## 3. DANH SÁCH TÀI KHOẢN MẪU ĐỂ TEST HỆ THỐNG

| Tên đăng nhập | Mật khẩu | Quyền truy cập | Đối tượng đại diện | Ghi chú kiểm thử |
| :--- | :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `QUAN_LI` | Nguyễn Văn Quản Trị (QL0001) | Xem báo cáo, quản lý nhân sự |
| `thuthu01` | `thuthu123` | `THU_THU` | Trần Thị Mai (TT0001) | Lập phiếu mượn, trả sách |
| `thuthu02` | `thuthu123` | `THU_THU` | Lê Văn Hùng (TT0002) | Lập phiếu mượn, trả sách |
| `sv001` | `123456` | `SINH_VIEN` | Đặng Gia Bảo (SV0001) | **Đang có phiếu mượn PM0001 quá hạn trả** (test UC-05/06 tính phạt) |
| `sv002` | `123456` | `SINH_VIEN` | Trần Bảo Ngọc (SV0002) | **Sinh viên ưu tiên** (giảm 20% tiền phạt - test UC-07) |
| `sv003` | `123456` | `SINH_VIEN` | Phạm Hoàng Nam (SV0003) | **Đang nợ phạt 30.000đ** (test chặn mượn tiếp nếu có nợ) |
| `sv004` | `123456` | `SINH_VIEN` | Vũ Minh Quân (SV0004) | Sinh viên chưa mượn cuốn nào |
| `sv_locked` | `123456` | `SINH_VIEN` | Tài khoản bị khóa | Test trường hợp đăng nhập báo tài khoản bị khóa |
