package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.dto.ResumoMensalDTO;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoMes;
import dev.douglaslira.sisvaleinterior.domain.model.Servidor;
import dev.douglaslira.sisvaleinterior.domain.repository.LancamentoRepository;
import dev.douglaslira.sisvaleinterior.domain.repository.ServidorRepository;
import dev.douglaslira.sisvaleinterior.domain.service.CalculadoraRessarcimento;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;

import java.time.YearMonth;
import java.util.List;

/**
 * Caso de uso: validar o mês de um servidor e devolver o resumo consolidado.
 *
 * <p>É o <strong>coração do sistema</strong>: busca os lançamentos do mês,
 * delega o cálculo à {@link CalculadoraRessarcimento} e converte o resultado
 * em {@link ResumoMensalDTO} pronto para a UI.</p>
 *
 * <p>Não aplica regra de tolerância aqui — a validação é responsabilidade
 * do domínio ({@code ValidadorHorario}, usado indiretamente pela calculadora).</p>
 */
public final class ValidarMesUseCase {

    private final LancamentoRepository lancamentoRepository;
    private final ServidorRepository servidorRepository;
    private final CalculadoraRessarcimento calculadora;

    /**
     * @param lancamentoRepository repositório de lançamentos (não pode ser nulo)
     * @param servidorRepository   repositório de servidores (não pode ser nulo)
     * @param calculadora          calculadora de ressarcimento do domínio (não pode ser nula)
     * @throws IllegalArgumentException se algum argumento for nulo
     */
    public ValidarMesUseCase(LancamentoRepository lancamentoRepository,
                            ServidorRepository servidorRepository,
                            CalculadoraRessarcimento calculadora) {
        if (lancamentoRepository == null) {
            throw new IllegalArgumentException("LancamentoRepository é obrigatório");
        }
        if (servidorRepository == null) {
            throw new IllegalArgumentException("ServidorRepository é obrigatório");
        }
        if (calculadora == null) {
            throw new IllegalArgumentException("CalculadoraRessarcimento é obrigatória");
        }
        this.lancamentoRepository = lancamentoRepository;
        this.servidorRepository = servidorRepository;
        this.calculadora = calculadora;
    }

    /**
     * Valida o mês de um servidor e devolve o resumo consolidado.
     *
     * <p>Mês sem lançamentos é um caso válido — devolve resumo com total
     * {@code R$ 0,00} e lista de dias vazia.</p>
     *
     * @param servidorId id do servidor (obrigatório)
     * @param mesAno     mês/ano de referência (obrigatório)
     * @return resumo mensal pronto para exibição
     * @throws ApplicationException se a entrada for inválida, o servidor não existir,
     *                              ou falhar a persistência
     */
    public ResumoMensalDTO executar(Long servidorId, YearMonth mesAno) {
        validarEntrada(servidorId, mesAno);

        Servidor servidor = buscarServidor(servidorId);
        List<Lancamento> lancamentos = buscarLancamentos(servidorId, mesAno);
        ResultadoMes resultado = calcular(lancamentos, mesAno, servidorId);

        return ResumoMensalDTO.de(resultado, servidor);
    }
    // Passos
    private void validarEntrada(Long servidorId, YearMonth mesAno) {
        if (servidorId == null) {
            throw new ApplicationException("Servidor é obrigatório");
        }
        if (mesAno == null) {
            throw new ApplicationException("Mês/ano é obrigatório");
        }
    }

    private Servidor buscarServidor(Long servidorId) {
        try {
            return servidorRepository.buscarPorId(servidorId)
                    .orElseThrow(() -> new ApplicationException("Servidor não encontrado"));
        } catch (PersistenceException e) {
            throw new ApplicationException("Erro ao consultar servidor", e);
        }
    }

    private List<Lancamento> buscarLancamentos(Long servidorId, YearMonth mesAno) {
        try {
            return lancamentoRepository.buscarPorServidorEMes(servidorId, mesAno);
        } catch (PersistenceException e) {
            throw new ApplicationException("Erro ao consultar lançamentos", e);
        }
    }

    private ResultadoMes calcular(List<Lancamento> lancamentos, YearMonth mesAno, Long servidorId) {
        return calculadora.calcularMes(lancamentos, mesAno, servidorId);
    }
}