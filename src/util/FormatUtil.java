package util;

import java.util.Locale;

/**
 * Tiện ích định dạng tiền tệ và hiển thị cho toàn bộ hệ thống.
 */
public final class FormatUtil {

    private static final Locale LOCALE_VN = Locale.forLanguageTag("vi-VN");

    private FormatUtil() {
        // Utility class
    }

    /**
     * Định dạng tiền tệ theo chuẩn Việt Nam (ví dụ: "15,000 đ").
     */
    public static String formatMoney(double amount) {
        return String.format(LOCALE_VN, "%,.0f đ", amount);
    }

    /**
     * Định dạng tiền tệ kèm đơn vị VNĐ (ví dụ: "15,000 VNĐ").
     */
    public static String formatMoneyVND(double amount) {
        return String.format(LOCALE_VN, "%,.0f VNĐ", amount);
    }
}
