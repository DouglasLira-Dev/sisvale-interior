package dev.douglaslira.sisvaleinterior.presentation.view;

import dev.douglaslira.sisvaleinterior.application.dto.ResultadoTrechoDTO;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de tabela <strong>somente leitura</strong> para exibir os trechos
 * de um lançamento (ou de um dia do relatório) em detalhe.
 *
 * <p>Consome diretamente {@link ResultadoTrechoDTO} — cada linha mostra
 * o par de horários, o valor, o status (via {@code StatusTrechoCellRenderer})
 * e o motivo da validação.</p>
 *
 * <p>É o modelo usado pelo {@code DialogDetalheTrechos}, reaproveitado
 * tanto pela tela de lançamentos quanto pela tela de relatório.</p>
 */
public class TrechoDetalheTableModel extends AbstractTableModel {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private static final String[] COLUNAS = {
            "#", "Referência", "Comparada", "Valor (R$)", "Status", "Motivo"
    };

    /** Índice da coluna Status — usado pela view para aplicar o renderer. */
    public static final int COLUNA_STATUS = 4;

    private List<ResultadoTrechoDTO> resultados = new ArrayList<>();

    @Override
    public int getRowCount() {
        return resultados.size();
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
            case 0 -> Integer.class;
            case COLUNA_STATUS -> ResultadoTrechoDTO.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ResultadoTrechoDTO resultado = resultados.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> rowIndex + 1;
            case 1 -> formatarHora(resultado.trecho().horaReferencia());
            case 2 -> formatarHora(resultado.trecho().horaComparada());
            case 3 -> formatarValor(resultado.trecho().valor());
            case COLUNA_STATUS -> resultado;
            case 5 -> resultado.motivo();
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
     * @param novosResultados lista de resultados (pode ser vazia, não nula)
     */
    public void atualizar(List<ResultadoTrechoDTO> novosResultados) {
        this.resultados = novosResultados == null
                ? new ArrayList<>()
                : new ArrayList<>(novosResultados);
        fireTableDataChanged();
    }

    // Helpers
    private static String formatarHora(java.time.LocalTime hora) {
        if (hora == null) {
            return "";
        }
        return hora.format(FORMATO_HORA);
    }

    private static String formatarValor(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return valor.toPlainString().replace('.', ',');
    }
}