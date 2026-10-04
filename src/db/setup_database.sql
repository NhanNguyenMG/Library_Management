-- =====================================================================
-- DỰ ÁN: HỆ THỐNG QUẢN LÝ THƯ VIỆN (LIBRARY MANAGEMENT SYSTEM)
-- MÔN HỌC: CÔNG NGHỆ PHẦN MỀM (SOEN330679)
-- FILE TỔNG HỢP: setup_database.sql (1-CLICK SETUP)
-- BAO GỒM: TẠO DATABASE -> TẠO BẢNG -> TẠO INDEX -> CHÈN SEED DATA
-- NGƯỜI THỰC HIỆN: Người số 5 (Hạ tầng chung & CSDL)
-- ĐÃ CHUẨN HÓA THEO CODING_STANDARDS.md & PROJECT_PLAN.md
-- =====================================================================

-- -------------------------------------------------------------
-- PHẦN 1: KHỞI TẠO CƠ SỞ DỮ LIỆU
-- -------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS quan_li_thu_vien 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE quan_li_thu_vien;

SET FOREIGN_KEY_CHECKS = 0;

-- Xóa các bảng cũ nếu đã tồn tại
DROP TABLE IF EXISTS thanh_toan;
DROP TABLE IF EXISTS chi_tiet_phieu_muon;
DROP TABLE IF EXISTS phieu_muon;
DROP TABLE IF EXISTS dau_sach;
DROP TABLE IF EXISTS sinh_vien;
DROP TABLE IF EXISTS thu_thu;
DROP TABLE IF EXISTS quan_li;
DROP TABLE IF EXISTS tai_khoan;

-- -------------------------------------------------------------
-- PHẦN 2: TẠO CẤU TRÚC 8 BẢNG THEO SRS & GLOSSARY MAPPING
-- -------------------------------------------------------------
CREATE TABLE tai_khoan (
    ten_dang_nhap VARCHAR(50) PRIMARY KEY,
    mat_khau VARCHAR(255) NOT NULL COMMENT 'Độ dài 255 ký tự hỗ trợ mã hóa mật khẩu theo SRS',
    quyen_truy_cap VARCHAR(20) NOT NULL COMMENT 'QUAN_LI, THU_THU, SINH_VIEN',
    trang_thai VARCHAR(20) NOT NULL COMMENT 'ACTIVE, LOCKED, INACTIVE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quan_li (
    ma_quan_li VARCHAR(12) PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    ho_ten VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    chuc_vu VARCHAR(50) NOT NULL,
    FOREIGN KEY (ten_dang_nhap) REFERENCES tai_khoan(ten_dang_nhap) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE thu_thu (
    ma_nhan_vien VARCHAR(12) PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    ho_ten VARCHAR(50) NOT NULL,
    ca_truc VARCHAR(30) NOT NULL,
    FOREIGN KEY (ten_dang_nhap) REFERENCES tai_khoan(ten_dang_nhap) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sinh_vien (
    mssv VARCHAR(12) PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    ho_ten VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    sdt VARCHAR(12) NOT NULL UNIQUE,
    so_sach_dang_muon INT NOT NULL DEFAULT 0,
    so_tien_no DECIMAL(12,2) DEFAULT 0,
    muc_giam_gia DECIMAL(5,2) DEFAULT 0 COMMENT 'Phần trăm giảm phạt (ví dụ: 20.00 là giảm 20%)',
    li_do TEXT COMMENT 'Lý do thuộc diện ưu tiên',
    loai VARCHAR(50) NOT NULL COMMENT 'THUONG hoặc UU_TIEN', 
    FOREIGN KEY (ten_dang_nhap) REFERENCES tai_khoan(ten_dang_nhap) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE dau_sach (
    ma_dau_sach VARCHAR(12) PRIMARY KEY,
    tac_sach VARCHAR(255) NOT NULL COMMENT 'Tên sách / Tựa sách',
    tac_gia VARCHAR(150) NOT NULL,
    mo_ta TEXT,
    so_luong_con INT NOT NULL,
    ten_sach VARCHAR(255) GENERATED ALWAYS AS (tac_sach) VIRTUAL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE phieu_muon (
    ma_phieu VARCHAR(12) PRIMARY KEY,
    mssv VARCHAR(12) NOT NULL,
    ma_nhan_vien VARCHAR(12) NOT NULL,
    ngay_muon TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    han_tra TIMESTAMP NOT NULL,
    ngay_tra_thuc_te TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (mssv) REFERENCES sinh_vien(mssv) ON DELETE RESTRICT,
    FOREIGN KEY (ma_nhan_vien) REFERENCES thu_thu(ma_nhan_vien) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE chi_tiet_phieu_muon (
    id VARCHAR(12) PRIMARY KEY,
    ma_phieu VARCHAR(12) NOT NULL,
    ma_dau_sach VARCHAR(12),
    so_luong INT NOT NULL CHECK (so_luong > 0),
    ghi_chu TEXT,
    CONSTRAINT uc_ma_phieu_dau_sach UNIQUE (ma_phieu, ma_dau_sach),
    FOREIGN KEY (ma_phieu) REFERENCES phieu_muon(ma_phieu) ON DELETE CASCADE,
    FOREIGN KEY (ma_dau_sach) REFERENCES dau_sach(ma_dau_sach) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE thanh_toan (
    ma_giao_dich VARCHAR(12) PRIMARY KEY,
    ma_phieu VARCHAR(12) NOT NULL UNIQUE,
    so_tien DECIMAL(12,2) NOT NULL,
    phuong_thuc VARCHAR(50) NOT NULL COMMENT 'TIEN_MAT, CHUYEN_KHOAN, VNPAY, CASH, BANK_TRANSFER',
    trang_thai VARCHAR(50) NOT NULL COMMENT 'DA_THANH_TOAN, CHUA_THANH_TOAN, PAID, PENDING',
    thoi_gian TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ma_phieu) REFERENCES phieu_muon(ma_phieu) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- PHẦN 3: TẠO CÁC INDEX TỐI ƯU TRUY VẤN
-- -------------------------------------------------------------
CREATE INDEX idx_phieumuon_mssv        ON phieu_muon(mssv);
CREATE INDEX idx_phieumuon_nhanvien    ON phieu_muon(ma_nhan_vien);
CREATE INDEX idx_phieumuon_hantra      ON phieu_muon(han_tra);
CREATE INDEX idx_phieumuon_ngaymuon    ON phieu_muon(ngay_muon);
CREATE INDEX idx_phieumuon_mssv_hantra ON phieu_muon(mssv, han_tra);

CREATE INDEX idx_chitiet_phieu         ON chi_tiet_phieu_muon(ma_phieu);
CREATE INDEX idx_chitiet_dausach       ON chi_tiet_phieu_muon(ma_dau_sach);

CREATE INDEX idx_dausach_tacsach       ON dau_sach(tac_sach);
CREATE INDEX idx_dausach_tacgia        ON dau_sach(tac_gia);
CREATE INDEX idx_dausach_soluongcon    ON dau_sach(ma_dau_sach, so_luong_con);

CREATE INDEX idx_sinhvien_sotienno     ON sinh_vien(so_tien_no);
CREATE INDEX idx_thanhtoan_trangthai   ON thanh_toan(trang_thai);

-- -------------------------------------------------------------
-- PHẦN 4: CHÈN BỘ DỮ LIỆU MẪU (SEED DATA ĐỦ 16 TEST CASE)
-- -------------------------------------------------------------
INSERT INTO tai_khoan (ten_dang_nhap, mat_khau, quyen_truy_cap, trang_thai) VALUES
('admin',     'admin123',  'QUAN_LI',   'ACTIVE'),
('thuthu01',  'thuthu123', 'THU_THU',   'ACTIVE'),
('thuthu02',  'thuthu123', 'THU_THU',   'ACTIVE'),
('sv001',     '123456',    'SINH_VIEN', 'ACTIVE'),
('sv002',     '123456',    'SINH_VIEN', 'ACTIVE'),
('sv003',     '123456',    'SINH_VIEN', 'ACTIVE'),
('sv004',     '123456',    'SINH_VIEN', 'ACTIVE'),
('sv_locked', '123456',    'SINH_VIEN', 'LOCKED');

INSERT INTO quan_li (ma_quan_li, ten_dang_nhap, ho_ten, email, chuc_vu) VALUES
('QL0001', 'admin', 'Nguyễn Văn Quản Trị', 'quantri@thuvien.edu.vn', 'Giám đốc Thư viện');

INSERT INTO thu_thu (ma_nhan_vien, ten_dang_nhap, ho_ten, ca_truc) VALUES
('TT0001', 'thuthu01', 'Trần Thị Mai', 'Ca Sáng (07:30 - 11:30)'),
('TT0002', 'thuthu02', 'Lê Văn Hùng',  'Ca Chiều (13:00 - 17:00)');

INSERT INTO sinh_vien (mssv, ten_dang_nhap, ho_ten, email, sdt, so_sach_dang_muon, so_tien_no, muc_giam_gia, li_do, loai) VALUES
('SV0001', 'sv001', 'Đặng Gia Bảo',   'baodg@student.edu.vn',  '0912345671', 2,     0.00, 0.00,  NULL,                                    'THUONG'),
('SV0002', 'sv002', 'Trần Bảo Ngọc',  'ngoctb@student.edu.vn', '0912345672', 1,     0.00, 20.00, 'Sinh viên diện chính sách khó khăn',    'UU_TIEN'),
('SV0003', 'sv003', 'Phạm Hoàng Nam', 'namph@student.edu.vn',  '0912345673', 1, 30000.00, 0.00,  NULL,                                    'THUONG'),
('SV0004', 'sv004', 'Vũ Minh Quân',   'quanvm@student.edu.vn', '0912345674', 0,     0.00, 0.00,  NULL,                                    'THUONG');

INSERT INTO dau_sach (ma_dau_sach, tac_sach, tac_gia, mo_ta, so_luong_con) VALUES
('DS0001', 'Nhập môn Công nghệ Phần mềm',        'Roger S. Pressman',  'Giáo trình tiêu chuẩn về các quy trình phát triển và kiểm thử phần mềm', 15),
('DS0002', 'Clean Code - Mã sạch',                'Robert C. Martin',   'Quy tắc và nghệ thuật viết code dễ đọc, dễ bảo trì cho lập trình viên',  8),
('DS0003', 'Design Patterns: Elements of Reusable Object-Oriented Software', 'GoF (Gang of Four)', '23 mẫu thiết kế hướng đối tượng kinh điển trong kỹ thuật phần mềm', 5),
('DS0004', 'Cơ sở Dữ liệu & Tối ưu SQL',          'Thomas Connolly',    'Kiến thức chuyên sâu về mô hình quan hệ, chuẩn hóa dữ liệu và Indexing', 10),
('DS0005', 'Lập trình Java Nâng cao & Desktop UI', 'Cay S. Horstmann',   'Lập trình ứng dụng Java Desktop với Swing, JDBC và đa luồng',            12),
('DS0006', 'Cấu trúc Dữ liệu và Giải thuật',      'Robert Sedgewick',   'Phân tích độ phức tạp thuật toán và cấu trúc cây, đồ thị',               20);

INSERT INTO phieu_muon (ma_phieu, mssv, ma_nhan_vien, ngay_muon, han_tra, ngay_tra_thuc_te) VALUES
('PM0001', 'SV0001', 'TT0001', '2026-09-15 09:00:00', '2026-09-29 09:00:00', NULL),
('PM0002', 'SV0002', 'TT0001', '2026-10-01 10:00:00', '2026-10-15 10:00:00', NULL),
('PM0003', 'SV0003', 'TT0002', '2026-09-01 08:30:00', '2026-09-15 08:30:00', '2026-09-20 14:00:00');

INSERT INTO chi_tiet_phieu_muon (id, ma_phieu, ma_dau_sach, so_luong, ghi_chu) VALUES
('CT0001', 'PM0001', 'DS0001', 1, 'Sách mới nguyên bản'),
('CT0002', 'PM0001', 'DS0002', 1, 'Bìa hơi nhăn nhẹ ở góc'),
('CT0003', 'PM0002', 'DS0003', 1, 'Ấn bản tái bản năm 2024'),
('CT0004', 'PM0003', 'DS0004', 1, 'Đã hoàn trả trễ 5 ngày');

INSERT INTO thanh_toan (ma_giao_dich, ma_phieu, so_tien, phuong_thuc, trang_thai, thoi_gian) VALUES
('GD0001', 'PM0003', 30000.00, 'TIEN_MAT', 'CHUA_THANH_TOAN', '2026-09-20 14:05:00');

SET FOREIGN_KEY_CHECKS = 1;

-- Thông báo hoàn thành
SELECT 'CÀI ĐẶT CSDL QUẢN LÝ THƯ VIỆN THÀNH CÔNG!' AS Status;
