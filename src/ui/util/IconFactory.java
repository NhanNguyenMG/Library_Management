package ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Nhà máy khởi tạo Vector Icon 2D bằng Graphics2D (IconFactory).
 * Đảm bảo hiển thị sắc nét trên mọi hệ điều hành mà không phụ thuộc font/ảnh ngoài.
 */
public final class IconFactory {

    private IconFactory() {
        // Utility class
    }

    public static Icon createSearchIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(size >= 20 ? 2.3f : 1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int d = (int) (size * 0.55);
                int ox = x + (int) (size * 0.12);
                int oy = y + (int) (size * 0.12);
                g2.drawOval(ox, oy, d, d);

                int hx1 = ox + (int) (d * 0.82);
                int hy1 = oy + (int) (d * 0.82);
                int hx2 = x + size - 3;
                int hy2 = y + size - 3;
                g2.drawLine(hx1, hy1, hx2, hy2);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }

    public static Icon createPlusIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(size >= 20 ? 2.6f : 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int midX = x + size / 2;
                int midY = y + size / 2;
                int pad = (int) (size * 0.22);

                g2.drawLine(x + pad, midY, x + size - pad, midY);
                g2.drawLine(midX, y + pad, midX, y + size - pad);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }

    public static Icon createReturnIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(size >= 20 ? 2.2f : 1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int left = x + (int) (size * 0.18);
                int right = x + (int) (size * 0.78);
                int top = y + (int) (size * 0.28);
                int bottom = y + (int) (size * 0.74);

                Path2D path = new Path2D.Float();
                path.moveTo(right, bottom);
                path.lineTo(right, top + (bottom - top) / 2);
                path.quadTo(right, top, right - (right - left) / 2, top);
                path.lineTo(left + 2, top);
                g2.draw(path);

                g2.drawLine(left, top, left + (int) (size * 0.24), top - (int) (size * 0.20));
                g2.drawLine(left, top, left + (int) (size * 0.24), top + (int) (size * 0.20));

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }

    public static Icon createDotIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                int d = (int) (size * 0.55);
                g2.fillOval(x + (size - d) / 2, y + (size - d) / 2, d, d);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }

    public static Icon createPaymentIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(size >= 20 ? 2.0f : 1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int w = (int) (size * 0.82);
                int h = (int) (size * 0.60);
                int startX = x + (size - w) / 2;
                int startY = y + (size - h) / 2;

                g2.drawRoundRect(startX, startY, w, h, 3, 3);
                g2.drawLine(startX, startY + (int) (h * 0.38), startX + w, startY + (int) (h * 0.38));
                g2.fillOval(startX + (int) (w * 0.18), startY + (int) (h * 0.62), (int)(size * 0.15), (int)(size * 0.15));
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }

    public static Icon createLogoutIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int midX = x + size / 2;
                int midY = y + size / 2;
                int r = (int) (size * 0.35);

                g2.drawArc(midX - r, midY - r, r * 2, r * 2, -55, 290);
                g2.drawLine(midX, midY - r - 2, midX, midY - 1);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }
}
