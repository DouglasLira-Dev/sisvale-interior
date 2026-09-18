package dev.douglaslira.sisvaleinterior.presentation.controller;

import dev.douglaslira.sisvaleinterior.application.dto.LancamentoComStatusDTO;
import dev.douglaslira.sisvaleinterior.application.dto.LancamentoDTO;
import dev.douglaslira.sisvaleinterior.application.dto.ResultadoTrechoDTO;
import dev.douglaslira.sisvaleinterior.application.dto.ServidorDTO;
import dev.douglaslira.sisvaleinterior.application.dto.TrechoDTO;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.application.usecase.ListarLancamentosUseCase;
import dev.douglaslira.sisvaleinterior.application.usecase.ListarServidoresUseCase;
import dev.douglaslira.sisvaleinterior.application.usecase.RegistrarLancamentoUseCase;
import dev.douglaslira.sisvaleinterior.application.usecase.RemoverLancamentoUseCase;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoTrecho;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoValidacao;
import dev.douglaslira.sisvaleinterior.domain.model.Trecho;
import dev.douglaslira.sisvaleinterior.domain.service.ValidadorHorario;
import dev.douglaslira.sisvaleinterior.presentation.view.TelaLancamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.TableModelEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller da tela de lançamentos.
 *
 * <p>Orquestra a {@link TelaLancamento} com os use cases de registro,
 * listagem e remoção de lançamentos.</p>
 *
 * <p><strong>Validação em tempo real:</strong> ao editar/adicionar/remover
 * um trecho na tabela, o controller valida o par de horários com o
 * {@link ValidadorHorario} e atualiza o status da célula. O total do dia
 * é recalculado a cada mudança — somando apenas os trechos válidos.</p>
 */
public final class LancamentoController {

    private static final Logger log = LoggerFactory.getLogger(LancamentoController.class);

    private static final int MESES_ANTES = 6;
    private static final int MESES_DEPOIS = 6;

    private final TelaLancamento view;
    private final RegistrarLancamentoUseCase registrar;
    private final RemoverLancamentoUseCase remover;
    private final ListarServidoresUseCase listarServidores;
    private final ListarLancamentosUseCase listarLancamentos;
    private final ValidadorHorario validador;

    /**
     * Flag que evita disparar {@code carregarLancamentos()} enquanto os
     * combos estão sendo populados.
     */
    private boolean populando = false;

    /**
     * Flag que evita loop infinito: quando o controller atualiza o status
     * de uma linha, o {@code TrechoTableModel} dispara um evento; sem esta
     * flag, o listener reprocessaria o evento em loop.
     */
    private boolean atualizandoStatus = false;

    /**
     * @param view              tela de lançamentos (não pode ser nula)
     * @param registrar         caso de uso de registro (não pode ser nulo)
     * @param remover           caso de uso de remoção (não pode ser nulo)
     * @param listarServidores  caso de uso de listagem de servidores (não pode ser nulo)
     * @param listarLancamentos caso de uso de listagem de lançamentos (não pode ser nulo)
     * @param validador         validador de horário (não pode ser nulo)
     * @throws IllegalArgumentException se algum argumento for nulo
     */
    public LancamentoController(TelaLancamento view,
                                RegistrarLancamentoUseCase registrar,
                                RemoverLancamentoUseCase remover,
                                ListarServidoresUseCase listarServidores,
                                ListarLancamentosUseCase listarLancamentos,
                                ValidadorHorario validador) {
        if (view == null) {
            throw new IllegalArgumentException("Tela é obrigatória");
        }
        if (registrar == null) {
            throw new IllegalArgumentException("RegistrarLancamentoUseCase é obrigatório");
        }
        if (remover == null) {
            throw new IllegalArgumentException("RemoverLancamentoUseCase é obrigatório");
        }
        if (listarServidores == null) {
            throw new IllegalArgumentException("ListarServidoresUseCase é obrigatório");
        }
        if (listarLancamentos == null) {
            throw new IllegalArgumentException("ListarLancamentosUseCase é obrigatório");
        }
        if (validador == null) {
            throw new IllegalArgumentException("ValidadorHorario é obrigatório");
        }

        this.view = view;
        this.registrar = registrar;
        this.remover = remover;
        this.listarServidores = listarServidores;
        this.listarLancamentos = listarLancamentos;
        this.validador = validador;

        // Listeners de ação
        view.adicionarListenerSalvar(e -> onSalvar());
        view.adicionarListenerExcluir(e -> onExcluir());
        view.adicionarListenerCancelar(e -> onCancelar());
        view.adicionarListenerServidorMudou(e -> onServidorMudou());
        view.adicionarListenerMesMudou(e -> onMesMudou());
        view.adicionarListenerAdicionarTrecho(e -> onAdicionarTrecho());
        view.adicionarListenerRemoverTrecho(e -> onRemoverTrecho());

        // Listener do modelo de trechos — validação em tempo real
        view.getModeloTrechos().addTableModelListener(this::onTrechoAlterado);

        carregarMeses();
        recarregarServidores();
        carregarLancamentos();
    }

    // Ações de combo
    private void onServidorMudou() {
        if (populando) return;
        carregarLancamentos();
    }

    private void onMesMudou() {
        if (populando) return;
        carregarLancamentos();
    }

    // Ações de trecho
    private void onAdicionarTrecho() {
        view.adicionarTrecho();
        // O TrechoTableModel dispara fireTableRowsInserted — o listener
        // onTrechoAlterado cuida de revalidar e recalcular o total.
    }

    private void onRemoverTrecho() {
        view.removerTrechoSelecionado();
        // O TrechoTableModel dispara fireTableRowsDeleted — o listener
        // onTrechoAlterado cuida de recalcular o total.
    }

    private void onCancelar() {
        view.limparFormulario();
        view.atualizarTotalDia(BigDecimal.ZERO);
    }

    /**
     * Chamado a cada alteração no {@code TrechoTableModel} — seja por
     * edição de célula, inserção ou remoção de linha.
     *
     * <p>Percorre as linhas afetadas, revalida cada uma com
     * {@link ValidadorHorario} e atualiza o status no model. Depois,
     * recalcula o total do dia.</p>
     */
    private void onTrechoAlterado(TableModelEvent e) {
        if (atualizandoStatus) {
            return;   // ignora o evento que nós mesmos disparamos
        }

        int primeira = e.getFirstRow();
        int ultima = e.getLastRow();

        // Evento do tipo "toda a tabela mudou" (ex.: limpar()) — percorre tudo
        if (primeira == TableModelEvent.HEADER_ROW) {
            return;
        }

        for (int row = primeira; row <= ultima; row++) {
            validarLinha(row);
        }

        recalcularTotalDia();
    }

    private void validarLinha(int row) {
        List<TrechoDTO> trechos = view.getTrechos();
        if (row < 0 || row >= trechos.size()) {
            return;
        }
        TrechoDTO trechoDTO = trechos.get(row);
        ResultadoTrechoDTO resultado = validarTrecho(trechoDTO);
        if (resultado == null) {
            return;
        }

        atualizandoStatus = true;
        try {
            view.atualizarStatusTrecho(row, resultado);
        } finally {
            atualizandoStatus = false;
        }
    }

    /**
     * Valida um trecho individualmente. Devolve {@code null} quando faltam
     * horários ou valor (linha em branco, ainda sendo preenchida).
     */
    private ResultadoTrechoDTO validarTrecho(TrechoDTO dto) {
        if (dto == null
                || dto.horaReferencia() == null
                || dto.horaComparada() == null
                || dto.valor() == null) {
            return null;
        }

        try {
            Trecho trecho = dto.paraDominio();
            ResultadoValidacao validacao = validador.validar(
                    trecho.horaReferencia(),
                    trecho.horaComparada()
            );
            ResultadoTrecho resultado = ResultadoTrecho.de(trecho, validacao);
            return ResultadoTrechoDTO.de(resultado);

        } catch (RuntimeException e) {
            log.warn("Falha ao validar trecho: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Soma os valores dos trechos válidos e atualiza o total na view.
     */
    private void recalcularTotalDia() {
        List<TrechoDTO> trechos = view.getTrechos();
        BigDecimal total = BigDecimal.ZERO;

        for (TrechoDTO dto : trechos) {
            ResultadoTrechoDTO resultado = validarTrecho(dto);
            if (resultado != null && resultado.valido()) {
                total = total.add(resultado.valorContabilizado());
            }
        }

        view.atualizarTotalDia(total);
    }

    // Salvar / Excluir
    private void onSalvar() {
        Long servidorId = view.getServidorSelecionadoId();
        LocalDate data = view.getData();
        List<TrechoDTO> trechos = view.getTrechos();

        try {
            registrar.executar(servidorId, data, trechos);
            view.limparFormulario();
            view.atualizarTotalDia(BigDecimal.ZERO);
            carregarLancamentos();
            view.mostrarMensagem("Sucesso", "Lançamento registrado.");
        } catch (ApplicationException e) {
            view.mostrarErro(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao registrar lançamento", e);
            view.mostrarErro("Erro inesperado ao registrar lançamento. Consulte o log.");
        }
    }

    private void onExcluir() {
        LancamentoDTO selecionado = view.getLancamentoSelecionado();
        if (selecionado == null) {
            view.mostrarErro("Selecione um lançamento na tabela.");
            return;
        }
        if (!view.confirmar("Excluir o lançamento de " + selecionado.data() + "?")) {
            return;
        }
        try {
            remover.executar(selecionado.id());
            carregarLancamentos();
            view.mostrarMensagem("Sucesso", "Lançamento excluído.");
        } catch (ApplicationException e) {
            view.mostrarErro(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao excluir lançamento", e);
            view.mostrarErro("Erro inesperado ao excluir lançamento. Consulte o log.");
        }
    }

    // Carregamentos
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

    public void recarregarServidores() {
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
            view.popularTabelaLancamentos(List.of());
            return;
        }

        try {
            List<LancamentoComStatusDTO> lancamentos =
                    listarLancamentos.executar(servidorId, mes);
            view.popularTabelaLancamentos(lancamentos);
        } catch (ApplicationException e) {
            view.mostrarErro("Erro ao carregar lançamentos: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao carregar lançamentos", e);
            view.mostrarErro("Erro inesperado ao carregar lançamentos. Consulte o log.");
        }
    }
}