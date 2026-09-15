package dev.douglaslira.sisvaleinterior.presentation.view;

import dev.douglaslira.sisvaleinterior.application.dto.LancamentoComStatusDTO;
import dev.douglaslira.sisvaleinterior.application.dto.LancamentoDTO;
import dev.douglaslira.sisvaleinterior.application.dto.StatusDia;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de tabela para exibir lançamentos diários com status e total.
 *
 * <p>Cada linha é um {@link LancamentoComStatusDTO} — que combina o
 * lançamento com o {@link StatusDia} e o total calculado pelo domínio.</p>
 *
 * <p>A coluna "Status" exibe o {@link StatusDia} como valor da célula; a
 * tela aplica o {@code StatusCellRenderer} nessa coluna.</p>
 */
public class LancamentoTableModel extends AbstractTableModel {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private static final String[] COLUNAS = {
            "Data", "Descida", "Entrada", "Ida (R$)", "Saída", "Ônibus", "Volta (R$)", "Total", "Status"
    };

    /** Índice da coluna Status — usado pela tela para aplicar o renderer. */
    public static final int COLUNA_STATUS = 8;

    private List<LancamentoComStatusDTO> linhas = new ArrayList<>();

    @Override
    public int getRowCount() {
        return linhas.size();
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
        LancamentoComStatusDTO linha = linhas.get(rowIndex);
        LancamentoDTO dto = linha.lancamento();

        return switch (columnIndex) {
            case 0 -> dto.data() == null ? "" : dto.data().format(FORMATO_DATA);
            case 1 -> dto.horaDescida() == null ? "" : dto.horaDescida().format(FORMATO_HORA);
            case 2 -> dto.horaEntrada() == null ? "" : dto.horaEntrada().format(FORMATO_HORA);
            case 3 -> formatarValor(dto.valorIda());
            case 4 -> dto.horaSaida() == null ? "" : dto.horaSaida().format(FORMATO_HORA);
            case 5 -> dto.horaOnibus() == null ? "" : dto.horaOnibus().format(FORMATO_HORA);
            case 6 -> formatarValor(dto.valorVolta());
            case 7 -> formatarValor(linha.total());
            case 8 -> linha.status();
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
     * @param novasLinhas lista de DTOs com status (não pode ser nula)
     */
    public void atualizar(List<LancamentoComStatusDTO> novasLinhas) {
        this.linhas = new ArrayList<>(novasLinhas);
        fireTableDataChanged();
    }

    /**
     * @param rowIndex índice da linha
     * @return o lançamento da linha, ou {@code null} se o índice for inválido
     */
    public LancamentoDTO getLancamento(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= linhas.size()) {
            return null;
        }
        return linhas.get(rowIndex).lancamento();
    }

    // Helpers
    private static String formatarValor(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return valor.toPlainString().replace('.', ',');
    }
}