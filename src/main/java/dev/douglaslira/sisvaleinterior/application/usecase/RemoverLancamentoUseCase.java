package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.domain.repository.LancamentoRepository;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;

public final class RemoverLancamentoUseCase {

    private final LancamentoRepository lancamentoRepository;

    public RemoverLancamentoUseCase(LancamentoRepository lancamentoRepository) {
        if (lancamentoRepository == null) {
            throw new IllegalArgumentException("LancamentoRepository é obrigatório");
        }
        this.lancamentoRepository = lancamentoRepository;
    }

    public void executar(Long lancamentoId) {
        if (lancamentoId == null) {
            throw new ApplicationException("Lançamento é obrigatório");
        }
        try {
            lancamentoRepository.remover(lancamentoId);
        } catch (PersistenceException e) {
            throw new ApplicationException("Erro ao excluir lançamento", e);
        }
    }
}