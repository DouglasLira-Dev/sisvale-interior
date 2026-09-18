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
 * Modelo de tabela para exibir os lançamentos do mês — <strong>uma linha
 * por lançamento</strong> (resumo por dia).
 *
 * <p>Cada linha é um {@link LancamentoComStatusDTO}, que combina o
 * lançamento com o status agregado do dia e o valor total calculado pelo
 * domínio.</p>
 *
 * <p>Para ver os trechos individuais, consulte a tela de Relatório —
 * aqui o foco é o panorama do mês.</p>
 */
public class LancamentoTableModel extends AbstractTableModel {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] COLUNAS = {
            "Data", "Qtd. trechos", "Total (R$)", "Status"
    };

    /** Índice da coluna Status — usado pela tela para aplicar o renderer. */
    public static final int COLUNA_STATUS = 3;

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
        return switch (columnIndex) {
            case 0 -> String.class;
            case 1 -> Integer.class;
            case 2 -> String.class;
            case 3 -> StatusDia.class;
            default -> Object.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LancamentoComStatusDTO linha = linhas.get(rowIndex);
        LancamentoDTO dto = linha.lancamento();

        return switch (columnIndex) {
            case 0 -> dto.data() == null ? "" : dto.data().format(FORMATO_DATA);
            case 1 -> dto.trechos() == null ? 0 : dto.trechos().size();
            case 2 -> formatarValor(linha.valorTotalDia());
            case 3 -> linha.status();
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

    /**
     * @param rowIndex índice da linha
     * @return o {@link LancamentoComStatusDTO} da linha (com status e
     *         lista de trechos), ou {@code null} se o índice for inválido
     */
    public LancamentoComStatusDTO getLancamentoComStatus(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= linhas.size()) {
            return null;
        }
        return linhas.get(rowIndex);
    }


    // Helpers
    private static String formatarValor(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return valor.toPlainString().replace('.', ',');
    }
}