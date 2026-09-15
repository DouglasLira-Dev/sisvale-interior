package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.dto.LancamentoDTO;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.domain.model.Servidor;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.ConnectionFactory;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.DatabaseInitializer;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.LancamentoRepositoryJdbc;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.ServidorRepositoryJdbc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do RegistrarLancamentoUseCase")
class RegistrarLancamentoUseCaseTest {

    private static final LocalDate DATA = LocalDate.of(2026, 9, 15);
    private static final LocalTime DESCIDA = LocalTime.of(7, 45);
    private static final LocalTime ENTRADA = LocalTime.of(7, 30);
    private static final LocalTime SAIDA = LocalTime.of(17, 0);
    private static final LocalTime ONIBUS = LocalTime.of(16, 45);
    private static final BigDecimal VALOR_IDA = new BigDecimal("20.00");
    private static final BigDecimal VALOR_VOLTA = new BigDecimal("22.00");

    private Connection ancora;
    private ServidorRepositoryJdbc servidorRepository;
    private RegistrarLancamentoUseCase useCase;
    private Long servidorId;

    @BeforeEach
    void setup() throws SQLException {
        String url = "jdbc:sqlite:file:usecase_registrar_" + UUID.randomUUID()
                + "?mode=memory&cache=shared";
        ancora = DriverManager.getConnection(url);

        ConnectionFactory factory = new ConnectionFactory(url);
        new DatabaseInitializer(factory).inicializar();

        servidorRepository = new ServidorRepositoryJdbc(factory);
        LancamentoRepositoryJdbc lancamentoRepository = new LancamentoRepositoryJdbc(factory);

        useCase = new RegistrarLancamentoUseCase(lancamentoRepository, servidorRepository);

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

    // Registro válido
    @Nested
    @DisplayName("Registro válido")
    class RegistroValido {

        @Test
        @DisplayName("deve retornar LancamentoDTO com id preenchido")
        void deveRetornarLancamentoDTOComId() {
            LancamentoDTO dto = useCase.executar(
                    servidorId, DATA, DESCIDA, ENTRADA, VALOR_IDA, SAIDA, ONIBUS, VALOR_VOLTA);

            assertThat(dto.id()).isNotNull().isPositive();
            assertThat(dto.servidorId()).isEqualTo(servidorId);
            assertThat(dto.data()).isEqualTo(DATA);
            assertThat(dto.horaDescida()).isEqualTo(DESCIDA);
            assertThat(dto.horaEntrada()).isEqualTo(ENTRADA);
            assertThat(dto.valorIda()).isEqualByComparingTo(VALOR_IDA);
            assertThat(dto.horaSaida()).isEqualTo(SAIDA);
            assertThat(dto.horaOnibus()).isEqualTo(ONIBUS);
            assertThat(dto.valorVolta()).isEqualByComparingTo(VALOR_VOLTA);
        }

        @Test
        @DisplayName("deve aceitar horário com segundos zero (normalizado para HH:mm)")
        void deveAceitarHorarioComSegundosZero() {
            LancamentoDTO dto = useCase.executar(
                    servidorId, DATA,
                    LocalTime.of(7, 45, 0), LocalTime.of(7, 30, 0), VALOR_IDA,
                    LocalTime.of(17, 0, 0), LocalTime.of(16, 45, 0), VALOR_VOLTA);

            assertThat(dto.id()).isNotNull();
            assertThat(dto.horaDescida()).isEqualTo(LocalTime.of(7, 45));
        }
    }

    // Validação de entrada
    @Nested
    @DisplayName("Validação de entrada")
    class ValidacaoDeEntrada {

        @Test
        @DisplayName("servidorId nulo lança ApplicationException")
        void servidorIdNuloLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(
                    null, DATA, DESCIDA, ENTRADA, VALOR_IDA, SAIDA, ONIBUS, VALOR_VOLTA))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Servidor");
        }

        @Test
        @DisplayName("data nula lança ApplicationException")
        void dataNulaLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(
                    servidorId, null, DESCIDA, ENTRADA, VALOR_IDA, SAIDA, ONIBUS, VALOR_VOLTA))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Data");
        }

        @Test
        @DisplayName("hora de descida nula lança ApplicationException")
        void horaDescidaNulaLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(
                    servidorId, DATA, null, ENTRADA, VALOR_IDA, SAIDA, ONIBUS, VALOR_VOLTA))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Horário");
        }

        @Test
        @DisplayName("valor de ida negativo lança ApplicationException")
        void valorIdaNegativoLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(
                    servidorId, DATA, DESCIDA, ENTRADA, new BigDecimal("-0.01"),
                    SAIDA, ONIBUS, VALOR_VOLTA))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        @DisplayName("valor de ida nulo lança ApplicationException")
        void valorIdaNuloLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(
                    servidorId, DATA, DESCIDA, ENTRADA, null, SAIDA, ONIBUS, VALOR_VOLTA))
                    .isInstanceOf(ApplicationException.class);
        }
    }

    // Servidor inexistente
    @Nested
    @DisplayName("Servidor inexistente")
    class ServidorInexistente {

        @Test
        @DisplayName("servidorId inexistente lança ApplicationException")
        void servidorIdInexistenteLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(
                    999L, DATA, DESCIDA, ENTRADA, VALOR_IDA, SAIDA, ONIBUS, VALOR_VOLTA))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Servidor não encontrado");
        }
    }
    
    // Duplicidade
    @Nested
    @DisplayName("Duplicidade")
    class Duplicidade {

        @Test
        @DisplayName("lançamento duplicado lança ApplicationException")
        void lancamentoDuplicadoLancaExcecao() {
            useCase.executar(
                    servidorId, DATA, DESCIDA, ENTRADA, VALOR_IDA, SAIDA, ONIBUS, VALOR_VOLTA);

            assertThatThrownBy(() -> useCase.executar(
                    servidorId, DATA, DESCIDA, ENTRADA, VALOR_IDA, SAIDA, ONIBUS, VALOR_VOLTA))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Já existe lançamento");
        }

        @Test
        @DisplayName("mesma data em outro servidor é permitido")
        void mesmaDataOutroServidorEhPermitido() {
            Servidor outro = servidorRepository.salvar(
                    Servidor.novo("Maria", "M002", "52998224725"));

            useCase.executar(
                    servidorId, DATA, DESCIDA, ENTRADA, VALOR_IDA, SAIDA, ONIBUS, VALOR_VOLTA);
            LancamentoDTO dto = useCase.executar(
                    outro.id(), DATA, DESCIDA, ENTRADA, VALOR_IDA, SAIDA, ONIBUS, VALOR_VOLTA);

            assertThat(dto.id()).isNotNull();
            assertThat(dto.servidorId()).isEqualTo(outro.id());
        }
    }

    // Construtor
    @Nested
    @DisplayName("Construtor")
    class Construtor {

        @Test
        @DisplayName("lancamentoRepository nulo lança IllegalArgumentException")
        void lancamentoRepositoryNulo() {
            assertThatThrownBy(() -> new RegistrarLancamentoUseCase(null, servidorRepository))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("servidorRepository nulo lança IllegalArgumentException")
        void servidorRepositoryNulo() {
            assertThatThrownBy(() -> new RegistrarLancamentoUseCase(
                    new LancamentoRepositoryJdbc(new ConnectionFactory("jdbc:sqlite::memory:")),
                    null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}