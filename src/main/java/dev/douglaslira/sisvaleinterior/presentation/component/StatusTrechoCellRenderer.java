package dev.douglaslira.sisvaleinterior.presentation.component;

import dev.douglaslira.sisvaleinterior.application.dto.ResultadoTrechoDTO;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

/**
 * Renderer de célula para exibir o status de validação de um trecho
 * individual.
 *
 * <p>Válido → {@code ✅ Válido} sobre verde escuro. Inválido → {@code ❌ Inválido}
 * sobre vermelho escuro. Célula vazia (modo edição) → em branco.</p>
 */
public class StatusTrechoCellRenderer extends DefaultTableCellRenderer {

    private static final Color FUNDO_VALIDO = new Color(0x1E, 0x4A, 0x1E);
    private static final Color FUNDO_INVALIDO = new Color(0x4A, 0x1E, 0x1E);

    public StatusTrechoCellRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

        Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        if (value instanceof ResultadoTrechoDTO resultado) {
            aplicarResultado(table, resultado, isSelected);
        } else {
            aplicarVazio(table, isSelected);
        }

        return c;
    }

    private void aplicarResultado(JTable table, ResultadoTrechoDTO resultado, boolean isSelected) {
        if (resultado.valido()) {
            setText("✅ Válido");
            setToolTipText("Dentro da tolerância (" + resultado.diferencaMinutos() + " min)");
            setBackground(isSelected ? table.getSelectionBackground() : FUNDO_VALIDO);
        } else {
            setText("❌ Inválido");
            setToolTipText(resultado.motivo());
            setBackground(isSelected ? table.getSelectionBackground() : FUNDO_INVALIDO);
        }
        setForeground(isSelected ? table.getSelectionForeground() : Color.WHITE);
    }

    private void aplicarVazio(JTable table, boolean isSelected) {
        setText("");
        setToolTipText(null);
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
    }
}