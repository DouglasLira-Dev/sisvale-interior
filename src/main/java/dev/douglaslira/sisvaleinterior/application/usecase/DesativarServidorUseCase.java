package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.domain.repository.ServidorRepository;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;

public final class DesativarServidorUseCase {

    private final ServidorRepository servidorRepository;

    public DesativarServidorUseCase(ServidorRepository servidorRepository) {
        if (servidorRepository == null) {
            throw new IllegalArgumentException("ServidorRepository é obrigatório");
        }
        this.servidorRepository = servidorRepository;
    }

    public void executar(Long servidorId) {
        if (servidorId == null) {
            throw new ApplicationException("Servidor é obrigatório");
        }
        try {
            servidorRepository.desativar(servidorId);
        } catch (PersistenceException e) {
            throw new ApplicationException("Erro ao excluir servidor", e);
        }
    }
}