package dev.douglaslira.sisvaleinterior.presentation.controller;

import dev.douglaslira.sisvaleinterior.application.dto.LancamentoComStatusDTO;
import dev.douglaslira.sisvaleinterior.application.dto.ServidorDTO;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.application.usecase.ListarLancamentosUseCase;
import dev.douglaslira.sisvaleinterior.application.usecase.ListarServidoresUseCase;
import dev.douglaslira.sisvaleinterior.application.usecase.RegistrarLancamentoUseCase;
import dev.douglaslira.sisvaleinterior.presentation.view.TelaLancamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller da tela de lançamentos.
 *
 * <p>Orquestra a {@link TelaLancamento} com os use cases de registro,
 * listagem de servidores e listagem de lançamentos.</p>
 *
 * <p>Segue o padrão MVP: a View não conhece use cases; o Controller não
 * conhece Swing. Comunicação sempre via métodos e listeners da View.</p>
 */
public final class LancamentoController {

    private static final Logger log = LoggerFactory.getLogger(LancamentoController.class);

    /** Quantidade de meses para trás e para frente no combo. */
    private static final int MESES_ANTES = 6;
    private static final int MESES_DEPOIS = 6;

    private final TelaLancamento view;
    private final RegistrarLancamentoUseCase registrar;
    private final ListarServidoresUseCase listarServidores;
    private final ListarLancamentosUseCase listarLancamentos;

    /**
     * Flag que evita disparar {@code carregarLancamentos()} enquanto os
     * combos estão sendo populados. Sem isso, cada {@code addItem} dispara
     * o listener e causa N consultas desnecessárias.
     */
    private boolean populando = false;

    /**
     * @param view             tela de lançamentos (não pode ser nula)
     * @param registrar        caso de uso de registro (não pode ser nulo)
     * @param listarServidores caso de uso de listagem de servidores (não pode ser nulo)
     * @param listarLancamentos caso de uso de listagem de lançamentos (não pode ser nulo)
     * @throws IllegalArgumentException se algum argumento for nulo
     */
    public LancamentoController(TelaLancamento view,
                                RegistrarLancamentoUseCase registrar,
                                ListarServidoresUseCase listarServidores,
                                ListarLancamentosUseCase listarLancamentos) {
        if (view == null) {
            throw new IllegalArgumentException("Tela é obrigatória");
        }
        if (registrar == null) {
            throw new IllegalArgumentException("RegistrarLancamentoUseCase é obrigatório");
        }
        if (listarServidores == null) {
            throw new IllegalArgumentException("ListarServidoresUseCase é obrigatório");
        }
        if (listarLancamentos == null) {
            throw new IllegalArgumentException("ListarLancamentosUseCase é obrigatório");
        }

        this.view = view;
        this.registrar = registrar;
        this.listarServidores = listarServidores;
        this.listarLancamentos = listarLancamentos;

        // Listeners registrados UMA vez — a flag `populando` protege contra reentrância.
        view.adicionarListenerSalvar(e -> onSalvar());
        view.adicionarListenerCancelar(e -> view.limparFormulario());
        view.adicionarListenerServidorMudou(e -> onServidorMudou());
        view.adicionarListenerMesMudou(e -> onMesMudou());

        carregarMeses();
        carregarServidores();

        // Depois de popular ambos os combos, carregar lançamentos do estado inicial.
        carregarLancamentos();
    }

    // =====================================================================
    // Ações
    // =====================================================================

    private void onServidorMudou() {
        if (populando) {
            return;
        }
        carregarLancamentos();
    }

    private void onMesMudou() {
        if (populando) {
            return;
        }
        carregarLancamentos();
    }

    private void onSalvar() {
        Long servidorId = view.getServidorSelecionadoId();
        LocalDate data = view.getDataComoLocalDate();
        LocalTime horaDescida = view.getHoraDescida();
        LocalTime horaEntrada = view.getHoraEntrada();
        LocalTime horaSaida = view.getHoraSaida();
        LocalTime horaOnibus = view.getHoraOnibus();

        try {
            registrar.executar(
                    servidorId,
                    data,
                    horaDescida,
                    horaEntrada,
                    view.getValorIda(),
                    horaSaida,
                    horaOnibus,
                    view.getValorVolta()
            );
            view.limparFormulario();
            carregarLancamentos();
            view.mostrarMensagem("Sucesso", "Lançamento registrado.");
        } catch (ApplicationException e) {
            view.mostrarErro(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao registrar lançamento", e);
            view.mostrarErro("Erro inesperado ao registrar lançamento. Consulte o log.");
        }
    }

    // =====================================================================
    // Carregamentos
    // =====================================================================

    private void carregarMeses() {
        populando = true;
        try {
            YearMonth atual = YearMonth.now();
            List<YearMonth> meses = new ArrayList<>();
            for (int i = -MESES_ANTES; i <= MESES_DEPOIS; i++) {
                meses.add(atual.plusMonths(i));
            }
            view.popularComboMeses(meses);
            view.setMesSelecionado(atual);
        } finally {
            populando = false;
        }
    }

    public void carregarServidores() {
        populando = true;
        try {
            List<ServidorDTO> servidores = listarServidores.executarApenasAtivos();
            view.popularComboServidores(servidores);
        } catch (ApplicationException e) {
            view.mostrarErro("Erro ao carregar servidores: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao carregar servidores", e);
            view.mostrarErro("Erro inesperado ao carregar servidores. Consulte o log.");
        } finally {
            populando = false;
        }
    }

    private void carregarLancamentos() {
        Long servidorId = view.getServidorSelecionadoId();
        YearMonth mes = view.getMesSelecionado();

        if (servidorId == null || mes == null) {
            view.popularTabela(List.of());
            return;
        }

        try {
            List<LancamentoComStatusDTO> lancamentos =
                    listarLancamentos.executar(servidorId, mes);
            view.popularTabela(lancamentos);
        } catch (ApplicationException e) {
            view.mostrarErro("Erro ao carregar lançamentos: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao carregar lançamentos", e);
            view.mostrarErro("Erro inesperado ao carregar lançamentos. Consulte o log.");
        }
    }
}