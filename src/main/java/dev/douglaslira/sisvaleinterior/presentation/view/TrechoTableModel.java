package dev.douglaslira.sisvaleinterior.presentation.view;

import dev.douglaslira.sisvaleinterior.application.dto.ResultadoTrechoDTO;
import dev.douglaslira.sisvaleinterior.application.dto.TrechoDTO;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de tabela para os trechos de um lançamento.
 *
 * <p>Serve tanto para <strong>edição</strong> (montagem do lançamento antes
 * de salvar) quanto para <strong>leitura</strong> (exibição de lançamentos
 * já calculados). A coluna Status fica vazia durante a edição e é preenchida
 * quando a linha recebe um {@link ResultadoTrechoDTO}.</p>
 *
 * <p>Cada linha é um {@link LinhaTrecho} — combina o DTO do trecho com um
 * resultado de validação opcional.</p>
 */
public class TrechoTableModel extends AbstractTableModel {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private static final String[] COLUNAS = {"#", "Referência", "Comparada", "Valor (R$)", "Status"};

    /** Índice da coluna Status. */
    public static final int COLUNA_STATUS = 4;

    private final List<LinhaTrecho> linhas = new ArrayList<>();

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
            case 0 -> Integer.class;
            case 3 -> BigDecimal.class;
            case 4 -> ResultadoTrechoDTO.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Só edita se ainda não foi validado (modo edição)
        boolean editavel = linhas.get(rowIndex).resultado() == null;
        return editavel && (columnIndex == 1 || columnIndex == 2 || columnIndex == 3);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LinhaTrecho linha = linhas.get(rowIndex);
        TrechoDTO trecho = linha.trecho();

        return switch (columnIndex) {
            case 0 -> rowIndex + 1;
            case 1 -> trecho.horaReferencia() == null ? "" : trecho.horaReferencia().format(FORMATO_HORA);
            case 2 -> trecho.horaComparada() == null ? "" : trecho.horaComparada().format(FORMATO_HORA);
            case 3 -> trecho.valor();
            case 4 -> linha.resultado();   // pode ser null em modo edição
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        LinhaTrecho atual = linhas.get(rowIndex);
        TrechoDTO trecho = atual.trecho();

        TrechoDTO novo = switch (columnIndex) {
            case 1 -> new TrechoDTO(parseHora(value), trecho.horaComparada(), trecho.valor());
            case 2 -> new TrechoDTO(trecho.horaReferencia(), parseHora(value), trecho.valor());
            case 3 -> new TrechoDTO(trecho.horaReferencia(), trecho.horaComparada(), parseValor(value));
            default -> trecho;
        };

        linhas.set(rowIndex, new LinhaTrecho(novo, atual.resultado()));
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    // API
    /**
     * Adiciona um trecho vazio (linha em branco para o usuário preencher).
     */
    public void adicionarLinhaVazia() {
        linhas.add(new LinhaTrecho(new TrechoDTO(null, null, null), null));
        int index = linhas.size() - 1;
        fireTableRowsInserted(index, index);
    }

    /**
     * Remove a linha do índice informado.
     *
     * @param rowIndex índice da linha
     */
    public void removerLinha(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= linhas.size()) {
            return;
        }
        linhas.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    /**
     * Substitui o conteúdo inteiro (usado para leitura).
     *
     * @param novosResultados lista de resultados de trecho
     */
    public void atualizar(List<ResultadoTrechoDTO> novosResultados) {
        linhas.clear();
        for (ResultadoTrechoDTO resultado : novosResultados) {
            linhas.add(new LinhaTrecho(resultado.trecho(), resultado));
        }
        fireTableDataChanged();
    }

        /**
     * Atualiza o resultado de validação de uma linha específica.
     *
     * <p>Usado pelo controller após validar um trecho individualmente — sem
     * substituir a tabela inteira. Dispara {@code fireTableRowsUpdated} para
     * a linha afetada.</p>
     *
     * @param rowIndex  índice da linha
     * @param resultado resultado da validação; {@code null} volta ao modo edição
     */
    public void atualizarResultado(int rowIndex, ResultadoTrechoDTO resultado) {
        if (rowIndex < 0 || rowIndex >= linhas.size()) {
            return;
        }
        LinhaTrecho atual = linhas.get(rowIndex);
        linhas.set(rowIndex, new LinhaTrecho(atual.trecho(), resultado));
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    /**
     * Limpa a tabela.
     */
    public void limpar() {
        linhas.clear();
        fireTableDataChanged();
    }

    /**
     * @return os trechos atualmente exibidos, na ordem da tabela
     */
    public List<TrechoDTO> getTrechos() {
        return linhas.stream()
                .map(LinhaTrecho::trecho)
                .toList();
    }

    /**
     * @return os resultados de validação (null se a linha ainda não foi validada)
     */
    public List<ResultadoTrechoDTO> getResultados() {
        return linhas.stream()
                .map(LinhaTrecho::resultado)
                .toList();
    }

    // Helpers
    private static LocalTime parseHora(Object value) {
        if (value instanceof LocalTime lt){
            return lt;
        }
        if (value == null) {
            return null;
        }
        String texto = value.toString().trim().replace(":", "");
        if (texto.isEmpty()) {
            return null;
        }
        // aceita só dígitos
        if(!texto.matches("\\d+")){
            return null;
        }
        int hora;
        int minuto;
        try {
            switch (texto.length()){
                case 4 -> {
                    hora = Integer.parseInt(texto.substring(0, 2));
                    minuto = Integer.parseInt(texto.substring(2,4));
                }
                case 3 -> {
                    hora = Integer.parseInt(texto.substring(0, 1));
                    minuto = Integer.parseInt(texto.substring(1,3));
                }
                case 2 -> {
                    hora = Integer.parseInt(texto);
                    minuto = 0;
                }
                default -> {
                    return null;
                }
            }
            return LocalTime.of(hora, minuto);
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal parseValor(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        String texto = value.toString().trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Linha
    /**
     * Linha da tabela: trecho + resultado de validação (opcional).
     *
     * @param trecho    dados do trecho
     * @param resultado resultado da validação; {@code null} em modo edição
     */
    public record LinhaTrecho(TrechoDTO trecho, ResultadoTrechoDTO resultado) {
    }
}