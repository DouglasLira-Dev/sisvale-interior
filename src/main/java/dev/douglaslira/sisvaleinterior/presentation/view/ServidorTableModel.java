package dev.douglaslira.sisvaleinterior.presentation.view;

import dev.douglaslira.sisvaleinterior.application.dto.ServidorDTO;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de tabela para exibir {@link ServidorDTO} na tela de cadastro.
 *
 * <p>Não editável. As colunas são fixas: ID, Nome, Matrícula, CPF, Ativo.</p>
 */
public class ServidorTableModel extends AbstractTableModel {

    private static final String[] COLUNAS = {"ID", "Nome", "Matrícula", "CPF", "Ativo"};

    private List<ServidorDTO> servidores = new ArrayList<>();

    @Override
    public int getRowCount() {
        return servidores.size();
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        ServidorDTO servidor = servidores.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> servidor.id();
            case 1 -> servidor.nome();
            case 2 -> servidor.matricula();
            case 3 -> servidor.cpf();
            case 4 -> servidor.ativo() ? "Sim" : "Não";
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * Substitui o conteúdo da tabela pela lista informada.
     *
     * @param novosServidores lista de DTOs (não pode ser nulo)
     */
    public void atualizar(List<ServidorDTO> novosServidores) {
        this.servidores = new ArrayList<>(novosServidores);
        fireTableDataChanged();
    }

    /**
     * @param rowIndex índice da linha
     * @return o DTO da linha, ou {@code null} se o índice for inválido
     */
    public ServidorDTO getServidor(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= servidores.size()) {
            return null;
        }
        return servidores.get(rowIndex);
    }
}