package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.dto.ServidorDTO;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do ListarServidoresUseCase")
class ListarServidoresUseCaseTest {

    private Connection ancora;
    private ServidorRepositoryJdbc servidorRepository;
    private ListarServidoresUseCase useCase;

    @BeforeEach
    void setup() throws SQLException {
        String url = "jdbc:sqlite:file:usecase_listar_" + UUID.randomUUID()
                + "?mode=memory&cache=shared";
        ancora = DriverManager.getConnection(url);

        ConnectionFactory factory = new ConnectionFactory(url);
        new DatabaseInitializer(factory).inicializar();

        servidorRepository = new ServidorRepositoryJdbc(factory);
        useCase = new ListarServidoresUseCase(servidorRepository);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (ancora != null) {
            ancora.close();
        }
    }

    private void criarServidor(String nome, String matricula, String cpf, boolean ativo) {
        servidorRepository.salvar(
                new dev.douglaslira.sisvaleinterior.domain.model.Servidor(
                        null, nome, matricula, cpf, ativo));
    }

    // executar() — todos
    @Nested
    @DisplayName("executar — todos")
    class ExecutarTodos {

        @Test
        @DisplayName("banco vazio devolve lista vazia")
        void bancoVazioDevolveListaVazia() {
            List<ServidorDTO> lista = useCase.executar();

            assertThat(lista).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("com 3 servidores devolve 3 DTOs ordenados por nome")
        void tresServidoresOrdenadosPorNome() {
            criarServidor("Carlos", "M003", "52998224725", true);
            criarServidor("Ana", "M001", "11144477735", true);
            criarServidor("Bruno", "M002", "12345678909", true);

            List<ServidorDTO> lista = useCase.executar();

            assertThat(lista).hasSize(3);
            assertThat(lista).extracting(ServidorDTO::nome)
                    .containsExactly("Ana", "Bruno", "Carlos");
        }

        @Test
        @DisplayName("inclui servidores inativos")
        void incluiInativos() {
            criarServidor("Ana", "M001", "11144477735", true);
            criarServidor("Bruno", "M002", "52998224725", false);

            List<ServidorDTO> lista = useCase.executar();

            assertThat(lista).hasSize(2);
            assertThat(lista).extracting(ServidorDTO::ativo)
                    .containsExactlyInAnyOrder(true, false);
        }

        @Test
        @DisplayName("CPF vem mascarado nos DTOs (LGPD)")
        void cpfVemMascarado() {
            criarServidor("Ana", "M001", "11144477735", true);

            List<ServidorDTO> lista = useCase.executar();

            assertThat(lista.get(0).cpf()).isEqualTo("***.***.777-35");
            assertThat(lista.get(0).cpf()).doesNotContain("11144477735");
        }

        @Test
        @DisplayName("lista retornada é imutável")
        void listaImutavel() {
            criarServidor("Ana", "M001", "11144477735", true);

            List<ServidorDTO> lista = useCase.executar();

            assertThatThrownBy(() -> lista.add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // executarApenasAtivos()
    @Nested
    @DisplayName("executarApenasAtivos")
    class ExecutarApenasAtivos {

        @Test
        @DisplayName("filtra apenas ativos")
        void filtraApenasAtivos() {
            criarServidor("Ana", "M001", "11144477735", true);
            criarServidor("Bruno", "M002", "52998224725", false);
            criarServidor("Carlos", "M003", "12345678909", true);

            List<ServidorDTO> lista = useCase.executarApenasAtivos();

            assertThat(lista).hasSize(2);
            assertThat(lista).extracting(ServidorDTO::nome)
                    .containsExactly("Ana", "Carlos");
            assertThat(lista).extracting(ServidorDTO::ativo)
                    .containsOnly(true);
        }

        @Test
        @DisplayName("sem ativos devolve lista vazia")
        void semAtivosDevolveVazio() {
            criarServidor("Bruno", "M002", "52998224725", false);

            assertThat(useCase.executarApenasAtivos()).isEmpty();
        }

        @Test
        @DisplayName("banco vazio devolve lista vazia")
        void bancoVazioDevolveVazio() {
            assertThat(useCase.executarApenasAtivos()).isEmpty();
        }
    }

    // Construtor
    @Nested
    @DisplayName("Construtor")
    class Construtor {

        @Test
        @DisplayName("repositório nulo lança IllegalArgumentException")
        void repositorioNuloLancaExcecao() {
            assertThatThrownBy(() -> new ListarServidoresUseCase(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}