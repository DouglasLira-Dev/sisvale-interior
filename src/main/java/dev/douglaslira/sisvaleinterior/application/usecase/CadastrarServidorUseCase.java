package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.dto.ServidorDTO;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.domain.exception.CpfInvalidoException;
import dev.douglaslira.sisvaleinterior.domain.model.Servidor;
import dev.douglaslira.sisvaleinterior.domain.repository.ServidorRepository;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;

/**
 * Caso de uso: cadastrar um novo servidor.
 *
 * <p>Valida a entrada, cria a entidade (que valida o CPF), verifica
 * duplicidade de matrícula e CPF, persiste e devolve um {@link ServidorDTO}
 * pronto para a UI.</p>
 *
 * <p>Exceções do domínio e da persistência são convertidas em
 * {@link ApplicationException} — a UI só precisa capturar essa.</p>
 */
public final class CadastrarServidorUseCase {

    private final ServidorRepository servidorRepository;

    /**
     * @param servidorRepository repositório de servidores (não pode ser nulo)
     * @throws IllegalArgumentException se {@code servidorRepository} for nulo
     */
    public CadastrarServidorUseCase(ServidorRepository servidorRepository) {
        if (servidorRepository == null) {
            throw new IllegalArgumentException("ServidorRepository é obrigatório");
        }
        this.servidorRepository = servidorRepository;
    }

    /**
     * Cadastra um novo servidor.
     *
     * @param nome      nome completo (obrigatório)
     * @param matricula matrícula funcional (obrigatória, única)
     * @param cpf       CPF com ou sem máscara (obrigatório, único, válido)
     * @return DTO do servidor cadastrado
     * @throws ApplicationException se a entrada for inválida, o CPF for inválido,
     *                              ou a matrícula/CPF já existirem
     */
    public ServidorDTO executar(String nome, String matricula, String cpf) {
        validarEntrada(nome, matricula, cpf);

        Servidor servidor = criarServidor(nome, matricula, cpf);
        verificarDuplicidade(servidor);

        Servidor salvo = persistir(servidor);
        return ServidorDTO.de(salvo);
    }

    // Passos
    private void validarEntrada(String nome, String matricula, String cpf) {
        if (nome == null || nome.isBlank()) {
            throw new ApplicationException("Nome é obrigatório");
        }
        if (matricula == null || matricula.isBlank()) {
            throw new ApplicationException("Matrícula é obrigatória");
        }
        if (cpf == null || cpf.isBlank()) {
            throw new ApplicationException("CPF é obrigatório");
        }
    }

    private Servidor criarServidor(String nome, String matricula, String cpf) {
        try {
            return new Servidor(null, nome, matricula, cpf, true);
        } catch (CpfInvalidoException e) {
            throw new ApplicationException("CPF inválido", e);
        }
    }

    private void verificarDuplicidade(Servidor servidor) {
        servidorRepository.buscarPorMatricula(servidor.matricula())
                .ifPresent(s -> {
                    throw new ApplicationException("Matrícula já cadastrada");
                });
    }

    private Servidor persistir(Servidor servidor) {
        try {
            return servidorRepository.salvar(servidor);
        } catch (PersistenceException e) {
            throw new ApplicationException("Erro ao salvar servidor", e);
        }
    }
}