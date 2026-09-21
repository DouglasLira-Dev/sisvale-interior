package dev.douglaslira.sisvaleinterior.presentation.component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.awt.Component;

public class TimeFieldCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final TimeField timeField = new TimeField();

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                boolean isSelected, int row, int column) {
        // 1. Se value é LocalTime → setTime(value)
        // 2. Se value é String → tentar parsear e setTime
        // 3. Se value é null/vazio → setTime(null)
        return timeField;
    }

    @Override
    public Object getCellEditorValue() {
        return timeField.getTime();
    }
}