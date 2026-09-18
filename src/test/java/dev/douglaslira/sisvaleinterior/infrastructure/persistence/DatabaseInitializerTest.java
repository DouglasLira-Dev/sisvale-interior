package dev.douglaslira.sisvaleinterior.infrastructure.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do DatabaseInitializer")
class DatabaseInitializerTest {

    private static String url;
    private static Connection ancora;

    private ConnectionFactory connectionFactory;
    private DatabaseInitializer initializer;

    @BeforeAll
    static void antesDeTudo() throws SQLException {
        url = "jdbc:sqlite:file:testdb_" + UUID.randomUUID() + "?mode=memory&cache=shared";
        ancora = DriverManager.getConnection(url);
    }

    @AfterAll
    static void depoisDeTudo() throws SQLException {
        if (ancora != null) {
            ancora.close();
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        limparBanco();
        connectionFactory = new ConnectionFactory(url);
        initializer = new DatabaseInitializer(connectionFactory);
    }

    /**
     * Apaga as tabelas na ordem correta (filhas antes das mães) para respeitar
     * a foreign key.
     */
    private void limparBanco() throws SQLException {
        try (Statement st = ancora.createStatement()) {
            st.execute("DROP TABLE IF EXISTS trecho");
            st.execute("DROP TABLE IF EXISTS lancamento");
            st.execute("DROP TABLE IF EXISTS servidor");
        }
    }

    // Inicialização
    @Nested
    @DisplayName("Inicialização")
    class Inicializacao {

        @Test
        @DisplayName("deve criar as tabelas servidor, lancamento e trecho")
        void deveCriarTabelas() throws SQLException {
            initializer.inicializar();

            List<String> tabelas = listarTabelas();
            assertThat(tabelas).contains("servidor", "lancamento", "trecho");
        }

        @Test
        @DisplayName("deve ser idempotente — segunda chamada não quebra")
        void deveSerIdempotente() throws SQLException {
            initializer.inicializar();
            initializer.inicializar();   // não deve lançar

            List<String> tabelas = listarTabelas();
            assertThat(tabelas).contains("servidor", "lancamento", "trecho");
        }

        @Test
        @DisplayName("deve criar os índices esperados")
        void deveCriarIndices() throws SQLException {
            initializer.inicializar();

            List<String> indices = listarIndices();
            assertThat(indices).contains(
                    "idx_lancamento_servidor_data",
                    "idx_trecho_lancamento"
            );
        }

        private List<String> listarTabelas() throws SQLException {
            try (Connection conn = connectionFactory.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")) {
                List<String> nomes = new ArrayList<>();
                while (rs.next()) {
                    nomes.add(rs.getString("name"));
                }
                return nomes;
            }
        }

        private List<String> listarIndices() throws SQLException {
            try (Connection conn = connectionFactory.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT name FROM sqlite_master WHERE type='index' ORDER BY name")) {
                List<String> nomes = new ArrayList<>();
                while (rs.next()) {
                    nomes.add(rs.getString("name"));
                }
                return nomes;
            }
        }
    }

    // Estrutura criada
    @Nested
    @DisplayName("Estrutura criada")
    class EstruturaCriada {

        @Test
        @DisplayName("tabela servidor tem as colunas corretas")
        void tabelaServidorTemColunasCorretas() throws SQLException {
            initializer.inicializar();

            List<String> colunas = listarColunas("servidor");
            assertThat(colunas).containsExactly(
                    "id", "nome", "matricula", "cpf", "ativo", "criado_em");
        }

        @Test
        @DisplayName("tabela lancamento tem as colunas corretas (sem horários/valores)")
        void tabelaLancamentoTemColunasCorretas() throws SQLException {
            initializer.inicializar();

            List<String> colunas = listarColunas("lancamento");
            assertThat(colunas).containsExactly(
                    "id", "servidor_id", "data", "criado_em");
        }

        @Test
        @DisplayName("tabela lancamento NÃO tem as colunas antigas de horário/valor")
        void tabelaLancamentoNaoTemColunasAntigas() throws SQLException {
            initializer.inicializar();

            List<String> colunas = listarColunas("lancamento");
            assertThat(colunas).doesNotContain(
                    "hora_descida", "hora_entrada", "valor_ida",
                    "hora_saida", "hora_onibus", "valor_volta");
        }

        @Test
        @DisplayName("tabela trecho tem as colunas corretas")
        void tabelaTrechoTemColunasCorretas() throws SQLException {
            initializer.inicializar();

            List<String> colunas = listarColunas("trecho");
            assertThat(colunas).containsExactly(
                    "id", "lancamento_id", "ordem",
                    "hora_referencia", "hora_comparada", "valor");
        }

        private List<String> listarColunas(String tabela) throws SQLException {
            try (Connection conn = connectionFactory.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("PRAGMA table_info(" + tabela + ")")) {
                List<String> nomes = new ArrayList<>();
                while (rs.next()) {
                    nomes.add(rs.getString("name"));
                }
                return nomes;
            }
        }
    }

    // Constraints do schema
    @Nested
    @DisplayName("Constraints do schema")
    class ConstraintsDoSchema {

        @BeforeEach
        void garantirSchema() {
            initializer.inicializar();
        }

        @Test
        @DisplayName("UNIQUE em servidor.matricula rejeita duplicado")
        void uniqueMatriculaRejeitaDuplicado() throws SQLException {
            inserirServidor("João", "M001", "11144477735");

            assertThatThrownBy(() -> inserirServidor("Maria", "M001", "52998224725"))
                    .isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("UNIQUE(servidor_id, data) em lancamento rejeita duplicado")
        void uniqueServidorDataRejeitaDuplicado() throws SQLException {
            long servidorId = inserirServidor("João", "M001", "11144477735");
            inserirLancamento(servidorId, "2026-09-15");

            assertThatThrownBy(() -> inserirLancamento(servidorId, "2026-09-15"))
                    .isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("CHECK do formato de data rejeita formato DD/MM/YYYY")
        void checkDataRejeitaFormatoBr() throws SQLException {
            long servidorId = inserirServidor("João", "M001", "11144477735");

            assertThatThrownBy(() -> inserirLancamento(servidorId, "15/09/2026"))
                    .isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("FOREIGN KEY rejeita lancamento com servidor inexistente")
        void foreignKeyRejeitaServidorInexistente() {
            assertThatThrownBy(() -> inserirLancamento(999L, "2026-09-15"))
                    .isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("FOREIGN KEY rejeita trecho com lançamento inexistente")
        void foreignKeyRejeitaTrechoComLancamentoInexistente() {
            assertThatThrownBy(() -> inserirTrecho(999L, 1))
                    .isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("UNIQUE(lancamento_id, ordem) rejeita trecho duplicado")
        void uniqueOrdemRejeitaTrechoDuplicado() throws SQLException {
            long servidorId = inserirServidor("João", "M001", "11144477735");
            long lancamentoId = inserirLancamento(servidorId, "2026-09-15");

            inserirTrecho(lancamentoId, 1);

            assertThatThrownBy(() -> inserirTrecho(lancamentoId, 1))
                    .isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("ON DELETE CASCADE remove lançamentos ao apagar servidor")
        void onDeleteCascadeRemoveLancamentos() throws SQLException {
            long servidorId = inserirServidor("João", "M001", "11144477735");
            inserirLancamento(servidorId, "2026-09-15");
            inserirLancamento(servidorId, "2026-09-16");

            assertThat(contarLancamentos()).isEqualTo(2);

            try (Connection conn = connectionFactory.getConnection();
                Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM servidor WHERE id = " + servidorId);
            }

            assertThat(contarLancamentos()).isZero();
        }

        @Test
        @DisplayName("ON DELETE CASCADE remove trechos ao apagar lançamento")
        void onDeleteCascadeRemoveTrechos() throws SQLException {
            long servidorId = inserirServidor("João", "M001", "11144477735");
            long lancamentoId = inserirLancamento(servidorId, "2026-09-15");

            inserirTrecho(lancamentoId, 1);
            inserirTrecho(lancamentoId, 2);

            assertThat(contarTrechos()).isEqualTo(2);

            try (Connection conn = connectionFactory.getConnection();
                Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM lancamento WHERE id = " + lancamentoId);
            }

            assertThat(contarTrechos()).isZero();
        }

        // ---------- helpers ----------
        private long inserirServidor(String nome, String matricula, String cpf) throws SQLException {
            try (Connection conn = connectionFactory.getConnection();
                Statement st = conn.createStatement()) {
                st.executeUpdate(
                        "INSERT INTO servidor (nome, matricula, cpf, ativo) VALUES ('"
                                + nome + "', '" + matricula + "', '" + cpf + "', 1)");
                try (ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        }

        private long inserirLancamento(long servidorId, String data) throws SQLException {
            try (Connection conn = connectionFactory.getConnection();
                Statement st = conn.createStatement()) {
                st.executeUpdate(
                        "INSERT INTO lancamento (servidor_id, data) VALUES ("
                                + servidorId + ", '" + data + "')");
                try (ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        }

        private void inserirTrecho(long lancamentoId, int ordem) throws SQLException {
            try (Connection conn = connectionFactory.getConnection();
                Statement st = conn.createStatement()) {
                st.executeUpdate(
                        "INSERT INTO trecho (lancamento_id, ordem, hora_referencia, hora_comparada, valor) "
                                + "VALUES (" + lancamentoId + ", " + ordem
                                + ", '07:45', '07:30', 20.00)");
            }
        }

        private long contarLancamentos() throws SQLException {
            try (Connection conn = connectionFactory.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM lancamento")) {
                rs.next();
                return rs.getLong(1);
            }
        }

        private long contarTrechos() throws SQLException {
            try (Connection conn = connectionFactory.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM trecho")) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}