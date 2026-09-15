package dev.douglaslira.sisvaleinterior.infrastructure.persistence;

import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
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

    /**
     * Executa o schema do banco. Idempotente — pode ser chamado sempre.
     *
     * @throws PersistenceException se o schema não for encontrado ou falhar a execução
     */
    public void inicializar() {
        log.info("Inicializando banco de dados...");

        String sql = lerSchemaSql();
        List<String> statements = dividirStatements(sql);

        try (Connection conn = connectionFactory.getConnection();
             Statement st = conn.createStatement()) {

            for (String comando : statements) {
                st.execute(comando);
            }

            log.info("Banco inicializado com sucesso ({} statements executados)", statements.size());

        } catch (SQLException e) {
            throw new PersistenceException("Falha ao inicializar banco de dados", e);
        }
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