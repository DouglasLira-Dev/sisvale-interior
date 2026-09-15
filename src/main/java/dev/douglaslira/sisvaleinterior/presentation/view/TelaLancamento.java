package dev.douglaslira.sisvaleinterior.presentation.view;

import dev.douglaslira.sisvaleinterior.application.dto.ServidorDTO;
import dev.douglaslira.sisvaleinterior.presentation.component.ButtonFactory;
import dev.douglaslira.sisvaleinterior.presentation.component.CurrencyField;
import dev.douglaslira.sisvaleinterior.presentation.component.DateField;
import dev.douglaslira.sisvaleinterior.presentation.component.StatusCellRenderer;
import dev.douglaslira.sisvaleinterior.presentation.component.TimeField;

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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Tela de lançamentos diários de servidores.
 *
 * <p>View pura: não conhece use cases nem repositórios. Expõe métodos para o
 * controller manipular e listeners para reagir a mudanças.</p>
 */
public class TelaLancamento extends JPanel {

    private static final int GAP = 8;
    private static final DateTimeFormatter FORMATO_MES =
            DateTimeFormatter.ofPattern("MMMM/yyyy", new Locale("pt", "BR"));

    // Filtros
    private final JComboBox<ServidorDTO> comboServidor = new JComboBox<>();
    private final JComboBox<YearMonth> comboMes = new JComboBox<>();

    // Formulário
    private final DateField campoData = new DateField();
    private final TimeField campoDescida = new TimeField();
    private final TimeField campoEntrada = new TimeField();
    private final CurrencyField campoValorIda = new CurrencyField();
    private final TimeField campoSaida = new TimeField();
    private final TimeField campoOnibus = new TimeField();
    private final CurrencyField campoValorVolta = new CurrencyField();

    private final JButton botaoSalvar = ButtonFactory.criarSalvar();
    private final JButton botaoCancelar = ButtonFactory.criarCancelar();

    // Tabela
    private final LancamentoTableModel modeloTabela = new LancamentoTableModel();
    private final JTable tabela = new JTable(modeloTabela);

    public TelaLancamento() {
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));

        add(montarFiltros(), BorderLayout.NORTH);
        add(montarCentro(), BorderLayout.CENTER);

        configurarRenderers();
    }

    // Montagem
    private JPanel montarFiltros() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 0));

        painel.add(new JLabel("Servidor:"));
        painel.add(comboServidor);

        painel.add(new JLabel("Mês:"));
        painel.add(comboMes);

        return painel;
    }

    private JPanel montarCentro() {
        JPanel painel = new JPanel(new BorderLayout(GAP, GAP));
        painel.add(montarFormulario(), BorderLayout.NORTH);
        painel.add(montarTabela(), BorderLayout.CENTER);
        return painel;
    }

    private JPanel montarFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Novo lançamento"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Linha 1 — Ida
        c.gridy = 0;
        c.gridx = 0; c.weightx = 0;
        painel.add(new JLabel("Data:"), c);
        c.gridx = 1; c.weightx = 0;
        painel.add(campoData, c);

        c.gridx = 2;
        painel.add(new JLabel("Descida:"), c);
        c.gridx = 3;
        painel.add(campoDescida, c);

        c.gridx = 4;
        painel.add(new JLabel("Entrada:"), c);
        c.gridx = 5;
        painel.add(campoEntrada, c);

        c.gridx = 6;
        painel.add(new JLabel("Valor ida:"), c);
        c.gridx = 7;
        painel.add(campoValorIda, c);

        // Linha 2 — Volta
        c.gridy = 1;
        c.gridx = 0;
        painel.add(new JLabel(""), c);   // alinhamento
        c.gridx = 1;
        painel.add(new JLabel(""), c);
        c.gridx = 2;
        painel.add(new JLabel("Saída:"), c);
        c.gridx = 3;
        painel.add(campoSaida, c);

        c.gridx = 4;
        painel.add(new JLabel("Ônibus:"), c);
        c.gridx = 5;
        painel.add(campoOnibus, c);

        c.gridx = 6;
        painel.add(new JLabel("Valor volta:"), c);
        c.gridx = 7;
        painel.add(campoValorVolta, c);

        // Linha 3 — botões
        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 8;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, GAP, 0));
        painelBotoes.add(botaoSalvar);
        painelBotoes.add(botaoCancelar);
        painel.add(painelBotoes, c);

        return painel;
    }

    private JScrollPane montarTabela() {
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.setAutoCreateRowSorter(true);
        tabela.setRowHeight(24);

        tabela.getColumnModel()
                .getColumn(LancamentoTableModel.COLUNA_STATUS)
                .setCellRenderer(new StatusCellRenderer());

        return new JScrollPane(tabela);
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

    public String getData() {
        return campoData.getText();
    }

    public java.time.LocalDate getDataComoLocalDate() {
        return campoData.getData();
    }

    public java.time.LocalTime getHoraDescida() {
        return campoDescida.getTime();
    }

    public java.time.LocalTime getHoraEntrada() {
        return campoEntrada.getTime();
    }

    public java.math.BigDecimal getValorIda() {
        return campoValorIda.getValor();
    }

    public java.time.LocalTime getHoraSaida() {
        return campoSaida.getTime();
    }

    public java.time.LocalTime getHoraOnibus() {
        return campoOnibus.getTime();
    }

    public java.math.BigDecimal getValorVolta() {
        return campoValorVolta.getValor();
    }

    public void limparFormulario() {
        campoData.setText("");
        campoDescida.setText("");
        campoEntrada.setText("");
        campoValorIda.setText("");
        campoSaida.setText("");
        campoOnibus.setText("");
        campoValorVolta.setText("");
        campoData.requestFocusInWindow();
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

    public void popularTabela(List<LancamentoTableModel.LinhaLancamento> linhas) {
        modeloTabela.atualizar(linhas);
    }

    public void adicionarListenerSalvar(ActionListener listener) {
        botaoSalvar.addActionListener(listener);
    }

    public void adicionarListenerCancelar(ActionListener listener) {
        botaoCancelar.addActionListener(listener);
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
}