package dev.douglaslira.sisvaleinterior.infrastructure.persistence;

import dev.douglaslira.sisvaleinterior.infrastructure.config.AppConfig;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Fábrica de conexões JDBC com SQLite.
 *
 * <p>Centraliza a criação de conexões. Nenhuma outra classe do sistema
 * chama {@link DriverManager} diretamente.</p>
 *
 * <p><strong>Sem pool de conexões por decisão consciente:</strong> SQLite é
 * embarcado, sem latência de rede, e a aplicação é desktop single-user.
 * Abrir uma conexão nova a cada operação é suficiente e mais simples.
 * Reavaliar se a aplicação ganhar concorrência real.</p>
 *
 * <p><strong>Foreign keys:</strong> o SQLite desabilita foreign keys por
 * padrão. Este factory executa {@code PRAGMA foreign_keys = ON} em toda
 * conexão aberta — sem isso, o {@code ON DELETE CASCADE} do schema não
 * funciona.</p>
 *
 * <p><strong>Exceções:</strong> este é o único ponto do sistema que captura
 * {@link SQLException} na abertura de conexão. Falhas são sinalizadas como
 * {@link PersistenceException}, mantendo o resto do sistema livre de JDBC.</p>
 *
 * <p><strong>Para testes:</strong> use a URL
 * {@code jdbc:sqlite:file:memdb?mode=memory&cache=shared} para compartilhar
 * um banco em memória entre conexões abertas. Mantenha uma conexão "âncora"
 * aberta enquanto o teste durar — o banco é destruído quando a última
 * conexão fecha.</p>
 */
public final class ConnectionFactory {

    private static final String PRAGMA_FOREIGN_KEYS = "PRAGMA foreign_keys = ON";

    private final String url;

    /**
     * Cria a fábrica com a URL JDBC informada.
     *
     * @param url URL JDBC (não pode ser nula ou vazia)
     * @throws IllegalArgumentException se {@code url} for nula ou em branco
     */
    public ConnectionFactory(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL do banco é obrigatória");
        }
        this.url = url.trim();
    }

    /**
     * Cria a fábrica usando a URL configurada em {@code application.properties}
     * ({@code db.url}).
     */
    public ConnectionFactory() {
        this(AppConfig.dbUrl());
    }

    /**
     * Abre uma nova conexão JDBC com o banco e ativa foreign keys.
     *
     * <p>Quem chama é responsável por fechar a conexão — use
     * {@code try-with-resources}.</p>
     *
     * @return conexão aberta com {@code PRAGMA foreign_keys = ON} aplicado
     * @throws PersistenceException se a conexão ou o PRAGMA falharem
     */
    public Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(url);
            ativarForeignKeys(conn);
            return conn;
        } catch (SQLException e) {
            throw new PersistenceException(
                    "Falha ao abrir conexão: " + url, e);
        }
    }

    private static void ativarForeignKeys(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute(PRAGMA_FOREIGN_KEYS);
        }
    }
}