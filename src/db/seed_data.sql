-- =====================================================================
-- DỰ ÁN: HỆ THỐNG QUẢN LÝ THƯ VIỆN (LIBRARY MANAGEMENT SYSTEM)
-- MÔN HỌC: CÔNG NGHỆ PHẦN MỀM (SOEN330679)
-- FILE: seed_data.sql (Dữ liệu mẫu phục vụ kiểm thử hệ thống)
-- NGƯỜI THỰC HIỆN: Người số 5 (Hạ tầng chung & CSDL)
-- =====================================================================

USE quan_li_thu_vien;

-- Tắt kiểm tra khóa ngoại tạm thời để chèn dữ liệu không bị xung đột
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Xóa dữ liệu cũ
DELETE FROM thanh_toan;
DELETE FROM chi_tiet_phieu_muon;
DELETE FROM phieu_muon;
DELETE FROM dau_sach;
DELETE FROM sinh_vien;
DELETE FROM thu_thu;
DELETE FROM quan_li;
DELETE FROM tai_khoan;

-- =====================================================================
-- 2. DỮ LIỆU TÀI KHOẢN (tai_khoan)
-- =====================================================================
INSERT INTO tai_khoan (ten_dang_nhap, mat_khau, quyen_truy_cap, trang_thai) VALUES
('admin',     'admin123',  'QUAN_LI',   'ACTIVE'),
('thuthu01',  'thuthu123', 'THU_THU',   'ACTIVE'),
('thuthu02',  'thuthu123', 'THU_THU',   'ACTIVE'),
('sv001',     '123456',    'SINH_VIEN', 'ACTIVE'),
('sv002',     '123456',    'SINH_VIEN', 'ACTIVE'),
('sv003',     '123456',    'SINH_VIEN', 'ACTIVE'),
('sv004',     '123456',    'SINH_VIEN', 'ACTIVE'),
('sv_locked', '123456',    'SINH_VIEN', 'LOCKED');

-- =====================================================================
-- 3. DỮ LIỆU QUẢN LÝ (quan_li)
-- =====================================================================
INSERT INTO quan_li (ma_quan_li, ten_dang_nhap, ho_ten, email, chuc_vu) VALUES
('QL0001', 'admin', 'Nguyễn Văn Quản Trị', 'quantri@thuvien.edu.vn', 'Giám đốc Thư viện');

-- =====================================================================
-- 4. DỮ LIỆU THỦ THƯ (thu_thu)
-- =====================================================================
INSERT INTO thu_thu (ma_nhan_vien, ten_dang_nhap, ho_ten, ca_truc) VALUES
('TT0001', 'thuthu01', 'Trần Thị Mai', 'Ca Sáng (07:30 - 11:30)'),
('TT0002', 'thuthu02', 'Lê Văn Hùng',  'Ca Chiều (13:00 - 17:00)');

-- =====================================================================
-- 5. DỮ LIỆU SINH VIÊN (sinh_vien)
-- Bao gồm sinh viên thường và sinh viên ưu tiên (hưởng giảm phạt)
-- =====================================================================
INSERT INTO sinh_vien (mssv, ten_dang_nhap, ho_ten, email, sdt, so_sach_dang_muon, so_tien_no, muc_giam_gia, li_do, loai) VALUES
('SV0001', 'sv001', 'Đặng Gia Bảo',   'baodg@student.edu.vn',  '0912345671', 2,     0.00, 0.00,  NULL,                                    'THUONG'),
('SV0002', 'sv002', 'Trần Bảo Ngọc',  'ngoctb@student.edu.vn', '0912345672', 1,     0.00, 20.00, 'Sinh viên diện chính sách khó khăn',    'UU_TIEN'),
('SV0003', 'sv003', 'Phạm Hoàng Nam', 'namph@student.edu.vn',  '0912345673', 1, 30000.00, 0.00,  NULL,                                    'THUONG'),
('SV0004', 'sv004', 'Vũ Minh Quân',   'quanvm@student.edu.vn', '0912345674', 0,     0.00, 0.00,  NULL,                                    'THUONG');

-- =====================================================================
-- 6. DỮ LIỆU ĐẦU SÁCH (dau_sach)
-- =====================================================================
INSERT INTO dau_sach (ma_dau_sach, tac_sach, tac_gia, mo_ta, so_luong_con) VALUES
('DS0001', 'Nhập môn Công nghệ Phần mềm',        'Roger S. Pressman',  'Giáo trình tiêu chuẩn về các quy trình phát triển và kiểm thử phần mềm', 15),
('DS0002', 'Clean Code - Mã sạch',                'Robert C. Martin',   'Quy tắc và nghệ thuật viết code dễ đọc, dễ bảo trì cho lập trình viên',  8),
('DS0003', 'Design Patterns: Elements of Reusable Object-Oriented Software', 'GoF (Gang of Four)', '23 mẫu thiết kế hướng đối tượng kinh điển trong kỹ thuật phần mềm', 5),
('DS0004', 'Cơ sở Dữ liệu & Tối ưu SQL',          'Thomas Connolly',    'Kiến thức chuyên sâu về mô hình quan hệ, chuẩn hóa dữ liệu và Indexing', 10),
('DS0005', 'Lập trình Java Nâng cao & Desktop UI', 'Cay S. Horstmann',   'Lập trình ứng dụng Java Desktop với Swing, JDBC và đa luồng',            12),
('DS0006', 'Cấu trúc Dữ liệu và Giải thuật',      'Robert Sedgewick',   'Phân tích độ phức tạp thuật toán và cấu trúc cây, đồ thị',               20);

-- =====================================================================
-- 7. DỮ LIỆU PHIẾU MƯỢN (phieu_muon)
-- Phục vụ kiểm thử:
-- - PM0001: Quá hạn trả (hạn trả 29/09/2026), chưa trả -> để Người 1 test trả trễ và tính phạt
-- - PM0002: Đang mượn còn hạn (hạn trả 15/10/2026) của SV ưu tiên
-- - PM0003: Đã trả trễ ngày 20/09/2026 và phát sinh phạt 30,000đ
-- =====================================================================
INSERT INTO phieu_muon (ma_phieu, mssv, ma_nhan_vien, ngay_muon, han_tra, ngay_tra_thuc_te) VALUES
('PM0001', 'SV0001', 'TT0001', '2026-09-15 09:00:00', '2026-09-29 09:00:00', NULL),
('PM0002', 'SV0002', 'TT0001', '2026-10-01 10:00:00', '2026-10-15 10:00:00', NULL),
('PM0003', 'SV0003', 'TT0002', '2026-09-01 08:30:00', '2026-09-15 08:30:00', '2026-09-20 14:00:00');

-- =====================================================================
-- 8. DỮ LIỆU CHI TIẾT PHIẾU MƯỢN (chi_tiet_phieu_muon)
-- =====================================================================
INSERT INTO chi_tiet_phieu_muon (id, ma_phieu, ma_dau_sach, so_luong, ghi_chu) VALUES
('CT0001', 'PM0001', 'DS0001', 1, 'Sách mới nguyên bản'),
('CT0002', 'PM0001', 'DS0002', 1, 'Bìa hơi nhăn nhẹ ở góc'),
('CT0003', 'PM0002', 'DS0003', 1, 'Ấn bản tái bản năm 2024'),
('CT0004', 'PM0003', 'DS0004', 1, 'Đã hoàn trả trễ 5 ngày');

-- =====================================================================
-- 9. DỮ LIỆU GIAO DỊCH PHẠT / THANH TOÁN (thanh_toan)
-- =====================================================================
INSERT INTO thanh_toan (ma_giao_dich, ma_phieu, so_tien, phuong_thuc, trang_thai, thoi_gian) VALUES
('GD0001', 'PM0003', 30000.00, 'TIEN_MAT', 'CHUA_THANH_TOAN', '2026-09-20 14:05:00');

-- Bật lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;
