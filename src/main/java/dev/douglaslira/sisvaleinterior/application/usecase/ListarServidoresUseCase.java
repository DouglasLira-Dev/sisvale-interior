package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.dto.ServidorDTO;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.domain.model.Servidor;
import dev.douglaslira.sisvaleinterior.domain.repository.ServidorRepository;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;

import java.util.List;

/**
 * Caso de uso: listar servidores para a UI.
 *
 * <p>Devolve DTOs com CPF mascarado, prontos para exibição. Nunca lança
 * exceção para lista vazia — retorna lista vazia.</p>
 */
public final class ListarServidoresUseCase {

    private final ServidorRepository servidorRepository;

    /**
     * @param servidorRepository repositório de servidores (não pode ser nulo)
     * @throws IllegalArgumentException se {@code servidorRepository} for nulo
     */
    public ListarServidoresUseCase(ServidorRepository servidorRepository) {
        if (servidorRepository == null) {
            throw new IllegalArgumentException("ServidorRepository é obrigatório");
        }
        this.servidorRepository = servidorRepository;
    }

    /**
     * Lista todos os servidores (ativos e inativos), ordenados por nome.
     *
     * @return lista de DTOs (possivelmente vazia)
     * @throws ApplicationException em caso de falha de persistência
     */
    public List<ServidorDTO> executar() {
        return listar(() -> servidorRepository.listarTodos());
    }

    /**
     * Lista apenas os servidores ativos, ordenados por nome.
     *
     * @return lista de DTOs (possivelmente vazia)
     * @throws ApplicationException em caso de falha de persistência
     */
    public List<ServidorDTO> executarApenasAtivos() {
        return listar(() -> servidorRepository.listarAtivos());
    }

    private List<ServidorDTO> listar(FonteDeServidores fonte) {
        try {
            List<Servidor> servidores = fonte.obter();
            return servidores.stream()
                    .map(ServidorDTO::de)
                    .toList();
        } catch (PersistenceException e) {
            throw new ApplicationException("Erro ao listar servidores", e);
        }
    }

    /**
     * Abstrai a chamada ao repositório — permite reusar o mesmo bloco
     * try/catch para os dois métodos públicos.
     */
    @FunctionalInterface
    private interface FonteDeServidores {
        List<Servidor> obter();
    }
}