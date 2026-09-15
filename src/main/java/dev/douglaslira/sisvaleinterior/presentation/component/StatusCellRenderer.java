package dev.douglaslira.sisvaleinterior.presentation.component;

import dev.douglaslira.sisvaleinterior.application.dto.StatusDia;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

/**
 * Renderer de célula para exibir o status de validação do dia na {@link JTable}.
 *
 * <p>Cada status é representado por <strong>ícone + texto + cor de fundo</strong>,
 * garantindo acessibilidade (a informação não depende apenas da cor):</p>
 * <ul>
 *   <li>{@link StatusDia#VALIDO} — {@code ✅ Válido} sobre verde escuro</li>
 *   <li>{@link StatusDia#PARCIAL} — {@code ⚠️ Parcial} sobre mostarda escura</li>
 *   <li>{@link StatusDia#INVALIDO} — {@code ❌ Inválido} sobre vermelho escuro</li>
 * </ul>
 *
 * <p>Valores desconhecidos (não {@link StatusDia}) caem no comportamento padrão
 * do {@link DefaultTableCellRenderer}.</p>
 */
public class StatusCellRenderer extends DefaultTableCellRenderer {

    private static final Color FUNDO_VALIDO = new Color(0x1E, 0x4A, 0x1E);
    private static final Color FUNDO_PARCIAL = new Color(0x4D, 0x4D, 0x1B);
    private static final Color FUNDO_INVALIDO = new Color(0x4A, 0x1E, 0x1E);

    public StatusCellRenderer() {
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

        if (value instanceof StatusDia status) {
            aplicarStatus(table, status, isSelected);
        } else {
            aplicarPadrao(table, value, isSelected);
        }

        return c;
    }

    private void aplicarStatus(JTable table, StatusDia status, boolean isSelected) {
        setText(rotulo(status));
        setToolTipText(descricao(status));
        setBackground(isSelected ? table.getSelectionBackground() : fundo(status));
        setForeground(isSelected ? table.getSelectionForeground() : Color.WHITE);
    }

    private void aplicarPadrao(JTable table, Object value, boolean isSelected) {
        setText(value == null ? "" : value.toString());
        setToolTipText(null);
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
    }

    private static String rotulo(StatusDia status) {
        return switch (status) {
            case VALIDO   -> "✅ Válido";
            case PARCIAL  -> "⚠️ Parcial";
            case INVALIDO -> "❌ Inválido";
        };
    }

    private static String descricao(StatusDia status) {
        return switch (status) {
            case VALIDO   -> "Ida e volta válidas";
            case PARCIAL  -> "Apenas um dos pares (ida ou volta) é válido";
            case INVALIDO -> "Nenhum dos pares (ida e volta) é válido";
        };
    }

    private static Color fundo(StatusDia status) {
        return switch (status) {
            case VALIDO   -> FUNDO_VALIDO;
            case PARCIAL  -> FUNDO_PARCIAL;
            case INVALIDO -> FUNDO_INVALIDO;
        };
    }
}