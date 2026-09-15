package dev.douglaslira.sisvaleinterior.infrastructure.persistence;

import dev.douglaslira.sisvaleinterior.domain.model.Horario;
import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;
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
 * <p><strong>Formato de armazenamento:</strong> datas em ISO ({@code YYYY-MM-DD}),
 * horas em {@code HH:mm}, valores em {@code NUMERIC(10,2)}.</p>
 */
public final class LancamentoRepositoryJdbc implements LancamentoRepository {

    private static final String SQL_INSERT = """
            INSERT INTO lancamento
                (servidor_id, data, hora_descida, hora_entrada, valor_ida,
                hora_saida, hora_onibus, valor_volta)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_UPDATE = """
            UPDATE lancamento
            SET servidor_id  = ?,
                data         = ?,
                hora_descida = ?,
                hora_entrada = ?,
                valor_ida    = ?,
                hora_saida   = ?,
                hora_onibus  = ?,
                valor_volta  = ?
            WHERE id = ?
            """;

    private static final String SQL_SELECT_POR_ID = """
            SELECT id, servidor_id, data, hora_descida, hora_entrada, valor_ida,
                hora_saida, hora_onibus, valor_volta
            FROM lancamento
            WHERE id = ?
            """;

    private static final String SQL_SELECT_POR_SERVIDOR_E_MES = """
            SELECT id, servidor_id, data, hora_descida, hora_entrada, valor_ida,
                hora_saida, hora_onibus, valor_volta
            FROM lancamento
            WHERE servidor_id = ?
            AND data BETWEEN ? AND ?
            ORDER BY data
            """;

    private static final String SQL_SELECT_POR_SERVIDOR_E_DATA = """
            SELECT id, servidor_id, data, hora_descida, hora_entrada, valor_ida,
                hora_saida, hora_onibus, valor_volta
            FROM lancamento
            WHERE servidor_id = ?
            AND data = ?
            """;

    private static final String SQL_SELECT_POR_SERVIDOR = """
            SELECT id, servidor_id, data, hora_descida, hora_entrada, valor_ida,
                hora_saida, hora_onibus, valor_volta
            FROM lancamento
            WHERE servidor_id = ?
            ORDER BY data
            """;

    private static final String SQL_DELETE = """
            DELETE FROM lancamento
            WHERE id = ?
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
            PreparedStatement ps = conn.prepareStatement(SQL_SELECT_POR_ID)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
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
            PreparedStatement ps = conn.prepareStatement(SQL_SELECT_POR_SERVIDOR_E_MES)) {

            ps.setLong(1, servidorId);
            ps.setString(2, inicio.toString());
            ps.setString(3, fim.toString());

            try (ResultSet rs = ps.executeQuery()) {
                return mapearLista(rs);
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
            PreparedStatement ps = conn.prepareStatement(SQL_SELECT_POR_SERVIDOR_E_DATA)) {

            ps.setLong(1, servidorId);
            ps.setString(2, data.toString());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
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
            PreparedStatement ps = conn.prepareStatement(SQL_SELECT_POR_SERVIDOR)) {

            ps.setLong(1, servidorId);

            try (ResultSet rs = ps.executeQuery()) {
                return mapearLista(rs);
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
            PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setLong(1, id);

            int linhas = ps.executeUpdate();
            if (linhas == 0) {
                throw new PersistenceException("Lançamento não encontrado para remover: id=" + id);
            }

        } catch (SQLException e) {
            throw new PersistenceException("Erro ao remover lançamento: id=" + id, e);
        }
    }

    // Métodos privados

    /**
     * Insere um novo lançamento e recupera o id gerado.
     * <p>{@code Lancamento} é imutável — devolve uma nova instância com o id
     * preenchido.</p>
     */
    private Lancamento inserir(Lancamento lancamento) {
        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            preencherParametros(ps, lancamento);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new PersistenceException(
                            "Falha ao recuperar id gerado para o lançamento");
                }
                long id = keys.getLong(1);
                return comId(lancamento, id);
            }

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Erro ao inserir lançamento (servidor=" + lancamento.servidorId()
                            + ", data=" + lancamento.data() + ")", e);
        }
    }

    /**
     * Atualiza um lançamento existente. Falha se o id não existir.
     */
    private Lancamento atualizar(Lancamento lancamento) {
        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            preencherParametros(ps, lancamento);
            ps.setLong(9, lancamento.id());

            int linhas = ps.executeUpdate();
            if (linhas == 0) {
                throw new PersistenceException(
                        "Lançamento não encontrado para atualização: id=" + lancamento.id());
            }

            return lancamento;

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Erro ao atualizar lançamento: id=" + lancamento.id(), e);
        }
    }

    /**
     * Preenche os 8 parâmetros do INSERT/UPDATE na ordem do SQL.
     */
    private void preencherParametros(PreparedStatement ps, Lancamento l) throws SQLException {
        ps.setLong(1, l.servidorId());
        ps.setString(2, l.data().toString());
        ps.setString(3, l.horaDescida().formatado());
        ps.setString(4, l.horaEntrada().formatado());
        ps.setBigDecimal(5, l.valorIda());
        ps.setString(6, l.horaSaida().formatado());
        ps.setString(7, l.horaOnibus().formatado());
        ps.setBigDecimal(8, l.valorVolta());
    }

    /**
     * Executa o loop de mapeamento e devolve lista imutável.
     */
    private List<Lancamento> mapearLista(ResultSet rs) throws SQLException {
        List<Lancamento> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(mapear(rs));
        }
        return List.copyOf(lista);
    }

    /**
     * Mapeia a linha atual do {@link ResultSet} para um {@link Lancamento}.
     * <p>Ponto único de conversão ResultSet → domínio.</p>
     */
    private Lancamento mapear(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long servidorId = rs.getLong("servidor_id");
        LocalDate data = LocalDate.parse(rs.getString("data"));
        Horario horaDescida = Horario.parse(rs.getString("hora_descida"));
        Horario horaEntrada = Horario.parse(rs.getString("hora_entrada"));
        BigDecimal valorIda = rs.getBigDecimal("valor_ida");
        Horario horaSaida = Horario.parse(rs.getString("hora_saida"));
        Horario horaOnibus = Horario.parse(rs.getString("hora_onibus"));
        BigDecimal valorVolta = rs.getBigDecimal("valor_volta");

        return new Lancamento(
                id, servidorId, data,
                horaDescida, horaEntrada, valorIda,
                horaSaida, horaOnibus, valorVolta
        );
    }

    /**
     * Cria uma cópia do lançamento com o id informado.
     */
    private Lancamento comId(Lancamento l, long id) {
        return new Lancamento(
                id, l.servidorId(), l.data(),
                l.horaDescida(), l.horaEntrada(), l.valorIda(),
                l.horaSaida(), l.horaOnibus(), l.valorVolta()
        );
    }
}