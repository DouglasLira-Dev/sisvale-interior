package dev.douglaslira.sisvaleinterior;

import dev.douglaslira.sisvaleinterior.application.usecase.CadastrarServidorUseCase;
import dev.douglaslira.sisvaleinterior.application.usecase.ListarLancamentosUseCase;
import dev.douglaslira.sisvaleinterior.application.usecase.ListarServidoresUseCase;
import dev.douglaslira.sisvaleinterior.application.usecase.RegistrarLancamentoUseCase;
import dev.douglaslira.sisvaleinterior.application.usecase.ValidarMesUseCase;
import dev.douglaslira.sisvaleinterior.domain.repository.LancamentoRepository;
import dev.douglaslira.sisvaleinterior.domain.repository.ServidorRepository;
import dev.douglaslira.sisvaleinterior.domain.service.CalculadoraRessarcimento;
import dev.douglaslira.sisvaleinterior.domain.service.ValidadorHorario;
import dev.douglaslira.sisvaleinterior.infrastructure.config.AppConfig;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.ConnectionFactory;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.DatabaseInitializer;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.LancamentoRepositoryJdbc;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.ServidorRepositoryJdbc;
import dev.douglaslira.sisvaleinterior.presentation.controller.LancamentoController;
import dev.douglaslira.sisvaleinterior.presentation.controller.RelatorioController;
import dev.douglaslira.sisvaleinterior.presentation.controller.ServidorController;
import dev.douglaslira.sisvaleinterior.presentation.theme.DarkTheme;
import dev.douglaslira.sisvaleinterior.presentation.view.TelaCadastroServidor;
import dev.douglaslira.sisvaleinterior.presentation.view.TelaLancamento;
import dev.douglaslira.sisvaleinterior.presentation.view.TelaPrincipal;
import dev.douglaslira.sisvaleinterior.presentation.view.TelaRelatorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.Locale;

/**
 * Monta a aplicação na ordem correta e a inicia.
 *
 * <p>Sequência:</p>
 * <ol>
 *   <li>Aplica o {@link DarkTheme} (antes de qualquer Swing)</li>
 *   <li>Define o {@code Locale} pt-BR</li>
 *   <li>Abre a conexão e inicializa o banco</li>
 *   <li>Cria repositórios JDBC</li>
 *   <li>Cria serviços do domínio (validador, calculadora)</li>
 *   <li>Cria os use cases</li>
 *   <li>Cria as views, controllers e a janela principal — tudo na EDT</li>
 * </ol>
 *
 * <p>Falhas em qualquer etapa são logadas e apresentadas ao usuário antes
 * de encerrar o processo.</p>
 */
public class ApplicationBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ApplicationBootstrap.class);

    /**
     * Inicia a aplicação. Nunca lança — em caso de falha, loga, mostra
     * diálogo amigável e encerra com código 1.
     */
    public void iniciar() {
        try {
            prepararAmbiente();
            montarEIniciar();
        } catch (Throwable t) {
            log.error("Falha ao iniciar aplicação", t);
            exibirErroFatal(t);
            System.exit(1);
        }
    }

    // Ambiente — antes de qualquer Swing
    private void prepararAmbiente() {
        Locale.setDefault(Locale.of("pt", "BR"));
        DarkTheme.aplicar();
    }

    // Montagem
    private void montarEIniciar() {
        ConnectionFactory factory = new ConnectionFactory();
        new DatabaseInitializer(factory).inicializar();
        log.info("Banco inicializado.");

        ServidorRepository servidorRepo = new ServidorRepositoryJdbc(factory);
        LancamentoRepository lancamentoRepo = new LancamentoRepositoryJdbc(factory);

        ValidadorHorario validador = new ValidadorHorario(AppConfig.tolerancia());
        CalculadoraRessarcimento calculadora = new CalculadoraRessarcimento(validador);

        CadastrarServidorUseCase cadastrarServidor = new CadastrarServidorUseCase(servidorRepo);
        ListarServidoresUseCase listarServidores = new ListarServidoresUseCase(servidorRepo);
        RegistrarLancamentoUseCase registrarLancamento =
                new RegistrarLancamentoUseCase(lancamentoRepo, servidorRepo);
        ListarLancamentosUseCase listarLancamentos =
                new ListarLancamentosUseCase(lancamentoRepo, calculadora);
        ValidarMesUseCase validarMes =
                new ValidarMesUseCase(lancamentoRepo, servidorRepo, calculadora);

        SwingUtilities.invokeLater(() -> montarJanela(
                cadastrarServidor,
                listarServidores,
                registrarLancamento,
                listarLancamentos,
                validarMes
        ));
    }

    /**
     * Cria views, controllers e a janela principal — tudo na EDT.
     */
    private void montarJanela(CadastrarServidorUseCase cadastrarServidor,
                            ListarServidoresUseCase listarServidores,
                            RegistrarLancamentoUseCase registrarLancamento,
                            ListarLancamentosUseCase listarLancamentos,
                            ValidarMesUseCase validarMes) {

        TelaCadastroServidor telaServidores = new TelaCadastroServidor();
        TelaLancamento telaLancamentos = new TelaLancamento();
        TelaRelatorio telaRelatorio = new TelaRelatorio();

        // Controllers registram listeners nas views e ficam ativos enquanto
        // as views existirem. A referência é descartada de propósito.
        new ServidorController(telaServidores, cadastrarServidor, listarServidores);
        new LancamentoController(telaLancamentos, registrarLancamento,
                listarServidores, listarLancamentos);
        new RelatorioController(telaRelatorio, listarServidores, validarMes);

        TelaPrincipal janela = new TelaPrincipal(telaServidores, telaLancamentos, telaRelatorio);
        janela.setVisible(true);

        log.info("Aplicação iniciada.");
    }

    // Erro fatal
    private void exibirErroFatal(Throwable t) {
        String mensagem = "Falha ao iniciar o SisVale Interior.\n\n"
                + t.getClass().getSimpleName() + ": " + t.getMessage()
                + "\n\nConsulte o arquivo de log para mais detalhes.";

        try {
            // DarkTheme pode não ter sido aplicado ainda — JOptionPane funciona mesmo assim.
            JOptionPane.showMessageDialog(null, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Throwable ignore) {
            // Se nem o diálogo funcionar (ambiente headless), o log já registrou.
        }
    }
}