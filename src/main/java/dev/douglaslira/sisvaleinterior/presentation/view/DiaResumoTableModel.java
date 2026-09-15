package dev.douglaslira.sisvaleinterior.presentation.view;

import dev.douglaslira.sisvaleinterior.application.dto.ResumoMensalDTO;
import dev.douglaslira.sisvaleinterior.application.dto.StatusDia;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de tabela para o relatório mensal — exibe cada dia do mês com
 * status, valor e motivos de validação.
 *
 * <p>Consome diretamente o {@link ResumoMensalDTO.DiaResumoDTO} produzido
 * pelo domínio, sem recalcular nada.</p>
 */
public class DiaResumoTableModel extends AbstractTableModel {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] COLUNAS = {
            "Data", "Status", "Valor (R$)", "Motivo ida", "Motivo volta"
    };

    /** Índice da coluna Status — usado pela tela para aplicar o renderer. */
    public static final int COLUNA_STATUS = 1;

    private List<ResumoMensalDTO.DiaResumoDTO> dias = new ArrayList<>();

    @Override
    public int getRowCount() {
        return dias.size();
    }

    @Override
    public int getColumnCount() {
        return COLUNAS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUNAS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == COLUNA_STATUS) {
            return StatusDia.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ResumoMensalDTO.DiaResumoDTO dia = dias.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> dia.data() == null ? "" : dia.data().format(FORMATO_DATA);
            case 1 -> dia.status();
            case 2 -> formatarValor(dia.valor());
            case 3 -> formatarMotivo(dia.motivoIda(), dia.diferencaIdaMinutos());
            case 4 -> formatarMotivo(dia.motivoVolta(), dia.diferencaVoltaMinutos());
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * Substitui o conteúdo da tabela.
     *
     * @param novosDias lista de dias (não pode ser nula)
     */
    public void atualizar(List<ResumoMensalDTO.DiaResumoDTO> novosDias) {
        this.dias = new ArrayList<>(novosDias);
        fireTableDataChanged();
    }

    /**
     * Limpa a tabela.
     */
    public void limpar() {
        this.dias = new ArrayList<>();
        fireTableDataChanged();
    }

    // Helpers
    private static String formatarValor(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return valor.toPlainString().replace('.', ',');
    }

    /**
     * Formata o motivo da validação, evitando repetir "Dentro da tolerância"
     * quando o par está válido — nesse caso mostra só "—".
     */
    private static String formatarMotivo(String motivo, long diferencaMinutos) {
        if (motivo == null || motivo.isBlank()) {
            return "";
        }
        // Quando o par é válido, o motivo padrão é "Dentro da tolerância"
        if (motivo.toLowerCase().contains("tolerância")) {
            return "OK (" + diferencaMinutos + " min)";
        }
        return motivo;
    }
}