package util;

import exception.ValidationException;

/**
 * Tiện ích kiểm tra hợp lệ dữ liệu đầu vào (Validation Utility).
 * Tương tự ValidationUtil trong kiến trúc của hệ thống phoneStore.
 */
public final class ValidationUtil {

    private ValidationUtil() {
        // Utility class không cho phép khởi tạo
    }

    /**
     * Kiểm tra chuỗi không được null hoặc rỗng.
     */
    public static void validateNotEmpty(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(errorMessage);
        }
    }

    /**
     * Kiểm tra số tiền phải lớn hơn 0.
     */
    public static void validatePositiveAmount(double amount, String errorMessage) {
        if (amount <= 0) {
            throw new ValidationException(errorMessage);
        }
    }

    /**
     * Kiểm tra số lượng phải là số nguyên dương.
     */
    public static void validatePositiveInteger(int value, String errorMessage) {
        if (value <= 0) {
            throw new ValidationException(errorMessage);
        }
    }

    /**
     * Kiểm tra mã sinh viên hợp lệ.
     */
    public static void validateStudentId(String studentId) {
        validateNotEmpty(studentId, "Mã thẻ sinh viên không được để trống.");
    }

    /**
     * Kiểm tra mã đầu sách hợp lệ.
     */
    public static void validateBookId(String bookId) {
        validateNotEmpty(bookId, "Mã đầu sách không được để trống.");
    }

    /**
     * Kiểm tra thông tin đăng nhập.
     */
    public static void validateCredentials(String username, String password) {
        validateNotEmpty(username, "Tên đăng nhập không được để trống.");
        validateNotEmpty(password, "Mật khẩu không được để trống.");
    }
}
