package dev.douglaslira.sisvaleinterior.presentation.view;

import dev.douglaslira.sisvaleinterior.application.dto.ServidorDTO;
import dev.douglaslira.sisvaleinterior.presentation.component.ButtonFactory;
import dev.douglaslira.sisvaleinterior.presentation.component.CpfField;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Tela de cadastro e listagem de servidores.
 *
 * <p>É uma <strong>view pura</strong>: não conhece use cases nem repositórios.
 * Expõe métodos para o controller manipular e listeners para reagir a cliques.</p>
 */
public class TelaCadastroServidor extends JPanel {

    private static final int GAP = 8;

    private final JTextField campoNome = new JTextField(20);
    private final JTextField campoMatricula = new JTextField(15);
    private final CpfField campoCpf = new CpfField();

    private final ServidorTableModel modeloTabela = new ServidorTableModel();
    private final JTable tabela = new JTable(modeloTabela);

    private final JButton botaoSalvar = ButtonFactory.criarSalvar();
    private final JButton botaoCancelar = ButtonFactory.criarCancelar();
    private final JButton botaoExcluir = ButtonFactory.criarExcluir();

    public TelaCadastroServidor() {
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));

        add(montarFormulario(), BorderLayout.NORTH);
        add(montarTabela(), BorderLayout.CENTER);
        add(montarBotoes(), BorderLayout.SOUTH);
    }

    // Montagem
    private JPanel montarFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Dados do servidor"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Linha 1 — Nome
        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        painel.add(new JLabel("Nome:"), c);
        c.gridx = 1; c.weightx = 1;
        painel.add(campoNome, c);

        // Linha 2 — Matrícula e CPF
        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        painel.add(new JLabel("Matrícula:"), c);
        c.gridx = 1; c.weightx = 0.5;
        painel.add(campoMatricula, c);

        c.gridx = 2; c.weightx = 0;
        c.insets = new Insets(4, 20, 4, 4);
        painel.add(new JLabel("CPF:"), c);
        c.gridx = 3; c.weightx = 0.5;
        c.insets = new Insets(4, 4, 4, 4);
        painel.add(campoCpf, c);

        return painel;
    }

    private JScrollPane montarTabela() {
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.setAutoCreateRowSorter(true);
        tabela.setRowHeight(24);

        return new JScrollPane(tabela);
    }

    private JPanel montarBotoes() {
        JPanel painel = new JPanel();
        painel.add(botaoSalvar);
        painel.add(botaoExcluir);
        painel.add(botaoCancelar);
        return painel;
    }

    // API para o controller
    public String getNome() {
        return campoNome.getText().trim();
    }

    public void setNome(String nome) {
        campoNome.setText(nome == null ? "" : nome);
    }

    public String getMatricula() {
        return campoMatricula.getText().trim();
    }

    public void setMatricula(String matricula) {
        campoMatricula.setText(matricula == null ? "" : matricula);
    }

    public String getCpf() {
        return campoCpf.getCpf();
    }

    public void setCpf(String cpf) {
        campoCpf.setCpf(cpf);
    }

    public void limparFormulario() {
        campoNome.setText("");
        campoMatricula.setText("");
        campoCpf.setText("");
        campoNome.requestFocusInWindow();
    }

    public void popularTabela(List<ServidorDTO> servidores) {
        modeloTabela.atualizar(servidores);
    }

    public ServidorDTO getServidorSelecionado() {
        int linhaView = tabela.getSelectedRow();
        if (linhaView < 0) {
            return null;
        }
        int linhaModel = tabela.convertRowIndexToModel(linhaView);
        return modeloTabela.getServidor(linhaModel);
    }

    public void adicionarListenerSalvar(ActionListener listener) {
        botaoSalvar.addActionListener(listener);
    }

    public void adicionarListenerExcluir(ActionListener listener) {
        botaoExcluir.addActionListener(listener);
    }

    public void adicionarListenerCancelar(ActionListener listener) {
        botaoCancelar.addActionListener(listener);
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