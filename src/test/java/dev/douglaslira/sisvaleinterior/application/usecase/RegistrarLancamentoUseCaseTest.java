package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.dto.LancamentoDTO;
import dev.douglaslira.sisvaleinterior.application.dto.TrechoDTO;
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
import java.util.List;
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

    
    // Helpers
    

    private TrechoDTO trechoIda() {
        return new TrechoDTO(DESCIDA, ENTRADA, VALOR_IDA);
    }

    private TrechoDTO trechoVolta() {
        return new TrechoDTO(SAIDA, ONIBUS, VALOR_VOLTA);
    }

    private List<TrechoDTO> trechosValidos() {
        return List.of(trechoIda(), trechoVolta());
    }

    // Registro válido
    @Nested
    @DisplayName("Registro válido")
    class RegistroValido {

        @Test
        @DisplayName("deve retornar LancamentoDTO com id preenchido")
        void deveRetornarLancamentoDTOComId() {
            LancamentoDTO dto = useCase.executar(servidorId, DATA, trechosValidos());

            assertThat(dto.id()).isNotNull().isPositive();
            assertThat(dto.servidorId()).isEqualTo(servidorId);
            assertThat(dto.data()).isEqualTo(DATA);
            assertThat(dto.trechos()).hasSize(2);

            TrechoDTO ida = dto.trechos().get(0);
            assertThat(ida.horaReferencia()).isEqualTo(DESCIDA);
            assertThat(ida.horaComparada()).isEqualTo(ENTRADA);
            assertThat(ida.valor()).isEqualByComparingTo(VALOR_IDA);

            TrechoDTO volta = dto.trechos().get(1);
            assertThat(volta.horaReferencia()).isEqualTo(SAIDA);
            assertThat(volta.horaComparada()).isEqualTo(ONIBUS);
            assertThat(volta.valor()).isEqualByComparingTo(VALOR_VOLTA);
        }

        @Test
        @DisplayName("deve aceitar horário com segundos zero (normalizado para HH:mm)")
        void deveAceitarHorarioComSegundosZero() {
            List<TrechoDTO> trechos = List.of(
                    new TrechoDTO(LocalTime.of(7, 45, 0), LocalTime.of(7, 30, 0), VALOR_IDA),
                    new TrechoDTO(LocalTime.of(17, 0, 0), LocalTime.of(16, 45, 0), VALOR_VOLTA)
            );

            LancamentoDTO dto = useCase.executar(servidorId, DATA, trechos);

            assertThat(dto.id()).isNotNull();
            assertThat(dto.trechos().get(0).horaReferencia()).isEqualTo(LocalTime.of(7, 45));
        }

        @Test
        @DisplayName("deve aceitar lançamento com 1 trecho")
        void deveAceitarLancamentoComUmTrecho() {
            LancamentoDTO dto = useCase.executar(
                    servidorId, DATA, List.of(trechoIda()));

            assertThat(dto.id()).isNotNull();
            assertThat(dto.trechos()).hasSize(1);
        }

        @Test
        @DisplayName("deve aceitar lançamento com 3 trechos")
        void deveAceitarLancamentoComTresTrechos() {
            List<TrechoDTO> trechos = List.of(
                    trechoIda(),
                    new TrechoDTO(LocalTime.of(12, 0), LocalTime.of(11, 45), new BigDecimal("15.00")),
                    trechoVolta()
            );

            LancamentoDTO dto = useCase.executar(servidorId, DATA, trechos);

            assertThat(dto.id()).isNotNull();
            assertThat(dto.trechos()).hasSize(3);
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
                    null, DATA, trechosValidos()))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Servidor");
        }

        @Test
        @DisplayName("data nula lança ApplicationException")
        void dataNulaLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(
                    servidorId, null, trechosValidos()))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Data");
        }

        @Test
        @DisplayName("lista de trechos nula lança ApplicationException")
        void trechosNulosLancamExcecao() {
            assertThatThrownBy(() -> useCase.executar(
                    servidorId, DATA, null))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("trecho");
        }

        @Test
        @DisplayName("lista de trechos vazia lança ApplicationException")
        void trechosVaziosLancamExcecao() {
            assertThatThrownBy(() -> useCase.executar(
                    servidorId, DATA, List.of()))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("trecho");
        }

        @Test
        @DisplayName("trecho com hora de referência nula lança ApplicationException")
        void trechoComHoraReferenciaNulaLancaExcecao() {
            List<TrechoDTO> trechos = List.of(
                    new TrechoDTO(null, ENTRADA, VALOR_IDA)
            );

            assertThatThrownBy(() -> useCase.executar(servidorId, DATA, trechos))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        @DisplayName("trecho com valor negativo lança ApplicationException")
        void trechoComValorNegativoLancaExcecao() {
            List<TrechoDTO> trechos = List.of(
                    new TrechoDTO(DESCIDA, ENTRADA, new BigDecimal("-0.01"))
            );

            assertThatThrownBy(() -> useCase.executar(servidorId, DATA, trechos))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        @DisplayName("trecho com valor nulo lança ApplicationException")
        void trechoComValorNuloLancaExcecao() {
            List<TrechoDTO> trechos = List.of(
                    new TrechoDTO(DESCIDA, ENTRADA, null)
            );

            assertThatThrownBy(() -> useCase.executar(servidorId, DATA, trechos))
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
                    999L, DATA, trechosValidos()))
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
            useCase.executar(servidorId, DATA, trechosValidos());

            assertThatThrownBy(() -> useCase.executar(
                    servidorId, DATA, trechosValidos()))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Já existe lançamento");
        }

        @Test
        @DisplayName("mesma data em outro servidor é permitido")
        void mesmaDataOutroServidorEhPermitido() {
            Servidor outro = servidorRepository.salvar(
                    Servidor.novo("Maria", "M002", "52998224725"));

            useCase.executar(servidorId, DATA, trechosValidos());
            LancamentoDTO dto = useCase.executar(outro.id(), DATA, trechosValidos());

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