package dev.douglaslira.sisvaleinterior.presentation.controller;

import dev.douglaslira.sisvaleinterior.application.dto.ResumoMensalDTO;
import dev.douglaslira.sisvaleinterior.application.dto.ServidorDTO;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.application.usecase.ListarServidoresUseCase;
import dev.douglaslira.sisvaleinterior.application.usecase.ValidarMesUseCase;
import dev.douglaslira.sisvaleinterior.presentation.view.TelaRelatorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller da tela de relatório mensal.
 *
 * <p>Orquestra a {@link TelaRelatorio} com o {@link ValidarMesUseCase} e o
 * {@link ListarServidoresUseCase}.</p>
 *
 * <p>A geração do resumo é uma ação explícita (botão "Gerar"). Trocar o
 * servidor ou o mês limpa o resumo exibido — evita que o operador veja
 * dados de um servidor enquanto outro está selecionado.</p>
 */
public final class RelatorioController {

    private static final Logger log = LoggerFactory.getLogger(RelatorioController.class);

    private static final int MESES_ANTES = 6;
    private static final int MESES_DEPOIS = 6;

    private final TelaRelatorio view;
    private final ListarServidoresUseCase listarServidores;
    private final ValidarMesUseCase validarMes;

    /**
     * @param view             tela de relatório (não pode ser nula)
     * @param listarServidores caso de uso de listagem de servidores (não pode ser nulo)
     * @param validarMes       caso de uso de validação mensal (não pode ser nulo)
     * @throws IllegalArgumentException se algum argumento for nulo
     */
    public RelatorioController(TelaRelatorio view,
                            ListarServidoresUseCase listarServidores,
                            ValidarMesUseCase validarMes) {
        if (view == null) {
            throw new IllegalArgumentException("Tela é obrigatória");
        }
        if (listarServidores == null) {
            throw new IllegalArgumentException("ListarServidoresUseCase é obrigatório");
        }
        if (validarMes == null) {
            throw new IllegalArgumentException("ValidarMesUseCase é obrigatório");
        }

        this.view = view;
        this.listarServidores = listarServidores;
        this.validarMes = validarMes;

        // Listeners
        view.adicionarListenerGerar(e -> onGerar());
        view.adicionarListenerServidorMudou(e -> view.popularResumo(null));
        view.adicionarListenerMesMudou(e -> view.popularResumo(null));

        // Init
        carregarMeses();
        recarregarServidores();
    }

    // Ações
    private void onGerar() {
        Long servidorId = view.getServidorSelecionadoId();
        YearMonth mes = view.getMesSelecionado();

        if (servidorId == null) {
            view.mostrarErro("Selecione um servidor.");
            return;
        }
        if (mes == null) {
            view.mostrarErro("Selecione um mês.");
            return;
        }

        try {
            ResumoMensalDTO resumo = validarMes.executar(servidorId, mes);
            view.popularResumo(resumo);
        } catch (ApplicationException e) {
            view.mostrarErro(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao gerar relatório", e);
            view.mostrarErro("Erro inesperado ao gerar relatório. Consulte o log.");
        }
    }

    // Carregamentos
    private void carregarMeses() {
        YearMonth atual = YearMonth.now();
        List<YearMonth> meses = new ArrayList<>();
        for (int i = -MESES_ANTES; i <= MESES_DEPOIS; i++) {
            meses.add(atual.plusMonths(i));
        }
        view.popularComboMeses(meses);
        view.setMesSelecionado(atual);
    }

    public void recarregarServidores() {
        try {
            List<ServidorDTO> servidores = listarServidores.executar();
            view.popularComboServidores(servidores);
        } catch (ApplicationException e) {
            view.mostrarErro("Erro ao carregar servidores: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao carregar servidores", e);
            view.mostrarErro("Erro inesperado ao carregar servidores. Consulte o log.");
        }
    }
}