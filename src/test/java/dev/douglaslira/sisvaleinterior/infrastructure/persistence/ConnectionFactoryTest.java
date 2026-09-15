package dev.douglaslira.sisvaleinterior.infrastructure.persistence;

import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do ConnectionFactory")
class ConnectionFactoryTest {

    /**
     * Gera uma URL única de banco em memória compartilhado por classe de teste.
     * Cada teste tem seu próprio banco, evitando interferência entre testes.
     */
    private static String urlMemoria() {
        return "jdbc:sqlite:file:memdb_" + UUID.randomUUID() + "?mode=memory&cache=shared";
    }
    
    // Criação de conexão
    @Nested
    @DisplayName("Criação de conexão")
    class CriacaoDeConexao {

        @Test
        @DisplayName("deve abrir uma conexão válida")
        void deveAbrirConexaoValida() throws SQLException {
            ConnectionFactory factory = new ConnectionFactory(urlMemoria());

            try (Connection conn = factory.getConnection()) {
                assertThat(conn).isNotNull();
                assertThat(conn.isClosed()).isFalse();
            }
        }

        @Test
        @DisplayName("deve permitir conexões independentes simultâneas")
        void devePermitirConexoesIndependentes() throws SQLException {
            // Para compartilhar o mesmo banco em memória, precisamos de uma conexão "âncora"
            // que fica viva enquanto as demais são abertas e fechadas.
            String url = urlMemoria();
            ConnectionFactory factory = new ConnectionFactory(url);

            try (Connection ancora = DriverManager.getConnection(url);
                Connection a = factory.getConnection();
                Connection b = factory.getConnection()) {

                assertThat(a).isNotSameAs(b);
                assertThat(a.isClosed()).isFalse();
                assertThat(b.isClosed()).isFalse();
            }
        }

        @Test
        @DisplayName("conexões fechadas em try-with-resources ficam realmente fechadas")
        void conexoesFechamAoFinal() throws SQLException {
            ConnectionFactory factory = new ConnectionFactory(urlMemoria());

            Connection conn;
            try (Connection c = factory.getConnection()) {
                conn = c;
            }
            assertThat(conn.isClosed()).isTrue();
        }
    }

    // PRAGMA foreign_keys
    @Nested
    @DisplayName("PRAGMA foreign_keys")
    class PragmaForeignKeys {

        @Test
        @DisplayName("deve ativar foreign_keys em toda conexão aberta")
        void deveAtivarForeignKeys() throws SQLException {
            ConnectionFactory factory = new ConnectionFactory(urlMemoria());

            try (Connection conn = factory.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("PRAGMA foreign_keys")) {

                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("cada nova conexão vem com foreign_keys ativado")
        void cadaConexaoVemComForeignKeysAtivo() throws SQLException {
            String url = urlMemoria();
            ConnectionFactory factory = new ConnectionFactory(url);

            try (Connection ancora = DriverManager.getConnection(url);
                Connection c1 = factory.getConnection();
                Connection c2 = factory.getConnection()) {

                assertThat(lerForeignKeys(c1)).isEqualTo(1);
                assertThat(lerForeignKeys(c2)).isEqualTo(1);
            }
        }

        private int lerForeignKeys(Connection conn) throws SQLException {
            try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("PRAGMA foreign_keys")) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // Validação de URL
    @Nested
    @DisplayName("Validação de URL")
    class ValidacaoDeUrl {

        @Test
        @DisplayName("deve rejeitar URL nula")
        void deveRejeitarUrlNula() {
            assertThatThrownBy(() -> new ConnectionFactory((String) null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("URL");
        }

        @Test
        @DisplayName("deve rejeitar URL vazia")
        void deveRejeitarUrlVazia() {
            assertThatThrownBy(() -> new ConnectionFactory(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("URL");
        }

        @Test
        @DisplayName("deve rejeitar URL só com espaços")
        void deveRejeitarUrlSoEspacos() {
            assertThatThrownBy(() -> new ConnectionFactory("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("URL");
        }
    }

    // Falhas de conexão
    @Nested
    @DisplayName("Falhas de conexão")
    class FalhasDeConexao {

        @Test
        @DisplayName("deve lançar PersistenceException para URL inválida")
        void deveLancarPersistenceExceptionParaUrlInvalida() {
            ConnectionFactory factory =
                    new ConnectionFactory("jdbc:sqlite:/caminho/que/nao/existe/arquivo.db");

            assertThatThrownBy(factory::getConnection)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("Falha ao abrir conexão");
        }

        @Test
        @DisplayName("deve preservar a causa (SQLException) na PersistenceException")
        void devePreservarCausa() {
            ConnectionFactory factory =
                    new ConnectionFactory("jdbc:sqlite:/caminho/que/nao/existe/arquivo.db");

            assertThatThrownBy(factory::getConnection)
                    .isInstanceOf(PersistenceException.class)
                    .hasCauseInstanceOf(SQLException.class);
        }
    }
}