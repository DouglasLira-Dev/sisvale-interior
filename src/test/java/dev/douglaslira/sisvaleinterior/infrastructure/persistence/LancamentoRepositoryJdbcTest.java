package dev.douglaslira.sisvaleinterior.infrastructure.persistence;

import dev.douglaslira.sisvaleinterior.domain.model.Horario;
import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;
import dev.douglaslira.sisvaleinterior.domain.model.Servidor;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;
import dev.douglaslira.sisvaleinterior.domain.model.Trecho;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do LancamentoRepositoryJdbc")
class LancamentoRepositoryJdbcTest {

    private static final YearMonth MES = YearMonth.of(2026, 9);

    private String url;
    private Connection ancora;
    private ConnectionFactory connectionFactory;
    private ServidorRepositoryJdbc servidorRepository;
    private LancamentoRepositoryJdbc repository;
    private Long servidorId;

    @BeforeEach
    void setup() throws SQLException {
        url = "jdbc:sqlite:file:repo_lancamento_" + UUID.randomUUID()
                + "?mode=memory&cache=shared";
        ancora = DriverManager.getConnection(url);

        connectionFactory = new ConnectionFactory(url);
        new DatabaseInitializer(connectionFactory).inicializar();

        servidorRepository = new ServidorRepositoryJdbc(connectionFactory);
        repository = new LancamentoRepositoryJdbc(connectionFactory);

        // Um servidor padrão é criado para que os lançamentos possam referenciá-lo.
        Servidor servidor = servidorRepository.salvar(
                Servidor.novo("João Silva", "M001", "11144477735"));
        servidorId = servidor.id();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (ancora != null) {
            ancora.close();
        }
    }

    // Helpers
    private Lancamento lancamento(int dia, String descida, String entrada, String valorIda,
                                String saida, String onibus, String valorVolta) {
        Trecho ida = new Trecho(
                Horario.parse(descida),
                Horario.parse(entrada),
                new BigDecimal(valorIda)
        );
        Trecho volta = new Trecho(
                Horario.parse(saida),
                Horario.parse(onibus),
                new BigDecimal(valorVolta)
        );
        return new Lancamento(
                null,
                servidorId,
                LocalDate.of(MES.getYear(), MES.getMonth(), dia),
                List.of(ida, volta)
        );
    }

    private Lancamento lancamentoPadrao(int dia) {
        return lancamento(dia, "07:45", "07:30", "20.00", "17:00", "16:45", "22.00");
    }

    // Inserir
    @Nested
    @DisplayName("Inserir")
    class Inserir {

        @Test
        @DisplayName("deve inserir lançamento com id nulo e retornar com id preenchido")
        void deveInserirComIdNulo() {
            Lancamento salvo = repository.salvar(lancamentoPadrao(15));

            assertThat(salvo.id()).isNotNull().isPositive();
            assertThat(salvo.servidorId()).isEqualTo(servidorId);
            assertThat(salvo.data()).isEqualTo(LocalDate.of(2026, 9, 15));
        }

        @Test
        @DisplayName("dois lançamentos inseridos têm IDs diferentes")
        void doisLancamentosTemIdsDiferentes() {
            Lancamento a = repository.salvar(lancamentoPadrao(15));
            Lancamento b = repository.salvar(lancamentoPadrao(16));

            assertThat(a.id()).isNotEqualTo(b.id());
        }

        @Test
        @DisplayName("servidor_id inexistente lança PersistenceException (FK)")
        void servidorInexistenteLancaExcecao() {
            Trecho ida = new Trecho(
                    Horario.parse("07:45"),
                    Horario.parse("07:30"),
                    new BigDecimal("20.00")
            );
            Trecho volta = new Trecho(
                    Horario.parse("17:00"),
                    Horario.parse("16:45"),
                    new BigDecimal("22.00")
            );
            Lancamento orfao = new Lancamento(
                    null, 999L, LocalDate.of(2026, 9, 15),
                    List.of(ida, volta)
            );

            assertThatThrownBy(() -> repository.salvar(orfao))
                    .isInstanceOf(PersistenceException.class);
        }

        @Test
        @DisplayName("duplicado (servidor_id, data) lança PersistenceException (UNIQUE)")
        void duplicadoLancaExcecao() {
            repository.salvar(lancamentoPadrao(15));

            assertThatThrownBy(() -> repository.salvar(lancamentoPadrao(15)))
                    .isInstanceOf(PersistenceException.class);
        }
    }

    // Buscar por servidor e mês
    @Nested
    @DisplayName("Buscar por servidor e mês")
    class BuscarPorServidorEMes {

        @Test
        @DisplayName("mês com 3 lançamentos devolve 3")
        void mesComTresDevolveTres() {
            repository.salvar(lancamentoPadrao(1));
            repository.salvar(lancamentoPadrao(2));
            repository.salvar(lancamentoPadrao(3));

            List<Lancamento> lista = repository.buscarPorServidorEMes(servidorId, MES);

            assertThat(lista).hasSize(3);
        }

        @Test
        @DisplayName("lançamentos de outro mês são filtrados")
        void filtraOutroMes() {
            repository.salvar(lancamentoPadrao(15));

            Trecho idaOut = new Trecho(
                    Horario.parse("07:45"),
                    Horario.parse("07:30"),
                    new BigDecimal("20.00")
            );
            Trecho voltaOut = new Trecho(
                    Horario.parse("17:00"),
                    Horario.parse("16:45"),
                    new BigDecimal("22.00")
            );
            Lancamento outubro = new Lancamento(
                    null, servidorId, LocalDate.of(2026, 10, 15),
                    List.of(idaOut, voltaOut)
            );
            repository.salvar(outubro);

            List<Lancamento> lista = repository.buscarPorServidorEMes(servidorId, MES);

            assertThat(lista).hasSize(1);
            assertThat(lista.get(0).data()).isEqualTo(LocalDate.of(2026, 9, 15));
        }

        @Test
        @DisplayName("mês sem lançamentos devolve lista vazia")
        void mesSemLancamentosDevolveVazio() {
            List<Lancamento> lista = repository.buscarPorServidorEMes(
                    servidorId, YearMonth.of(2026, 12));

            assertThat(lista).isEmpty();
        }

        @Test
        @DisplayName("lançamentos vêm ordenados por data crescente")
        void ordenadosPorData() {
            repository.salvar(lancamentoPadrao(20));
            repository.salvar(lancamentoPadrao(5));
            repository.salvar(lancamentoPadrao(15));

            List<Lancamento> lista = repository.buscarPorServidorEMes(servidorId, MES);

            assertThat(lista)
                    .extracting(Lancamento::data)
                    .containsExactly(
                            LocalDate.of(2026, 9, 5),
                            LocalDate.of(2026, 9, 15),
                            LocalDate.of(2026, 9, 20)
                    );
        }

        @Test
        @DisplayName("primeiro e último dia do mês são incluídos (BETWEEN)")
        void incluiPrimeiroEUltimoDia() {
            repository.salvar(lancamentoPadrao(1));
            repository.salvar(lancamentoPadrao(30));

            List<Lancamento> lista = repository.buscarPorServidorEMes(servidorId, MES);

            assertThat(lista).hasSize(2);
        }
    }

    // Buscar por servidor e data
    @Nested
    @DisplayName("Buscar por servidor e data")
    class BuscarPorServidorEData {

        @Test
        @DisplayName("data existente devolve Optional com lançamento")
        void dataExistenteDevolveLancamento() {
            repository.salvar(lancamentoPadrao(15));

            Optional<Lancamento> encontrado = repository.buscarPorServidorEData(
                    servidorId, LocalDate.of(2026, 9, 15));

            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().data()).isEqualTo(LocalDate.of(2026, 9, 15));
        }

        @Test
        @DisplayName("data inexistente devolve Optional.empty()")
        void dataInexistenteDevolveVazio() {
            Optional<Lancamento> encontrado = repository.buscarPorServidorEData(
                    servidorId, LocalDate.of(2026, 9, 15));

            assertThat(encontrado).isEmpty();
        }
    }

    // Listar por servidor
    @Nested
    @DisplayName("Listar por servidor")
    class ListarPorServidor {

        @Test
        @DisplayName("devolve todos os lançamentos do servidor, ordenados por data")
        void devolveTodosOrdenados() {
            repository.salvar(lancamentoPadrao(20));
            repository.salvar(lancamentoPadrao(5));

            // Lançamento de outro servidor, não deve aparecer
            Servidor outro = servidorRepository.salvar(
                    Servidor.novo("Maria", "M002", "52998224725"));
            Trecho idaOutro = new Trecho(
                    Horario.parse("07:45"),
                    Horario.parse("07:30"),
                    new BigDecimal("20.00")
            );
            Trecho voltaOutro = new Trecho(
                    Horario.parse("17:00"),
                    Horario.parse("16:45"),
                    new BigDecimal("22.00")
            );
            new LancamentoRepositoryJdbc(connectionFactory).salvar(new Lancamento(
                    null, outro.id(), LocalDate.of(2026, 9, 10),
                    List.of(idaOutro, voltaOutro)
            ));

            List<Lancamento> lista = repository.listarPorServidor(servidorId);

            assertThat(lista).hasSize(2);
            assertThat(lista)
                    .extracting(Lancamento::data)
                    .containsExactly(
                            LocalDate.of(2026, 9, 5),
                            LocalDate.of(2026, 9, 20)
                    );
        }

        @Test
        @DisplayName("servidor sem lançamentos devolve lista vazia")
        void servidorSemLancamentosDevolveVazio() {
            assertThat(repository.listarPorServidor(servidorId)).isEmpty();
        }
    }

    // Remover
    @Nested
    @DisplayName("Remover")
    class Remover {

        @Test
        @DisplayName("após remover, buscarPorId devolve Optional.empty()")
        void aposRemoverDevolveVazio() {
            Lancamento salvo = repository.salvar(lancamentoPadrao(15));

            repository.remover(salvo.id());

            assertThat(repository.buscarPorId(salvo.id())).isEmpty();
        }

        @Test
        @DisplayName("remover id inexistente lança PersistenceException")
        void removerIdInexistenteLancaExcecao() {
            assertThatThrownBy(() -> repository.remover(999L))
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("não encontrado");
        }

        @Test
        @DisplayName("remover id nulo lança IllegalArgumentException")
        void removerIdNuloLancaExcecao() {
            assertThatThrownBy(() -> repository.remover(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // Conversão de tipos
    @Nested
    @DisplayName("Conversão de tipos")
    class ConversaoDeTipos {

    @Test
    @DisplayName("LocalDate e Horario são preservados após ida e volta")
    void dataEHorariosPreservados() {
        Lancamento original = lancamento(
                15, "07:45", "07:30", "20.00", "17:00", "16:45", "22.00");

        Lancamento salvo = repository.salvar(original);
        Lancamento recuperado = repository.buscarPorId(salvo.id()).orElseThrow();

        assertThat(recuperado.data()).isEqualTo(LocalDate.of(2026, 9, 15));
        assertThat(recuperado.trechos()).hasSize(2);

        Trecho ida = recuperado.trechos().get(0);
        assertThat(ida.horaReferencia()).isEqualTo(Horario.parse("07:45"));
        assertThat(ida.horaComparada()).isEqualTo(Horario.parse("07:30"));

        Trecho volta = recuperado.trechos().get(1);
        assertThat(volta.horaReferencia()).isEqualTo(Horario.parse("17:00"));
        assertThat(volta.horaComparada()).isEqualTo(Horario.parse("16:45"));
    }

    @Test
    @DisplayName("BigDecimal com 2 casas é preservado")
    void bigDecimalPreservado() {
        Lancamento original = lancamento(
                15, "07:45", "07:30", "20.55", "17:00", "16:45", "22.75");

        Lancamento salvo = repository.salvar(original);
        Lancamento recuperado = repository.buscarPorId(salvo.id()).orElseThrow();

        assertThat(recuperado.trechos().get(0).valor()).isEqualByComparingTo("20.55");
        assertThat(recuperado.trechos().get(1).valor()).isEqualByComparingTo("22.75");
    }

    @Test
    @DisplayName("hora com minutos é persistida e recuperada como HH:mm")
    void horaComMinutosPreservada() {
        Lancamento original = lancamento(
                15, "07:05", "07:00", "20.00", "17:05", "17:00", "22.00");

        Lancamento salvo = repository.salvar(original);
        Lancamento recuperado = repository.buscarPorId(salvo.id()).orElseThrow();

        // Garante que o zero à esquerda foi preservado
        assertThat(recuperado.trechos().get(0).horaReferencia().formatado())
                .isEqualTo("07:05");
        assertThat(recuperado.trechos().get(1).horaReferencia().formatado())
                .isEqualTo("17:05");
        }
    }

    // ON DELETE CASCADE
    @Nested
    @DisplayName("ON DELETE CASCADE")
    class OnDeleteCascade {

        @Test
        @DisplayName("apagar servidor remove os lançamentos associados")
        void apagarServidorRemoveLancamentos() throws SQLException {
            repository.salvar(lancamentoPadrao(15));
            repository.salvar(lancamentoPadrao(16));

            assertThat(repository.listarPorServidor(servidorId)).hasSize(2);

            try (Connection conn = connectionFactory.getConnection();
                Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM servidor WHERE id = " + servidorId);
            }

            assertThat(repository.listarPorServidor(servidorId)).isEmpty();
        }
    }
}