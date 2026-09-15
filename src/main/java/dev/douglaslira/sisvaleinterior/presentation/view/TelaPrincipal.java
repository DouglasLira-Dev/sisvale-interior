package dev.douglaslira.sisvaleinterior.presentation.view;

import dev.douglaslira.sisvaleinterior.infrastructure.config.AppConfig;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;
import java.awt.Dimension;

/**
 * Janela principal do SisVale Interior.
 *
 * <p>Recebe as três telas já construídas (com seus controllers conectados) e
 * as organiza em um {@link JTabbedPane}.</p>
 *
 * <p>O título é lido do {@code application.properties} via
 * {@link AppConfig#uiTitulo()} — fonte única de verdade.</p>
 *
 * <p>{@code setVisible(true)} é responsabilidade de quem instancia (o
 * {@code Main}), não desta classe — facilita testes e mantém o padrão MVP
 * adotado no projeto.</p>
 */
public class TelaPrincipal extends JFrame {

    private static final String TITULO_ABA_SERVIDORES = "Servidores";
    private static final String TITULO_ABA_LANCAMENTOS = "Lançamentos";
    private static final String TITULO_ABA_RELATORIO = "Relatório";

    private static final int LARGURA = 1200;
    private static final int ALTURA = 800;
    private static final int LARGURA_MINIMA = 800;
    private static final int ALTURA_MINIMA = 600;

    /**
     * Cria a janela principal com as três telas.
     *
     * @param telaServidores  tela de cadastro de servidores (não pode ser nula)
     * @param telaLancamentos tela de lançamentos (não pode ser nula)
     * @param telaRelatorio   tela de relatório (não pode ser nula)
     * @throws IllegalArgumentException se alguma tela for nula
     */
    public TelaPrincipal(TelaCadastroServidor telaServidores,
                        TelaLancamento telaLancamentos,
                        TelaRelatorio telaRelatorio) {
        if (telaServidores == null) {
            throw new IllegalArgumentException("Tela de servidores é obrigatória");
        }
        if (telaLancamentos == null) {
            throw new IllegalArgumentException("Tela de lançamentos é obrigatória");
        }
        if (telaRelatorio == null) {
            throw new IllegalArgumentException("Tela de relatório é obrigatória");
        }

        setTitle(AppConfig.uiTitulo());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(LARGURA, ALTURA);
        setMinimumSize(new Dimension(LARGURA_MINIMA, ALTURA_MINIMA));
        setLocationRelativeTo(null);

        abas.addTab(TITULO_ABA_SERVIDORES, telaServidores);
        abas.addTab(TITULO_ABA_LANCAMENTOS, telaLancamentos);
        abas.addTab(TITULO_ABA_RELATORIO, telaRelatorio);

        setContentPane(abas);
    }

    private final JTabbedPane abas = new JTabbedPane();

    public void adicionarListenerMudancaAba(ChangeListener listener) {
    abas.addChangeListener(listener);
}
}