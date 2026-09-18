package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.dto.ServidorDTO;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.ConnectionFactory;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.DatabaseInitializer;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.ServidorRepositoryJdbc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do CadastrarServidorUseCase")
class CadastrarServidorUseCaseTest {

    private static final String NOME = "João Silva";
    private static final String MATRICULA = "M001";
    private static final String CPF_VALIDO = "11144477735";
    private static final String CPF_VALIDO_MASCARADO = "111.444.777-35";

    private Connection ancora;
    private CadastrarServidorUseCase useCase;

    @BeforeEach
    void setup() throws SQLException {
        String url = "jdbc:sqlite:file:usecase_cadastro_" + UUID.randomUUID()
                + "?mode=memory&cache=shared";
        ancora = DriverManager.getConnection(url);

        ConnectionFactory factory = new ConnectionFactory(url);
        new DatabaseInitializer(factory).inicializar();

        ServidorRepositoryJdbc repository = new ServidorRepositoryJdbc(factory);
        useCase = new CadastrarServidorUseCase(repository);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (ancora != null) {
            ancora.close();
        }
    }

    // Cadastro válido
    @Nested
    @DisplayName("Cadastro válido")
    class CadastroValido {

        @Test
        @DisplayName("deve retornar ServidorDTO com id preenchido")
        void deveRetornarServidorDTOComId() {
            ServidorDTO dto = useCase.executar(NOME, MATRICULA, CPF_VALIDO);

            assertThat(dto.id()).isNotNull().isPositive();
            assertThat(dto.nome()).isEqualTo(NOME);
            assertThat(dto.matricula()).isEqualTo(MATRICULA);
            assertThat(dto.ativo()).isTrue();
        }

        @Test
        @DisplayName("deve mascarar o CPF no DTO retornado (LGPD)")
        void deveMascararCpfNoDTO() {
            ServidorDTO dto = useCase.executar(NOME, MATRICULA, CPF_VALIDO);

            assertThat(dto.cpf()).isEqualTo("***.***.777-35");
            assertThat(dto.cpf()).doesNotContain(CPF_VALIDO);
        }

        @Test
        @DisplayName("deve aceitar CPF com máscara e normalizar internamente")
        void deveAceitarCpfComMascara() {
            ServidorDTO dto = useCase.executar(NOME, MATRICULA, CPF_VALIDO_MASCARADO);

            assertThat(dto.id()).isNotNull();
            assertThat(dto.cpf()).isEqualTo("***.***.777-35");
        }
    }

    // Validação de nome
    @Nested
    @DisplayName("Validação de nome")
    class ValidacaoDeNome {

        @Test
        @DisplayName("nome nulo lança ApplicationException")
        void nomeNuloLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(null, MATRICULA, CPF_VALIDO))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Nome");
        }

        @Test
        @DisplayName("nome vazio lança ApplicationException")
        void nomeVazioLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar("", MATRICULA, CPF_VALIDO))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Nome");
        }

        @Test
        @DisplayName("nome só com espaços lança ApplicationException")
        void nomeSoEspacosLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar("   ", MATRICULA, CPF_VALIDO))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Nome");
        }
    }

    // Validação de matrícula
    @Nested
    @DisplayName("Validação de matrícula")
    class ValidacaoDeMatricula {

        @Test
        @DisplayName("matrícula nula lança ApplicationException")
        void matriculaNulaLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(NOME, null, CPF_VALIDO))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Matrícula");
        }

        @Test
        @DisplayName("matrícula vazia lança ApplicationException")
        void matriculaVaziaLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(NOME, "", CPF_VALIDO))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Matrícula");
        }

        @Test
        @DisplayName("matrícula só com espaços lança ApplicationException")
        void matriculaSoEspacosLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(NOME, "   ", CPF_VALIDO))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Matrícula");
        }
    }

    // Validação de CPF
    @Nested
    @DisplayName("Validação de CPF")
    class ValidacaoDeCpf {

        @Test
        @DisplayName("CPF nulo lança ApplicationException")
        void cpfNuloLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(NOME, MATRICULA, null))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("CPF");
        }

        @Test
        @DisplayName("CPF vazio lança ApplicationException")
        void cpfVazioLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(NOME, MATRICULA, ""))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("CPF");
        }

        @Test
        @DisplayName("CPF inválido lança ApplicationException (convertida)")
        void cpfInvalidoLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(NOME, MATRICULA, "11111111111"))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("CPF");
        }

        @Test
        @DisplayName("CPF com dígito verificador errado lança ApplicationException")
        void cpfComDvErradoLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(NOME, MATRICULA, "11144477736"))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("CPF");
        }
    }

    // Duplicidade
    @Nested
    @DisplayName("Duplicidade")
    class Duplicidade {

        @Test
        @DisplayName("matrícula duplicada lança ApplicationException")
        void matriculaDuplicadaLancaExcecao() {
            useCase.executar(NOME, MATRICULA, CPF_VALIDO);

            assertThatThrownBy(() ->
                    useCase.executar("Maria Souza", MATRICULA, "52998224725"))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Matrícula já cadastrada");
        }

        @Test
        @DisplayName("CPF duplicado lança ApplicationException")
        void cpfDuplicadoLancaExcecao() {
            useCase.executar(NOME, MATRICULA, CPF_VALIDO);

            assertThatThrownBy(() ->
                    useCase.executar("Maria Souza", "M002", CPF_VALIDO))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("CPF já cadastrado");
        }

        @Test
        @DisplayName("CPF duplicado com máscara também é detectado")
        void cpfDuplicadoComMascaraEDetectado() {
            useCase.executar(NOME, MATRICULA, CPF_VALIDO);

            assertThatThrownBy(() ->
                    useCase.executar("Maria Souza", "M002", CPF_VALIDO_MASCARADO))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("CPF já cadastrado");
        }
    }
}