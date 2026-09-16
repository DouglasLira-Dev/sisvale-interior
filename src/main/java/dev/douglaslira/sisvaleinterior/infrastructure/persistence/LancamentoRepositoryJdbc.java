package dev.douglaslira.sisvaleinterior.infrastructure.persistence;

import dev.douglaslira.sisvaleinterior.domain.model.Horario;
import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;
import dev.douglaslira.sisvaleinterior.domain.model.Trecho;
import dev.douglaslira.sisvaleinterior.domain.repository.LancamentoRepository;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementação JDBC de {@link LancamentoRepository} sobre SQLite.
 *
 * <p>Toda a manipulação de JDBC acontece aqui. {@code SQLException} nunca
 * vaza — é sempre encapsulada em {@link PersistenceException}.</p>
 *
 * <p><strong>Transações:</strong> o {@code salvar} executa em uma única
 * {@link Connection} com {@code setAutoCommit(false)} — o INSERT do lançamento
 * e os INSERTs dos trechos são atômicos. Em caso de falha, rollback garante
 * que nada fica pela metade.</p>
 *
 * <p><strong>Leitura:</strong> cada lançamento é buscado com seus trechos
 * (1 query para o lançamento + 1 query para os trechos, ordenados por
 * {@code ordem}).</p>
 */
public final class LancamentoRepositoryJdbc implements LancamentoRepository {

    // SQL — lancamento
    private static final String SQL_INSERT_LANCAMENTO = """
            INSERT INTO lancamento (servidor_id, data)
            VALUES (?, ?)
            """;

    private static final String SQL_UPDATE_LANCAMENTO = """
            UPDATE lancamento
            SET servidor_id = ?,
                data        = ?
            WHERE id = ?
            """;

    private static final String SQL_SELECT_LANCAMENTO_POR_ID = """
            SELECT id, servidor_id, data
            FROM lancamento
            WHERE id = ?
            """;

    private static final String SQL_SELECT_LANCAMENTO_POR_SERVIDOR_E_MES = """
            SELECT id, servidor_id, data
            FROM lancamento
            WHERE servidor_id = ?
            AND data BETWEEN ? AND ?
            ORDER BY data
            """;

    private static final String SQL_SELECT_LANCAMENTO_POR_SERVIDOR_E_DATA = """
            SELECT id, servidor_id, data
            FROM lancamento
            WHERE servidor_id = ?
            AND data = ?
            """;

    private static final String SQL_SELECT_LANCAMENTO_POR_SERVIDOR = """
            SELECT id, servidor_id, data
            FROM lancamento
            WHERE servidor_id = ?
            ORDER BY data
            """;

    private static final String SQL_DELETE_LANCAMENTO = """
            DELETE FROM lancamento
            WHERE id = ?
            """;

    // ---------------------------------------------------------------
    // SQL — trecho
    // ---------------------------------------------------------------

    private static final String SQL_INSERT_TRECHO = """
            INSERT INTO trecho (lancamento_id, ordem, hora_referencia, hora_comparada, valor)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String SQL_DELETE_TRECHOS_POR_LANCAMENTO = """
            DELETE FROM trecho
            WHERE lancamento_id = ?
            """;

    private static final String SQL_SELECT_TRECHOS_POR_LANCAMENTO = """
            SELECT ordem, hora_referencia, hora_comparada, valor
            FROM trecho
            WHERE lancamento_id = ?
            ORDER BY ordem
            """;

    private final ConnectionFactory connectionFactory;

    /**
     * @param connectionFactory fábrica de conexões (não pode ser nula)
     * @throws IllegalArgumentException se {@code connectionFactory} for nulo
     */
    public LancamentoRepositoryJdbc(ConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("ConnectionFactory é obrigatório");
        }
        this.connectionFactory = connectionFactory;
    }

    // salvar — upsert
    @Override
    public Lancamento salvar(Lancamento lancamento) {
        if (lancamento == null) {
            throw new IllegalArgumentException("Lançamento é obrigatório");
        }
        return lancamento.id() == null ? inserir(lancamento) : atualizar(lancamento);
    }


    // buscarPorId
    @Override
    public Optional<Lancamento> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id é obrigatório");
        }

        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_SELECT_LANCAMENTO_POR_ID)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Lancamento lancamento = mapearLancamento(rs);
                List<Trecho> trechos = buscarTrechos(conn, id);
                return Optional.of(comTrechos(lancamento, trechos));
            }

        } catch (SQLException e) {
            throw new PersistenceException("Erro ao buscar lançamento por id: " + id, e);
        }
    }

    // buscarPorServidorEMes
    @Override
    public List<Lancamento> buscarPorServidorEMes(Long servidorId, YearMonth mes) {
        if (servidorId == null) {
            throw new IllegalArgumentException("Servidor é obrigatório");
        }
        if (mes == null) {
            throw new IllegalArgumentException("Mês/ano é obrigatório");
        }

        LocalDate inicio = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();

        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_SELECT_LANCAMENTO_POR_SERVIDOR_E_MES)) {

            ps.setLong(1, servidorId);
            ps.setString(2, inicio.toString());
            ps.setString(3, fim.toString());

            try (ResultSet rs = ps.executeQuery()) {
                return mapearListaComTrechos(conn, rs);
            }

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Erro ao buscar lançamentos do servidor " + servidorId + " em " + mes, e);
        }
    }

    // buscarPorServidorEData
    @Override
    public Optional<Lancamento> buscarPorServidorEData(Long servidorId, LocalDate data) {
        if (servidorId == null) {
            throw new IllegalArgumentException("Servidor é obrigatório");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }

        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_SELECT_LANCAMENTO_POR_SERVIDOR_E_DATA)) {

            ps.setLong(1, servidorId);
            ps.setString(2, data.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Lancamento lancamento = mapearLancamento(rs);
                List<Trecho> trechos = buscarTrechos(conn, lancamento.id());
                return Optional.of(comTrechos(lancamento, trechos));
            }

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Erro ao buscar lançamento do servidor " + servidorId + " em " + data, e);
        }
    }

    // listarPorServidor
    @Override
    public List<Lancamento> listarPorServidor(Long servidorId) {
        if (servidorId == null) {
            throw new IllegalArgumentException("Servidor é obrigatório");
        }

        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_SELECT_LANCAMENTO_POR_SERVIDOR)) {

            ps.setLong(1, servidorId);

            try (ResultSet rs = ps.executeQuery()) {
                return mapearListaComTrechos(conn, rs);
            }

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Erro ao listar lançamentos do servidor " + servidorId, e);
        }
    }

    // remover
    @Override
    public void remover(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id é obrigatório");
        }

        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_DELETE_LANCAMENTO)) {

            ps.setLong(1, id);

            int linhas = ps.executeUpdate();
            if (linhas == 0) {
                throw new PersistenceException("Lançamento não encontrado para remover: id=" + id);
            }

        } catch (SQLException e) {
            throw new PersistenceException("Erro ao remover lançamento: id=" + id, e);
        }
    }

    // Inserção
    private Lancamento inserir(Lancamento lancamento) {
        Connection conn = null;
        try {
            conn = connectionFactory.getConnection();
            conn.setAutoCommit(false);

            long id = inserirLancamento(conn, lancamento);
            inserirTrechos(conn, id, lancamento.trechos());

            conn.commit();
            return comId(lancamento, id);

        } catch (SQLException e) {
            rollback(conn);
            throw new PersistenceException(
                    "Erro ao inserir lançamento (servidor=" + lancamento.servidorId()
                            + ", data=" + lancamento.data() + ")", e);
        } finally {
            fechar(conn);
        }
    }

    private long inserirLancamento(Connection conn, Lancamento lancamento) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                SQL_INSERT_LANCAMENTO, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, lancamento.servidorId());
            ps.setString(2, lancamento.data().toString());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new PersistenceException(
                            "Falha ao recuperar id gerado para o lançamento");
                }
                return keys.getLong(1);
            }
        }
    }

    private void inserirTrechos(Connection conn, long lancamentoId, List<Trecho> trechos)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_TRECHO)) {
            int ordem = 1;
            for (Trecho t : trechos) {
                ps.setLong(1, lancamentoId);
                ps.setInt(2, ordem);
                ps.setString(3, t.horaReferencia().formatado());
                ps.setString(4, t.horaComparada().formatado());
                ps.setBigDecimal(5, t.valor());
                ps.addBatch();
                ordem++;
            }
            ps.executeBatch();
        }
    }

    // Atualização
    private Lancamento atualizar(Lancamento lancamento) {
        Connection conn = null;
        try {
            conn = connectionFactory.getConnection();
            conn.setAutoCommit(false);

            int linhas = atualizarLancamento(conn, lancamento);
            if (linhas == 0) {
                throw new PersistenceException(
                        "Lançamento não encontrado para atualização: id=" + lancamento.id());
            }

            deletarTrechos(conn, lancamento.id());
            inserirTrechos(conn, lancamento.id(), lancamento.trechos());

            conn.commit();
            return lancamento;

        } catch (SQLException e) {
            rollback(conn);
            throw new PersistenceException(
                    "Erro ao atualizar lançamento: id=" + lancamento.id(), e);
        } finally {
            fechar(conn);
        }
    }

    private int atualizarLancamento(Connection conn, Lancamento lancamento) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_LANCAMENTO)) {
            ps.setLong(1, lancamento.servidorId());
            ps.setString(2, lancamento.data().toString());
            ps.setLong(3, lancamento.id());
            return ps.executeUpdate();
        }
    }

    private void deletarTrechos(Connection conn, long lancamentoId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE_TRECHOS_POR_LANCAMENTO)) {
            ps.setLong(1, lancamentoId);
            ps.executeUpdate();
        }
    }

    // Leitura auxiliar

    /**
     * Itera o {@link ResultSet} de lançamentos, busca os trechos de cada um
     * e devolve a lista imutável.
     */
    private List<Lancamento> mapearListaComTrechos(Connection conn, ResultSet rs)
            throws SQLException {

        List<Lancamento> lista = new ArrayList<>();
        while (rs.next()) {
            Lancamento lancamento = mapearLancamento(rs);
            List<Trecho> trechos = buscarTrechos(conn, lancamento.id());
            lista.add(comTrechos(lancamento, trechos));
        }
        return List.copyOf(lista);
    }

    private List<Trecho> buscarTrechos(Connection conn, long lancamentoId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_SELECT_TRECHOS_POR_LANCAMENTO)) {
            ps.setLong(1, lancamentoId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Trecho> trechos = new ArrayList<>();
                while (rs.next()) {
                    trechos.add(mapearTrecho(rs));
                }
                return trechos;
            }
        }
    }

    // Mapeamento
    private static Lancamento mapearLancamento(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long servidorId = rs.getLong("servidor_id");
        LocalDate data = LocalDate.parse(rs.getString("data"));
        return new Lancamento(id, servidorId, data, List.of(new Trecho(
                Horario.parse("00:00"), Horario.parse("00:00"), BigDecimal.ZERO)));
        // placeholder — os trechos reais são substituídos em comTrechos(...)
    }

    private static Trecho mapearTrecho(ResultSet rs) throws SQLException {
        Horario referencia = Horario.parse(rs.getString("hora_referencia"));
        Horario comparada = Horario.parse(rs.getString("hora_comparada"));
        BigDecimal valor = rs.getBigDecimal("valor");
        return new Trecho(referencia, comparada, valor);
    }

    /**
     * Cria uma cópia do lançamento com os trechos informados.
     */
    private static Lancamento comTrechos(Lancamento base, List<Trecho> trechos) {
        return new Lancamento(base.id(), base.servidorId(), base.data(), trechos);
    }

    /**
     * Cria uma cópia do lançamento com o id informado.
     */
    private static Lancamento comId(Lancamento base, long id) {
        return new Lancamento(id, base.servidorId(), base.data(), base.trechos());
    }

    // Recursos
    private static void rollback(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignore) {
            // melhor esforço: se o rollback falhar, o finally fecha a conexão
        }
    }

    private static void fechar(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException ignore) {
            // silencioso: fechar conexão não deve mascarar o erro original
        }
    }
}