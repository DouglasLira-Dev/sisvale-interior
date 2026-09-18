package dev.douglaslira.sisvaleinterior.presentation.view;

import dev.douglaslira.sisvaleinterior.application.dto.ResultadoTrechoDTO;
import dev.douglaslira.sisvaleinterior.presentation.component.StatusTrechoCellRenderer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.List;

/**
 * Diálogo modal que exibe os trechos de um lançamento em detalhe.
 *
 * <p>Reaproveitado pela tela de lançamentos e pela tela de relatório — em
 * ambos os casos, recebe a lista de {@link ResultadoTrechoDTO} e mostra
 * em uma tabela somente leitura com status colorido e motivo completo.</p>
 */
public class DialogDetalheTrechos extends JDialog {

    private static final int LARGURA = 760;
    private static final int ALTURA = 360;

    /**
     * Cria e exibe o diálogo.
     *
     * @param owner      janela dona (para centralizar); pode ser {@code null}
     * @param contexto   linha de contexto exibida no topo (ex.: "Data: ... — Total: ...")
     * @param resultados lista de resultados a exibir (pode ser vazia, não nula)
     */
    public DialogDetalheTrechos(Window owner,
                                String contexto,
                                List<ResultadoTrechoDTO> resultados) {
        super(owner, "Detalhes dos trechos", ModalityType.APPLICATION_MODAL);

        setLayout(new BorderLayout(8, 8));
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(montarCabecalho(contexto), BorderLayout.NORTH);
        add(montarTabela(resultados), BorderLayout.CENTER);
        add(montarRodape(), BorderLayout.SOUTH);

        setSize(LARGURA, ALTURA);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private JLabel montarCabecalho(String contexto) {
        JLabel label = new JLabel(contexto == null ? "" : contexto);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return label;
    }

    private JScrollPane montarTabela(List<ResultadoTrechoDTO> resultados) {
        TrechoDetalheTableModel modelo = new TrechoDetalheTableModel();
        modelo.atualizar(resultados);

        JTable tabela = new JTable(modelo);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.setRowHeight(24);
        tabela.setAutoCreateRowSorter(true);

        tabela.getColumnModel()
                .getColumn(TrechoDetalheTableModel.COLUNA_STATUS)
                .setCellRenderer(new StatusTrechoCellRenderer());

        // Larguras sugeridas
        tabela.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(300);

        return new JScrollPane(tabela);
    }

    private JPanel montarRodape() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));

        JButton botaoFechar = new JButton("Fechar");
        botaoFechar.addActionListener(e -> dispose());

        painel.add(botaoFechar);
        return painel;
    }
}