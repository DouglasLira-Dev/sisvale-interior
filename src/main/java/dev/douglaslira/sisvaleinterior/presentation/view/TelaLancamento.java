package dev.douglaslira.sisvaleinterior.presentation.view;

import dev.douglaslira.sisvaleinterior.application.dto.LancamentoComStatusDTO;
import dev.douglaslira.sisvaleinterior.application.dto.LancamentoDTO;
import dev.douglaslira.sisvaleinterior.application.dto.ResultadoTrechoDTO;
import dev.douglaslira.sisvaleinterior.application.dto.ServidorDTO;
import dev.douglaslira.sisvaleinterior.application.dto.TrechoDTO;
import dev.douglaslira.sisvaleinterior.presentation.component.ButtonFactory;
import dev.douglaslira.sisvaleinterior.presentation.component.DateField;
import dev.douglaslira.sisvaleinterior.presentation.component.StatusCellRenderer;
import dev.douglaslira.sisvaleinterior.presentation.component.StatusTrechoCellRenderer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Tela de lançamentos diários de servidores.
 *
 * <p>View pura: não conhece use cases nem repositórios. Expõe métodos para o
 * controller manipular e listeners para reagir a mudanças.</p>
 *
 * <p>Duas tabelas: uma <strong>editável</strong> para montar os trechos do
 * novo lançamento; outra <strong>somente leitura</strong> para listar os
 * lançamentos do mês selecionado.</p>
 */
public class TelaLancamento extends JPanel {

    private static final int GAP = 8;
    private static final DateTimeFormatter FORMATO_MES =
            DateTimeFormatter.ofPattern("MMMM/yyyy", Locale.of("pt", "BR"));

    // Filtros
    private final JComboBox<ServidorDTO> comboServidor = new JComboBox<>();
    private final JComboBox<YearMonth> comboMes = new JComboBox<>();

    // Formulário — data + tabela de trechos editável
    private final DateField campoData = new DateField();
    private final TrechoTableModel modeloTrechos = new TrechoTableModel();
    private final JTable tabelaTrechos = new JTable(modeloTrechos);

    private final JButton botaoAdicionarTrecho = ButtonFactory.criarAdicionarTrecho();
    private final JButton botaoRemoverTrecho = ButtonFactory.criarRemoverTrecho();
    private final JButton botaoSalvar = ButtonFactory.criarSalvar();
    private final JButton botaoCancelar = ButtonFactory.criarCancelar();

    private final JLabel labelTotalDia = new JLabel("Total do dia: R$ 0,00");

    // Tabela principal — lançamentos do mês
    private final LancamentoTableModel modeloLancamentos = new LancamentoTableModel();
    private final JTable tabelaLancamentos = new JTable(modeloLancamentos);

    public TelaLancamento() {
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));

        add(montarFiltros(), BorderLayout.NORTH);
        add(montarCentro(), BorderLayout.CENTER);

        configurarRenderers();
        configurarEstilo();
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

    private JSplitPane montarCentro() {
        JPanel painelFormulario = montarFormulario();
        JPanel painelListagem = montarListagem();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                painelFormulario, painelListagem);
        split.setResizeWeight(0.5);
        split.setBorder(null);
        return split;
    }

    private JPanel montarFormulario() {
        JPanel painel = new JPanel(new BorderLayout(GAP, GAP));
        painel.setBorder(BorderFactory.createTitledBorder("Novo lançamento"));

        // Data
        JPanel painelData = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 0));
        painelData.add(new JLabel("Data:"));
        painelData.add(campoData);
        painel.add(painelData, BorderLayout.NORTH);

        // Tabela de trechos
        tabelaTrechos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaTrechos.setFillsViewportHeight(true);
        tabelaTrechos.setRowHeight(24);
        tabelaTrechos.getColumnModel()
                .getColumn(TrechoTableModel.COLUNA_STATUS)
                .setCellRenderer(new StatusTrechoCellRenderer());

        painel.add(new JScrollPane(tabelaTrechos), BorderLayout.CENTER);

        // Rodapé
        painel.add(montarRodapeFormulario(), BorderLayout.SOUTH);
        return painel;
    }

    private JPanel montarRodapeFormulario() {
        JPanel painel = new JPanel(new BorderLayout(GAP, GAP));

        // Linha 1 — botões de trecho + total
        JPanel linha1 = new JPanel(new BorderLayout(GAP, 0));
        JPanel botoesTrecho = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 0));
        botoesTrecho.add(botaoAdicionarTrecho);
        botoesTrecho.add(botaoRemoverTrecho);
        linha1.add(botoesTrecho, BorderLayout.WEST);

        JPanel painelTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        painelTotal.add(labelTotalDia);
        linha1.add(painelTotal, BorderLayout.EAST);

        // Linha 2 — ações principais
        JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.CENTER, GAP, 0));
        linha2.add(botaoSalvar);
        linha2.add(botaoCancelar);

        painel.add(linha1, BorderLayout.NORTH);
        painel.add(linha2, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel montarListagem() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Lançamentos do mês"));

        tabelaLancamentos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaLancamentos.setFillsViewportHeight(true);
        tabelaLancamentos.setAutoCreateRowSorter(true);
        tabelaLancamentos.setRowHeight(24);
        tabelaLancamentos.getColumnModel()
                .getColumn(LancamentoTableModel.COLUNA_STATUS)
                .setCellRenderer(new StatusCellRenderer());

        painel.add(new JScrollPane(tabelaLancamentos), BorderLayout.CENTER);
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

    private void configurarEstilo() {
        labelTotalDia.setFont(labelTotalDia.getFont().deriveFont(Font.BOLD, 16f));
        labelTotalDia.setHorizontalAlignment(SwingConstants.RIGHT);
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

    public LocalDate getData() {
        return campoData.getData();
    }

    public void setData(LocalDate data) {
        campoData.setData(data);
    }

    public List<TrechoDTO> getTrechos() {
        return modeloTrechos.getTrechos();
    }

    public void adicionarTrecho() {
        modeloTrechos.adicionarLinhaVazia();
    }

    public void removerTrechoSelecionado() {
        int linhaView = tabelaTrechos.getSelectedRow();
        if (linhaView < 0) {
            return;
        }
        int linhaModel = tabelaTrechos.convertRowIndexToModel(linhaView);
        modeloTrechos.removerLinha(linhaModel);
    }

    public void atualizarStatusTrecho(int rowIndex, ResultadoTrechoDTO resultado) {
        modeloTrechos.atualizarResultado(rowIndex, resultado);
    }

    public void atualizarTotalDia(BigDecimal valor) {
        String texto = valor == null
                ? "0,00"
                : valor.toPlainString().replace('.', ',');
        labelTotalDia.setText("Total do dia: R$ " + texto);
    }

    public TrechoTableModel getModeloTrechos() {
        return modeloTrechos;
    }

    public void limparFormulario() {
        campoData.setText("");
        modeloTrechos.limpar();
        atualizarTotalDia(BigDecimal.ZERO);
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

    public void popularTabelaLancamentos(List<LancamentoComStatusDTO> linhas) {
        modeloLancamentos.atualizar(linhas);
    }

    public LancamentoDTO getLancamentoSelecionado() {
        int linhaView = tabelaLancamentos.getSelectedRow();
        if (linhaView < 0) {
            return null;
        }
        int linhaModel = tabelaLancamentos.convertRowIndexToModel(linhaView);
        return modeloLancamentos.getLancamento(linhaModel);
    }

    // Listeners
    public void adicionarListenerSalvar(ActionListener listener) {
        botaoSalvar.addActionListener(listener);
    }

    public void adicionarListenerCancelar(ActionListener listener) {
        botaoCancelar.addActionListener(listener);
    }

    public void adicionarListenerAdicionarTrecho(ActionListener listener) {
        botaoAdicionarTrecho.addActionListener(listener);
    }

    public void adicionarListenerRemoverTrecho(ActionListener listener) {
        botaoRemoverTrecho.addActionListener(listener);
    }

    public void adicionarListenerServidorMudou(ActionListener listener) {
        comboServidor.addActionListener(listener);
    }

    public void adicionarListenerMesMudou(ActionListener listener) {
        comboMes.addActionListener(listener);
    }

    // Diálogos
    public void mostrarMensagem(String titulo, String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public boolean confirmar(String mensagem) {
        int resposta = JOptionPane.showConfirmDialog(
                this, mensagem, "Confirmação",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return resposta == JOptionPane.YES_OPTION;
    }
}