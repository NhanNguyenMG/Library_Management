package ui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Thành phần nút chọn danh mục trên thanh Sidebar (SidebarMenuItem).
 * Hỗ trợ hiệu ứng Hover và trạng thái Active.
 */
public class SidebarMenuItem extends JPanel {

    private static final Color COLOR_SIDEBAR = new Color(26, 37, 54);          // #1A2536
    private static final Color COLOR_SIDEBAR_HOVER = new Color(38, 52, 75);    // #26344B
    private static final Color COLOR_SIDEBAR_TEXT = new Color(203, 213, 225);   // #CBD5E1

    private boolean active = false;
    private final JLabel lblIcon;
    private final JLabel lblText;
    private final Runnable clickAction;

    public SidebarMenuItem(Icon icon, String text, Runnable action) {
        this.clickAction = action;
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        setBackground(COLOR_SIDEBAR);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblIcon = new JLabel(icon);
        lblText = new JLabel(text);
        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblText.setForeground(COLOR_SIDEBAR_TEXT);

        add(lblIcon);
        add(lblText);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!active) {
                    setBackground(COLOR_SIDEBAR_HOVER);
                    lblText.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!active) {
                    setBackground(COLOR_SIDEBAR);
                    lblText.setForeground(COLOR_SIDEBAR_TEXT);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (clickAction != null) {
                    clickAction.run();
                }
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            setBackground(COLOR_SIDEBAR_HOVER);
            lblText.setForeground(Color.WHITE);
            lblText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        } else {
            setBackground(COLOR_SIDEBAR);
            lblText.setForeground(COLOR_SIDEBAR_TEXT);
            lblText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }
        repaint();
    }
}
