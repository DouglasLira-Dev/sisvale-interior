package dev.douglaslira.sisvaleinterior.presentation.view;

import dev.douglaslira.sisvaleinterior.application.dto.ResumoMensalDTO;
import dev.douglaslira.sisvaleinterior.application.dto.ServidorDTO;
import dev.douglaslira.sisvaleinterior.presentation.component.StatusCellRenderer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Tela de relatório mensal de um servidor.
 *
 * <p>View pura: expõe métodos para o controller popular combos, tabela e
 * painel de totais. Não conhece use cases nem repositórios.</p>
 */
public class TelaRelatorio extends JPanel {

    private static final int GAP = 8;
    private static final DateTimeFormatter FORMATO_MES =
            DateTimeFormatter.ofPattern("MMMM/yyyy", Locale.of("pt", "BR"));

    // Filtros
    private final JComboBox<ServidorDTO> comboServidor = new JComboBox<>();
    private final JComboBox<YearMonth> comboMes = new JComboBox<>();
    private final JButton botaoGerar = new JButton("Gerar");

    // Tabela
    private final DiaResumoTableModel modeloTabela = new DiaResumoTableModel();
    private final JTable tabela = new JTable(modeloTabela);

    // Totais
    private final JLabel labelTotal = new JLabel("Total a ressarcir: R$ 0,00");
    private final JLabel labelContadores = new JLabel("Válidos: 0 | Parciais: 0 | Inválidos: 0");

    public TelaRelatorio() {
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));

        add(montarFiltros(), BorderLayout.NORTH);
        add(montarTabela(), BorderLayout.CENTER);
        add(montarTotais(), BorderLayout.SOUTH);

        configurarRenderers();
        configurarEstiloTotais();
    }

    // Montagem
    private JPanel montarFiltros() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 0));

        painel.add(new JLabel("Servidor:"));
        painel.add(comboServidor);

        painel.add(new JLabel("Mês:"));
        painel.add(comboMes);

        painel.add(botaoGerar);

        return painel;
    }

    private JScrollPane montarTabela() {
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.setAutoCreateRowSorter(true);
        tabela.setRowHeight(24);

        tabela.getColumnModel()
                .getColumn(DiaResumoTableModel.COLUNA_STATUS)
                .setCellRenderer(new StatusCellRenderer());

        return new JScrollPane(tabela);
    }

    private JPanel montarTotais() {
        JPanel painel = new JPanel(new GridLayout(2, 1, 0, 4));
        painel.setBorder(BorderFactory.createEmptyBorder(GAP, 0, 0, 0));
        painel.add(labelTotal);
        painel.add(labelContadores);
        return painel;
    }

    private void configurarRenderers() {
        comboServidor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ServidorDTO s) {
                    setText(s.nome() + " (" + s.matricula() + ")");
                } else {
                    setText("—");
                }
                return this;
            }
        });

        comboMes.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof YearMonth ym) {
                    String texto = ym.format(FORMATO_MES);
                    setText(Character.toUpperCase(texto.charAt(0)) + texto.substring(1));
                } else {
                    setText("—");
                }
                return this;
            }
        });
    }

    private void configurarEstiloTotais() {
        labelTotal.setFont(labelTotal.getFont().deriveFont(Font.BOLD, 18f));
        labelTotal.setHorizontalAlignment(SwingConstants.LEFT);

        labelContadores.setFont(labelContadores.getFont().deriveFont(Font.PLAIN, 14f));
        labelContadores.setHorizontalAlignment(SwingConstants.LEFT);
    }

    // API para o controller
    public Long getServidorSelecionadoId() {
        ServidorDTO dto = (ServidorDTO) comboServidor.getSelectedItem();
        return dto == null ? null : dto.id();
    }

    public YearMonth getMesSelecionado() {
        return (YearMonth) comboMes.getSelectedItem();
    }

    public void setMesSelecionado(YearMonth mes) {
        comboMes.setSelectedItem(mes);
    }

    public void popularComboServidores(List<ServidorDTO> servidores) {
        comboServidor.removeAllItems();
        for (ServidorDTO s : servidores) {
            comboServidor.addItem(s);
        }
    }

    public void popularComboMeses(List<YearMonth> meses) {
        comboMes.removeAllItems();
        for (YearMonth m : meses) {
            comboMes.addItem(m);
        }
    }

    /**
     * Popula a tabela e o painel de totais com o resultado do mês.
     *
     * @param resumo resumo mensal; se {@code null}, limpa a tela
     */
    public void popularResumo(ResumoMensalDTO resumo) {
        if (resumo == null) {
            modeloTabela.limpar();
            labelTotal.setText("Total a ressarcir: R$ 0,00");
            labelContadores.setText("Válidos: 0 | Parciais: 0 | Inválidos: 0");
            return;
        }

        modeloTabela.atualizar(resumo.dias());
        labelTotal.setText("Total a ressarcir: R$ " + formatarValor(resumo.valorTotalMes()));
        labelContadores.setText(String.format(
                "Válidos: %d | Parciais: %d | Inválidos: %d",
                resumo.diasTotalmenteValidos(),
                resumo.diasParciais(),
                resumo.diasTotalmenteInvalidos()));
    }

    public void adicionarListenerGerar(ActionListener listener) {
        botaoGerar.addActionListener(listener);
    }

    public void adicionarListenerServidorMudou(ActionListener listener) {
        comboServidor.addActionListener(listener);
    }

    public void adicionarListenerMesMudou(ActionListener listener) {
        comboMes.addActionListener(listener);
    }

    public void mostrarMensagem(String titulo, String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // Helpers
    private static String formatarValor(BigDecimal valor) {
        if (valor == null) {
            return "0,00";
        }
        return valor.toPlainString().replace('.', ',');
    }
}