package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.domain.repository.LancamentoRepository;
import dev.douglaslira.sisvaleinterior.domain.repository.ServidorRepository;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;

public final class RemoverServidorUseCase {

    private final ServidorRepository servidorRepository;
    private final LancamentoRepository lancamentoRepository;

    public RemoverServidorUseCase(ServidorRepository servidorRepository,
                                LancamentoRepository lancamentoRepository) {
        if (servidorRepository == null) {
            throw new IllegalArgumentException("ServidorRepository é obrigatório");
        }
        if (lancamentoRepository == null) {
            throw new IllegalArgumentException("LancamentoRepository é obrigatório");
        }
        this.servidorRepository = servidorRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    public void executar(Long servidorId) {
        if (servidorId == null) {
            throw new ApplicationException("Servidor é obrigatório");
        }
        try {
            if (!lancamentoRepository.listarPorServidor(servidorId).isEmpty()) {
                throw new ApplicationException(
                        "Este servidor já tem lançamentos registrados — excluir apagaria "
                                + "esse histórico junto. Use \"Desativar\" para mantê-lo fora "
                                + "de novos lançamentos sem perder o histórico.");
            }
            servidorRepository.remover(servidorId);
        } catch (PersistenceException e) {
            throw new ApplicationException("Erro ao excluir servidor", e);
        }
    }
}