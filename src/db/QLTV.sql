-- =====================================================================
-- DỰ ÁN: HỆ THỐNG QUẢN LÝ THƯ VIỆN (LIBRARY MANAGEMENT SYSTEM)
-- MÔN HỌC: CÔNG NGHỆ PHẦN MỀM (SOEN330679)
-- FILE: QLTV.sql (Data Definition Language - DDL)
-- NGƯỜI THỰC HIỆN: Người số 5 (Hạ tầng chung & CSDL)
-- ĐÃ CHUẨN HÓA THEO CODING_STANDARDS.md & PROJECT_PLAN.md (Giai đoạn 1)
-- =====================================================================

-- 1. Khởi tạo Database nếu chưa tồn tại
CREATE DATABASE IF NOT EXISTS quan_li_thu_vien 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE quan_li_thu_vien;

-- 2. Xóa các bảng cũ theo thứ tự đảo ngược ràng buộc khóa ngoại (để tránh lỗi FK constraint)
DROP TABLE IF EXISTS thanh_toan;
DROP TABLE IF EXISTS chi_tiet_phieu_muon;
DROP TABLE IF EXISTS phieu_muon;
DROP TABLE IF EXISTS dau_sach;
DROP TABLE IF EXISTS sinh_vien;
DROP TABLE IF EXISTS thu_thu;
DROP TABLE IF EXISTS quan_li;
DROP TABLE IF EXISTS tai_khoan;

-- =====================================================================
-- 3. Tạo các bảng theo lược đồ chuẩn (8 Entity Models theo SRS)
-- =====================================================================

-- Bảng 1: Tài khoản người dùng (Dùng chung cho Quản lý, Thủ thư, Sinh viên)
CREATE TABLE tai_khoan (
    ten_dang_nhap VARCHAR(50) PRIMARY KEY,
    mat_khau VARCHAR(255) NOT NULL COMMENT 'Độ dài 255 ký tự hỗ trợ mã hóa mật khẩu theo SRS',
    quyen_truy_cap VARCHAR(20) NOT NULL COMMENT 'QUAN_LI, THU_THU, SINH_VIEN',
    trang_thai VARCHAR(20) NOT NULL COMMENT 'ACTIVE, LOCKED, INACTIVE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng 2: Thông tin Quản lý
CREATE TABLE quan_li (
    ma_quan_li VARCHAR(12) PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    ho_ten VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    chuc_vu VARCHAR(50) NOT NULL,
    FOREIGN KEY (ten_dang_nhap) REFERENCES tai_khoan(ten_dang_nhap) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng 3: Thông tin Thủ thư
CREATE TABLE thu_thu (
    ma_nhan_vien VARCHAR(12) PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    ho_ten VARCHAR(50) NOT NULL,
    ca_truc VARCHAR(30) NOT NULL,
    FOREIGN KEY (ten_dang_nhap) REFERENCES tai_khoan(ten_dang_nhap) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng 4: Thông tin Sinh viên (Bao gồm cả Sinh viên thường và Sinh viên ưu tiên)
CREATE TABLE sinh_vien (
    mssv VARCHAR(12) PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    ho_ten VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    sdt VARCHAR(12) NOT NULL UNIQUE,
    so_sach_dang_muon INT NOT NULL DEFAULT 0,
    so_tien_no DECIMAL(12,2) DEFAULT 0,
    muc_giam_gia DECIMAL(5,2) DEFAULT 0 COMMENT 'Phần trăm giảm phạt (ví dụ: 0.20 là giảm 20%)',
    li_do TEXT COMMENT 'Lý do thuộc diện ưu tiên',
    loai VARCHAR(50) NOT NULL COMMENT 'THUONG hoặc UU_TIEN', 
    FOREIGN KEY (ten_dang_nhap) REFERENCES tai_khoan(ten_dang_nhap) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng 5: Đầu sách trong thư viện
CREATE TABLE dau_sach (
    ma_dau_sach VARCHAR(12) PRIMARY KEY,
    tac_sach VARCHAR(255) NOT NULL COMMENT 'Tên sách / Tựa sách',
    tac_gia VARCHAR(150) NOT NULL,
    mo_ta TEXT,
    so_luong_con INT NOT NULL,
    -- Cột sinh ảo ten_sach tương thích với cả 2 cách gọi tac_sach và ten_sach theo SRS và Glossary
    ten_sach VARCHAR(255) GENERATED ALWAYS AS (tac_sach) VIRTUAL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng 6: Phiếu mượn sách
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

-- Bảng 7: Chi tiết phiếu mượn (Giải quyết quan hệ N-N giữa Phiếu mượn và Đầu sách)
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

-- Bảng 8: Giao dịch thanh toán tiền phạt (UC-07)
CREATE TABLE thanh_toan (
    ma_giao_dich VARCHAR(12) PRIMARY KEY,
    ma_phieu VARCHAR(12) NOT NULL UNIQUE,
    so_tien DECIMAL(12,2) NOT NULL,
    phuong_thuc VARCHAR(50) NOT NULL COMMENT 'TIEN_MAT, CHUYEN_KHOAN, VNPAY, CASH, BANK_TRANSFER',
    trang_thai VARCHAR(50) NOT NULL COMMENT 'DA_THANH_TOAN, CHUA_THANH_TOAN, PAID, PENDING',
    thoi_gian TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ma_phieu) REFERENCES phieu_muon(ma_phieu) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
