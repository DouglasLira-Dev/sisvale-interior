package dev.douglaslira.sisvaleinterior.infrastructure.persistence;

import dev.douglaslira.sisvaleinterior.domain.model.Servidor;
import dev.douglaslira.sisvaleinterior.domain.repository.ServidorRepository;
import dev.douglaslira.sisvaleinterior.domain.service.ValidadorCpf;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementação JDBC de {@link ServidorRepository} sobre SQLite.
 *
 * <p>Toda a manipulação de JDBC acontece aqui. {@code SQLException} nunca
 * vaza — é sempre encapsulada em {@link PersistenceException}.</p>
 */
public final class ServidorRepositoryJdbc implements ServidorRepository {

    private static final String SQL_INSERT = """
            INSERT INTO servidor (nome, matricula, cpf, ativo)
            VALUES (?, ?, ?, ?)
            """;

    private static final String SQL_UPDATE = """
            UPDATE servidor
            SET nome = ?,
                matricula = ?,
                cpf = ?,
                ativo = ?
            WHERE id = ?
            """;

    private static final String SQL_SELECT_POR_ID = """
            SELECT id, nome, matricula, cpf, ativo
            FROM servidor
            WHERE id = ?
            """;

    private static final String SQL_SELECT_POR_MATRICULA = """
            SELECT id, nome, matricula, cpf, ativo
            FROM servidor
            WHERE matricula = ?
            """;

    private static final String SQL_SELECT_POR_CPF = """
            SELECT id, nome, matricula, cpf, ativo
            FROM servidor
            WHERE cpf = ?
            """;

    private static final String SQL_SELECT_TODOS = """
            SELECT id, nome, matricula, cpf, ativo
            FROM servidor
            ORDER BY nome
            """;

    private static final String SQL_SELECT_ATIVOS = """
            SELECT id, nome, matricula, cpf, ativo
            FROM servidor
            WHERE ativo = 1
            ORDER BY nome
            """;

    private static final String SQL_DESATIVAR = """
            UPDATE servidor
            SET ativo = 0
            WHERE id = ?
            """;

    private final ConnectionFactory connectionFactory;

    /**
     * @param connectionFactory fábrica de conexões (não pode ser nula)
     * @throws IllegalArgumentException se {@code connectionFactory} for nulo
     */
    public ServidorRepositoryJdbc(ConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("ConnectionFactory é obrigatório");
        }
        this.connectionFactory = connectionFactory;
    }

    // salvar — upsert
    @Override
    public Servidor salvar(Servidor servidor) {
        if (servidor == null) {
            throw new IllegalArgumentException("Servidor é obrigatório");
        }
        return servidor.id() == null ? inserir(servidor) : atualizar(servidor);
    }

    // buscarPorId
    @Override
    public Optional<Servidor> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id é obrigatório");
        }

        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_SELECT_POR_ID)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new PersistenceException("Erro ao buscar servidor por id: " + id, e);
        }
    }

    // buscarPorMatricula
    @Override
    public Optional<Servidor> buscarPorMatricula(String matricula) {
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("Matrícula é obrigatória");
        }

        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_SELECT_POR_MATRICULA)) {

            ps.setString(1, matricula.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new PersistenceException("Erro ao buscar servidor por matrícula: " + matricula, e);
        }
    }

    @Override
    public Optional<Servidor> buscarPorCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }

        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_SELECT_POR_CPF)) {

            ps.setString(1, ValidadorCpf.normalizar(cpf));   // ← normaliza

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new PersistenceException("Erro ao buscar servidor por CPF", e);
        }
    }

    // listarTodos
    @Override
    public List<Servidor> listarTodos() {
        return listar(SQL_SELECT_TODOS, "Erro ao listar servidores");
    }

    // listarAtivos
    @Override
    public List<Servidor> listarAtivos() {
        return listar(SQL_SELECT_ATIVOS, "Erro ao listar servidores ativos");
    }

    // desativar
    @Override
    public void desativar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id é obrigatório");
        }

        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_DESATIVAR)) {

            ps.setLong(1, id);

            int linhas = ps.executeUpdate();
            if (linhas == 0) {
                throw new PersistenceException("Servidor não encontrado para desativar: id=" + id);
            }

        } catch (SQLException e) {
            throw new PersistenceException("Erro ao desativar servidor: id=" + id, e);
        }
    }
    // Métodos privados
    /**
     * Insere um novo servidor e recupera o id gerado.
     *
     * <p>{@code Servidor} é imutável — devolve uma nova instância com o id
     * preenchido.</p>
     */
    private Servidor inserir(Servidor servidor) {
        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, servidor.nome());
            ps.setString(2, servidor.matricula());
            ps.setString(3, servidor.cpf());
            ps.setInt(4, servidor.ativo() ? 1 : 0);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new PersistenceException("Falha ao recuperar id gerado para o servidor");
                }
                long id = keys.getLong(1);
                return new Servidor(id, servidor.nome(), servidor.matricula(),
                        servidor.cpf(), servidor.ativo());
            }

        } catch (SQLException e) {
            throw new PersistenceException("Erro ao inserir servidor: " + servidor.matricula(), e);
        }
    }

    /**
     * Atualiza um servidor existente. Falha se o id não existir.
     */
    private Servidor atualizar(Servidor servidor) {
        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, servidor.nome());
            ps.setString(2, servidor.matricula());
            ps.setString(3, servidor.cpf());
            ps.setInt(4, servidor.ativo() ? 1 : 0);
            ps.setLong(5, servidor.id());

            int linhas = ps.executeUpdate();
            if (linhas == 0) {
                throw new PersistenceException(
                        "Servidor não encontrado para atualização: id=" + servidor.id());
            }

            return servidor;

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Erro ao atualizar servidor: id=" + servidor.id(), e);
        }
    }

    /**
     * Executa uma query de listagem e mapeia cada linha para {@link Servidor}.
     */
    private List<Servidor> listar(String sql, String mensagemErro) {
        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            List<Servidor> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return List.copyOf(lista);

        } catch (SQLException e) {
            throw new PersistenceException(mensagemErro, e);
        }
    }

    /**
     * Mapeia a linha atual do {@link ResultSet} para um {@link Servidor}.
     *
     * <p>Ponto único de conversão ResultSet → domínio.</p>
     */
    private Servidor mapear(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String nome = rs.getString("nome");
        String matricula = rs.getString("matricula");
        String cpf = rs.getString("cpf");
        boolean ativo = rs.getInt("ativo") == 1;

        return new Servidor(id, nome, matricula, cpf, ativo);
    }
}