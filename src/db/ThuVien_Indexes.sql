-- =====================================================================
-- DỰ ÁN: HỆ THỐNG QUẢN LÝ THƯ VIỆN (LIBRARY MANAGEMENT SYSTEM)
-- MÔN HỌC: CÔNG NGHỆ PHẦN MỀM (SOEN330679)
-- FILE: ThuVien_Indexes.sql (Index Design & Query Optimization)
-- NGƯỜI THỰC HIỆN: Người số 5 (Hạ tầng chung & CSDL)
-- =====================================================================

USE quan_li_thu_vien;

-- 1. Index cho bảng phieu_muon (Tối ưu tra cứu lịch sử mượn, nhân viên lập phiếu)
CREATE INDEX idx_phieumuon_mssv        ON phieu_muon(mssv);
CREATE INDEX idx_phieumuon_nhanvien    ON phieu_muon(ma_nhan_vien);
CREATE INDEX idx_phieumuon_hantra      ON phieu_muon(han_tra);
CREATE INDEX idx_phieumuon_ngaymuon    ON phieu_muon(ngay_muon);

-- Composite index cho tra cứu phiếu mượn theo sinh viên và hạn trả (phục vụ kiểm tra quá hạn)
CREATE INDEX idx_phieumuon_mssv_hantra ON phieu_muon(mssv, han_tra);

-- 2. Index cho bảng chi_tiet_phieu_muon (Tối ưu JOIN giữa phiếu mượn và đầu sách)
CREATE INDEX idx_chitiet_phieu         ON chi_tiet_phieu_muon(ma_phieu);
CREATE INDEX idx_chitiet_dausach       ON chi_tiet_phieu_muon(ma_dau_sach);

-- 3. Index cho bảng dau_sach (Tối ưu tìm kiếm tài liệu theo tựa sách, tác giả và kiểm tra số lượng tồn)
CREATE INDEX idx_dausach_tacsach       ON dau_sach(tac_sach);
CREATE INDEX idx_dausach_tacgia        ON dau_sach(tac_gia);
CREATE INDEX idx_dausach_soluongcon    ON dau_sach(ma_dau_sach, so_luong_con);

-- 4. Index cho bảng sinh_vien (Tối ưu lọc danh sách sinh viên đang nợ tiền phạt)
CREATE INDEX idx_sinhvien_sotienno     ON sinh_vien(so_tien_no);

-- 5. Index cho bảng thanh_toan (Tối ưu lọc trạng thái thanh toán)
CREATE INDEX idx_thanhtoan_trangthai   ON thanh_toan(trang_thai);

-- =====================================================================
-- CÁC CÂU TRUY VẤN MẪU ĐÁNH GIÁ HIỆU NĂNG (DÙNG EXPLAIN / EXPLAIN ANALYZE)
-- =====================================================================

-- Truy vấn 1: Tìm các phiếu mượn quá hạn của một sinh viên (Sử dụng idx_phieumuon_mssv_hantra)
EXPLAIN ANALYZE
SELECT pm.ma_phieu, pm.ngay_muon, pm.han_tra
FROM phieu_muon pm
WHERE pm.mssv = 'SV0001' AND pm.han_tra < NOW();

-- Truy vấn 2: Lấy chi tiết sách trong phiếu mượn (Sử dụng idx_chitiet_phieu)
EXPLAIN ANALYZE
SELECT ct.ma_dau_sach, ct.so_luong
FROM chi_tiet_phieu_muon ct
WHERE ct.ma_phieu = 'PM0001';

-- Truy vấn 3: Thống kê top đầu sách được mượn nhiều nhất trong tháng (Sử dụng idx_chitiet_dausach, idx_phieumuon_ngaymuon)
EXPLAIN ANALYZE
SELECT ds.ma_dau_sach, ds.tac_sach, SUM(ct.so_luong) AS tong_luot_muon
FROM chi_tiet_phieu_muon ct
JOIN phieu_muon pm ON pm.ma_phieu = ct.ma_phieu
JOIN dau_sach ds   ON ds.ma_dau_sach = ct.ma_dau_sach
WHERE pm.ngay_muon BETWEEN '2026-09-01' AND '2026-09-30'
GROUP BY ds.ma_dau_sach, ds.tac_sach
ORDER BY tong_luot_muon DESC;

-- Truy vấn 4: Tra cứu sinh viên còn nợ tiền phạt (Sử dụng idx_sinhvien_sotienno)
EXPLAIN ANALYZE
SELECT mssv, ho_ten, so_tien_no
FROM sinh_vien
WHERE so_tien_no > 0;
