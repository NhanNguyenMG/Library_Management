package ui.component;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Renderer hiển thị Button bên trong ô của JTable (TableCellRenderer).
 */
public class TableButtonRenderer extends JButton implements TableCellRenderer {

    private final String buttonText;

    public TableButtonRenderer(String text, Color bg, Color fg, Border border) {
        this.buttonText = text;
        setOpaque(true);
        setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (bg != null) setBackground(bg);
        if (fg != null) setForeground(fg);
        if (border != null) setBorder(border);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        setText(buttonText != null ? buttonText : (value != null ? value.toString() : ""));
        return this;
    }
}
