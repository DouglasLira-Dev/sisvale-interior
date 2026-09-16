package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.dto.LancamentoDTO;
import dev.douglaslira.sisvaleinterior.application.dto.TrechoDTO;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.domain.exception.HorarioInvalidoException;
import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;
import dev.douglaslira.sisvaleinterior.domain.model.Trecho;
import dev.douglaslira.sisvaleinterior.domain.repository.LancamentoRepository;
import dev.douglaslira.sisvaleinterior.domain.repository.ServidorRepository;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;

import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso: registrar um lançamento diário de servidor.
 *
 * <p>Não aplica a regra de tolerância de 15 minutos — apenas registra.
 * A validação é feita em separado pelo cálculo mensal
 * ({@code ValidarMesUseCase}).</p>
 *
 * <p>Exceções do domínio e da persistência são convertidas em
 * {@link ApplicationException} — a UI só precisa capturar essa.</p>
 */
public final class RegistrarLancamentoUseCase {

    private final LancamentoRepository lancamentoRepository;
    private final ServidorRepository servidorRepository;

    /**
     * @param lancamentoRepository repositório de lançamentos (não pode ser nulo)
     * @param servidorRepository   repositório de servidores (não pode ser nulo)
     * @throws IllegalArgumentException se algum repositório for nulo
     */
    public RegistrarLancamentoUseCase(LancamentoRepository lancamentoRepository,
                                    ServidorRepository servidorRepository) {
        if (lancamentoRepository == null) {
            throw new IllegalArgumentException("LancamentoRepository é obrigatório");
        }
        if (servidorRepository == null) {
            throw new IllegalArgumentException("ServidorRepository é obrigatório");
        }
        this.lancamentoRepository = lancamentoRepository;
        this.servidorRepository = servidorRepository;
    }

    /**
     * Registra um lançamento diário com N trechos.
     *
     * @param servidorId id do servidor (obrigatório)
     * @param data       data do lançamento (obrigatória)
     * @param trechos    lista de trechos (não pode ser nula nem vazia)
     * @return DTO do lançamento registrado
     * @throws ApplicationException se a entrada for inválida, o servidor não existir,
     *                              houver duplicidade, ou falhar a persistência
     */
    public LancamentoDTO executar(Long servidorId,
                                LocalDate data,
                                List<TrechoDTO> trechos) {

        validarEntrada(servidorId, data, trechos);
        verificarServidorExiste(servidorId);
        verificarDuplicidade(servidorId, data);

        Lancamento lancamento = criarLancamento(servidorId, data, trechos);
        Lancamento salvo = persistir(lancamento);
        return LancamentoDTO.de(salvo);
    }

    // Passos
    private void validarEntrada(Long servidorId, LocalDate data, List<TrechoDTO> trechos) {
        if (servidorId == null) {
            throw new ApplicationException("Servidor é obrigatório");
        }
        if (data == null) {
            throw new ApplicationException("Data é obrigatória");
        }
        if (trechos == null || trechos.isEmpty()) {
            throw new ApplicationException("Informe pelo menos um trecho");
        }
    }

    private void verificarServidorExiste(Long servidorId) {
        try {
            if (servidorRepository.buscarPorId(servidorId).isEmpty()) {
                throw new ApplicationException("Servidor não encontrado");
            }
        } catch (PersistenceException e) {
            throw new ApplicationException("Erro ao consultar servidor", e);
        }
    }

    private void verificarDuplicidade(Long servidorId, LocalDate data) {
        try {
            lancamentoRepository.buscarPorServidorEData(servidorId, data)
                    .ifPresent(l -> {
                        throw new ApplicationException(
                                "Já existe lançamento para este servidor nesta data");
                    });
        } catch (PersistenceException e) {
            throw new ApplicationException("Erro ao verificar duplicidade", e);
        }
    }

    private Lancamento criarLancamento(Long servidorId, LocalDate data, List<TrechoDTO> trechosDTO) {
        try {
            List<Trecho> trechos = trechosDTO.stream()
                    .map(TrechoDTO::paraDominio)
                    .toList();
            return new Lancamento(null, servidorId, data, trechos);

        } catch (HorarioInvalidoException e) {
            throw new ApplicationException("Horário inválido: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException("Dados do lançamento inválidos: " + e.getMessage(), e);
        }
    }

    private Lancamento persistir(Lancamento lancamento) {
        try {
            return lancamentoRepository.salvar(lancamento);
        } catch (PersistenceException e) {
            throw new ApplicationException("Erro ao salvar lançamento", e);
        }
    }
}