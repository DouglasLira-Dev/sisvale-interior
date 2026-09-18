package dev.douglaslira.sisvaleinterior.presentation.view;

import dev.douglaslira.sisvaleinterior.application.dto.ResultadoTrechoDTO;
import dev.douglaslira.sisvaleinterior.application.dto.ResumoMensalDTO;
import dev.douglaslira.sisvaleinterior.application.dto.StatusDia;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de tabela para o relatório mensal — exibe cada dia do mês com
 * status, valor e um resumo dos trechos.
 *
 * <p>Consome diretamente o {@link ResumoMensalDTO.DiaResumoDTO} produzido
 * pelo domínio, sem recalcular nada.</p>
 */
public class DiaResumoTableModel extends AbstractTableModel {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] COLUNAS = {
            "Data", "Status", "Valor (R$)", "Trechos"
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
            case 2 -> formatarValor(dia.valorTotalDia());
            case 3 -> resumirTrechos(dia.resultados());
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
     * Produz um resumo textual dos trechos — ex.: {@code "2/3 válidos"}.
     * Se houver apenas 1 trecho, mostra {@code "válido"} ou {@code "inválido"}.
     */
    private static String resumirTrechos(List<ResultadoTrechoDTO> resultados) {
        if (resultados == null || resultados.isEmpty()) {
            return "—";
        }
        long validos = resultados.stream().filter(ResultadoTrechoDTO::valido).count();
        if (resultados.size() == 1) {
            return validos == 1 ? "válido" : "inválido";
        }
        return validos + "/" + resultados.size() + " válidos";
    }
}