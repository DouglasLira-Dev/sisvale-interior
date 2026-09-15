package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.dto.LancamentoComStatusDTO;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoDia;
import dev.douglaslira.sisvaleinterior.domain.repository.LancamentoRepository;
import dev.douglaslira.sisvaleinterior.domain.service.CalculadoraRessarcimento;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;

import java.time.YearMonth;
import java.util.List;

/**
 * Caso de uso: listar os lançamentos de um servidor em um mês,
 * já com o status consolidado de cada dia.
 *
 * <p>Combina a busca no repositório com o cálculo do domínio
 * ({@link CalculadoraRessarcimento#calcularDia}) para devolver um DTO
 * pronto para a UI — sem que ela precise recalcular nada.</p>
 */
public final class ListarLancamentosUseCase {

    private final LancamentoRepository lancamentoRepository;
    private final CalculadoraRessarcimento calculadora;

    /**
     * @param lancamentoRepository repositório de lançamentos (não pode ser nulo)
     * @param calculadora          calculadora de ressarcimento do domínio (não pode ser nula)
     * @throws IllegalArgumentException se algum argumento for nulo
     */
    public ListarLancamentosUseCase(LancamentoRepository lancamentoRepository,
                                    CalculadoraRessarcimento calculadora) {
        if (lancamentoRepository == null) {
            throw new IllegalArgumentException("LancamentoRepository é obrigatório");
        }
        if (calculadora == null) {
            throw new IllegalArgumentException("CalculadoraRessarcimento é obrigatória");
        }
        this.lancamentoRepository = lancamentoRepository;
        this.calculadora = calculadora;
    }

    /**
     * Lista os lançamentos do servidor no mês informado, com status e total.
     *
     * @param servidorId id do servidor (obrigatório)
     * @param mesAno     mês/ano de referência (obrigatório)
     * @return lista de lançamentos com status, ordenada por data
     * @throws ApplicationException se algum argumento for nulo ou a
     *                              persistência falhar
     */
    public List<LancamentoComStatusDTO> executar(Long servidorId, YearMonth mesAno) {
        validarEntrada(servidorId, mesAno);

        List<Lancamento> lancamentos = buscar(servidorId, mesAno);
        return mapearComStatus(lancamentos);
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

    private List<Lancamento> buscar(Long servidorId, YearMonth mesAno) {
        try {
            return lancamentoRepository.buscarPorServidorEMes(servidorId, mesAno);
        } catch (PersistenceException e) {
            throw new ApplicationException("Erro ao listar lançamentos", e);
        }
    }

    private List<LancamentoComStatusDTO> mapearComStatus(List<Lancamento> lancamentos) {
        return lancamentos.stream()
                .map(this::paraDTO)
                .toList();
    }

    private LancamentoComStatusDTO paraDTO(Lancamento lancamento) {
        ResultadoDia resultado = calculadora.calcularDia(lancamento);
        return LancamentoComStatusDTO.de(lancamento, resultado);
    }
}