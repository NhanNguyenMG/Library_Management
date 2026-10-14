package ui.component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Editor xử lý sự kiện bấm Button bên trong ô của JTable (TableCellEditor).
 */
public class TableButtonEditor extends DefaultCellEditor {

    private final JButton button;
    private final String buttonText;
    private int currentRow;

    public TableButtonEditor(String text, Color bg, Color fg, Border border, Consumer<Integer> action) {
        super(new JCheckBox());
        this.buttonText = text;
        this.button = new JButton(text);
        this.button.setOpaque(true);
        this.button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (bg != null) this.button.setBackground(bg);
        if (fg != null) this.button.setForeground(fg);
        if (border != null) this.button.setBorder(border);

        this.button.addActionListener(e -> {
            int row = currentRow;
            fireEditingStopped();
            if (action != null) {
                SwingUtilities.invokeLater(() -> action.accept(row));
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.currentRow = row;
        this.button.setText(buttonText != null ? buttonText : (value != null ? value.toString() : ""));
        return this.button;
    }

    @Override
    public Object getCellEditorValue() {
        return buttonText;
    }
}
