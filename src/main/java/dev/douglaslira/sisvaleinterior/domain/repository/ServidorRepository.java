package dev.douglaslira.sisvaleinterior.domain.repository;

import dev.douglaslira.sisvaleinterior.domain.model.Servidor;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de {@link Servidor}.
 * <p>Vive no domínio — define <strong>o que</strong> precisa ser persistido,
 * não <strong>como</strong>. A implementação concreta (JDBC, SQLite, etc.)
 * fica na camada de infraestrutura.</p>
 * <p>Nenhuma operação aqui expõe detalhes técnicos (Connection, ResultSet,
 * SQLException). Falhas de persistência são sinalizadas via
 * {@code PersistenceException} (runtime).</p>
 */
public interface ServidorRepository {

    /**
     * Salva o servidor — <strong>insere</strong> se o {@code id} for nulo,
     * <strong>atualiza</strong> caso contrário.
     *
     * @param servidor servidor a salvar (não pode ser nulo)
     * @return o servidor salvo, com {@code id} preenchido após inserção
     */
    Servidor salvar(Servidor servidor);

    /**
     * @param id identificador do servidor
     * @return o servidor, se existir
     */
    Optional<Servidor> buscarPorId(Long id);

    /**
     * @param matricula matrícula funcional
     * @return o servidor, se existir
     */
    Optional<Servidor> buscarPorMatricula(String matricula);

    /**
     * @param cpf CPF no formato puro (11 dígitos) ou com máscara
     * @return o servidor, se existir
     */
    Optional<Servidor> buscarPorCpf(String cpf);

    /**
     * @return todos os servidores cadastrados (ativos e inativos)
     */
    List<Servidor> listarTodos();

    /**
     * @return apenas os servidores ativos
     */
    List<Servidor> listarAtivos();

    /**
     * Desativa o servidor — <strong>soft delete</strong>: a linha permanece
     * no banco, apenas o campo {@code ativo} vira {@code false}. Mantém o
     * histórico de lançamentos associados.
     *
     * @param id identificador do servidor a desativar
     */
    void desativar(Long id);
}