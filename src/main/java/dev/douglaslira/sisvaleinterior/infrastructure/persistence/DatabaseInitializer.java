package dev.douglaslira.sisvaleinterior.infrastructure.persistence;

import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Inicializa o banco de dados na primeira execução (ou a cada execução —
 * é idempotente).
 *
 * <p>Lê {@code db/schema.sql} do classpath e executa cada statement.
 * O script usa {@code CREATE TABLE IF NOT EXISTS} e {@code CREATE INDEX
 * IF NOT EXISTS}, então rodar sempre é seguro.</p>
 *
 * <p><strong>Não</strong> aplica {@code PRAGMA foreign_keys = ON} — isso é
 * responsabilidade do {@link ConnectionFactory}, que já ativa em toda
 * conexão aberta.</p>
 */
public final class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private static final String SCHEMA_PATH = "db/schema.sql";

    private final ConnectionFactory connectionFactory;

    /**
     * @param connectionFactory fábrica de conexões (não pode ser nula)
     * @throws IllegalArgumentException se {@code connectionFactory} for nulo
     */
    public DatabaseInitializer(ConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("ConnectionFactory é obrigatório");
        }
        this.connectionFactory = connectionFactory;
    }

        // DatabaseInitializer.java — chamar dentro de inicializar(), depois de rodar o schema.sql
    public void inicializar() {
        log.info("Inicializando banco de dados...");

        String sql = lerSchemaSql();
        List<String> statements = dividirStatements(sql);

        try (Connection conn = connectionFactory.getConnection();
            Statement st = conn.createStatement()) {

            for (String comando : statements) {
                st.execute(comando);
            }
            aplicarMigracoes(conn);   // nova linha

            log.info("Banco inicializado com sucesso ({} statements executados)", statements.size());

        } catch (SQLException e) {
            throw new PersistenceException("Falha ao inicializar banco de dados", e);
        }
    }

    private void aplicarMigracoes(Connection conn) throws SQLException {
        int versao;
        try (Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("PRAGMA user_version")) {
            rs.next();
            versao = rs.getInt(1);
        }
        if (versao >= 1) {
            return;
        }

        log.info("Aplicando migração 1: CPF não-único e lançamento com pernas opcionais...");
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = OFF");

            st.execute("ALTER TABLE servidor RENAME TO servidor_old");
            st.execute("""
                    CREATE TABLE servidor (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        matricula TEXT NOT NULL UNIQUE,
                        cpf TEXT NOT NULL,
                        ativo INTEGER NOT NULL DEFAULT 1 CHECK (ativo IN (0, 1)),
                        criado_em TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%S','now','localtime'))
                    )
                    """);
            st.execute("""
                    INSERT INTO servidor (id, nome, matricula, cpf, ativo, criado_em)
                    SELECT id, nome, matricula, cpf, ativo, criado_em FROM servidor_old
                    """);
            st.execute("DROP TABLE servidor_old");

            st.execute("ALTER TABLE lancamento RENAME TO lancamento_old");
            st.execute("""
                    CREATE TABLE lancamento (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        servidor_id INTEGER NOT NULL,
                        data TEXT NOT NULL,
                        hora_descida TEXT,
                        hora_entrada TEXT,
                        valor_ida NUMERIC(10,2),
                        hora_saida TEXT,
                        hora_onibus TEXT,
                        valor_volta NUMERIC(10,2),
                        criado_em TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%S','now','localtime')),
                        CONSTRAINT fk_lancamento_servidor FOREIGN KEY (servidor_id)
                            REFERENCES servidor (id) ON DELETE CASCADE ON UPDATE CASCADE,
                        CONSTRAINT uq_lancamento_servidor_data UNIQUE (servidor_id, data),
                        CONSTRAINT ck_lancamento_data_format
                            CHECK (data GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]')
                    )
                    """);
            st.execute("""
                    INSERT INTO lancamento
                    SELECT id, servidor_id, data, hora_descida, hora_entrada, valor_ida,
                        hora_saida, hora_onibus, valor_volta, criado_em
                    FROM lancamento_old
                    """);
            st.execute("DROP TABLE lancamento_old");

            st.execute("PRAGMA foreign_keys = ON");
            st.execute("PRAGMA user_version = 1");
        }
        log.info("Migração 1 aplicada.");
    }

    /**
     * Lê o schema do classpath.
     *
     * @throws PersistenceException se o arquivo não for encontrado ou falhar a leitura
     */
    private String lerSchemaSql() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(SCHEMA_PATH)) {
            if (in == null) {
                throw new PersistenceException(
                        "Arquivo " + SCHEMA_PATH + " não encontrado no classpath");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("Falha ao ler " + SCHEMA_PATH, e);
        }
    }

    /**
     * Divide o conteúdo do arquivo em statements individuais.
     *
     * <p>Remove linhas de comentário ({@code --}) antes de dividir por {@code ;}.
     * Funciona para o schema atual, que não contém {@code ;} dentro de strings
     * literais nem de blocos {@code BEGIN...END}.</p>
     */
    private List<String> dividirStatements(String sql) {
        StringBuilder limpo = new StringBuilder();
        for (String linha : sql.split("\\R")) {
            String trimmed = linha.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            limpo.append(linha).append('\n');
        }

        List<String> statements = new ArrayList<>();
        for (String parte : limpo.toString().split(";")) {
            String comando = parte.trim();
            if (!comando.isEmpty()) {
                statements.add(comando);
            }
        }
        return statements;
    }
}