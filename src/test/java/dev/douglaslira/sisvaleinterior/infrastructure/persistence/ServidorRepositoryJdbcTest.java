package dev.douglaslira.sisvaleinterior.infrastructure.persistence;

import dev.douglaslira.sisvaleinterior.domain.model.Servidor;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do ServidorRepositoryJdbc")
class ServidorRepositoryJdbcTest {

    private String url;
    private Connection ancora;
    private ConnectionFactory connectionFactory;
    private ServidorRepositoryJdbc repository;

    @BeforeEach
    void setup() throws SQLException {
        url = "jdbc:sqlite:file:repo_servidor_" + UUID.randomUUID()
                + "?mode=memory&cache=shared";
        ancora = DriverManager.getConnection(url);   // mantém o banco vivo

        connectionFactory = new ConnectionFactory(url);
        new DatabaseInitializer(connectionFactory).inicializar();
        repository = new ServidorRepositoryJdbc(connectionFactory);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (ancora != null) {
            ancora.close();
        }
    }

    // Helpers
    private Servidor novoServidor(String nome, String matricula, String cpf) {
        return Servidor.novo(nome, matricula, cpf);
    }

    private Servidor servidorPadrao() {
        return novoServidor("João Silva", "M001", "11144477735");
    }

    // Inserir
    @Nested
    @DisplayName("Inserir")
    class Inserir {

        @Test
        @DisplayName("deve inserir servidor com id nulo e retornar com id preenchido")
        void deveInserirServidorComIdNulo() {
            Servidor salvo = repository.salvar(servidorPadrao());

            assertThat(salvo.id()).isNotNull().isPositive();
            assertThat(salvo.nome()).isEqualTo("João Silva");
            assertThat(salvo.matricula()).isEqualTo("M001");
            assertThat(salvo.cpf()).isEqualTo("11144477735");
            assertThat(salvo.ativo()).isTrue();
        }

        @Test
        @DisplayName("dois servidores inseridos têm IDs diferentes")
        void doisServidoresTemIdsDiferentes() {
            Servidor a = repository.salvar(novoServidor("João", "M001", "11144477735"));
            Servidor b = repository.salvar(novoServidor("Maria", "M002", "52998224725"));

            assertThat(a.id()).isNotEqualTo(b.id());
        }

        @Test
        @DisplayName("matrícula duplicada lança PersistenceException")
        void matriculaDuplicadaLancaExcecao() {
            repository.salvar(novoServidor("João", "M001", "11144477735"));

            assertThatThrownBy(() ->
                    repository.salvar(novoServidor("Maria", "M001", "52998224725")))
                    .isInstanceOf(PersistenceException.class);
        }

        @Test
        @DisplayName("CPF duplicado lança PersistenceException")
        void cpfDuplicadoLancaExcecao() {
            repository.salvar(novoServidor("João", "M001", "11144477735"));

            assertThatThrownBy(() ->
                    repository.salvar(novoServidor("Maria", "M002", "11144477735")))
                    .isInstanceOf(PersistenceException.class);
        }
    }

    // Atualizar
    @Nested
    @DisplayName("Atualizar")
    class Atualizar {

        @Test
        @DisplayName("salvar com id existente atualiza os dados")
        void salvarComIdExistenteAtualiza() {
            Servidor salvo = repository.salvar(servidorPadrao());
            Servidor alterado = new Servidor(
                    salvo.id(), "João Silva Jr.", "M001", "11144477735", false);

            Servidor resultado = repository.salvar(alterado);

            assertThat(resultado.id()).isEqualTo(salvo.id());
            assertThat(resultado.nome()).isEqualTo("João Silva Jr.");
            assertThat(resultado.ativo()).isFalse();

            Optional<Servidor> recarregado = repository.buscarPorId(salvo.id());
            assertThat(recarregado).isPresent();
            assertThat(recarregado.get().nome()).isEqualTo("João Silva Jr.");
            assertThat(recarregado.get().ativo()).isFalse();
        }

        @Test
        @DisplayName("salvar com id inexistente lança PersistenceException")
        void salvarComIdInexistenteLancaExcecao() {
            Servidor fantasma = new Servidor(
                    999L, "Fantasma", "M999", "11144477735", true);

            assertThatThrownBy(() -> repository.salvar(fantasma))
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // Buscar por id
    @Nested
    @DisplayName("Buscar por id")
    class BuscarPorId {

        @Test
        @DisplayName("id existente devolve Optional com servidor correto")
        void idExistenteDevolveServidor() {
            Servidor salvo = repository.salvar(servidorPadrao());

            Optional<Servidor> encontrado = repository.buscarPorId(salvo.id());

            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().id()).isEqualTo(salvo.id());
            assertThat(encontrado.get().matricula()).isEqualTo("M001");
        }

        @Test
        @DisplayName("id inexistente devolve Optional.empty()")
        void idInexistenteDevolveVazio() {
            Optional<Servidor> encontrado = repository.buscarPorId(999L);

            assertThat(encontrado).isEmpty();
        }

        @Test
        @DisplayName("id nulo lança IllegalArgumentException")
        void idNuloLancaExcecao() {
            assertThatThrownBy(() -> repository.buscarPorId(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // Buscar por matrícula
    @Nested
    @DisplayName("Buscar por matrícula")
    class BuscarPorMatricula {

        @Test
        @DisplayName("matrícula existente devolve servidor correto")
        void matriculaExistenteDevolveServidor() {
            repository.salvar(servidorPadrao());

            Optional<Servidor> encontrado = repository.buscarPorMatricula("M001");

            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().nome()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("matrícula inexistente devolve Optional.empty()")
        void matriculaInexistenteDevolveVazio() {
            Optional<Servidor> encontrado = repository.buscarPorMatricula("M999");

            assertThat(encontrado).isEmpty();
        }
    }

    // Buscar por CPF
    @Nested
    @DisplayName("Buscar por CPF")
    class BuscarPorCpf {

        @Test
        @DisplayName("CPF existente devolve servidor correto")
        void cpfExistenteDevolveServidor() {
            repository.salvar(servidorPadrao());

            Optional<Servidor> encontrado = repository.buscarPorCpf("11144477735");

            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().nome()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("CPF existente com máscara também encontra (normalização)")
        void cpfComMascaraTambemEncontra() {
            repository.salvar(servidorPadrao());

            Optional<Servidor> encontrado = repository.buscarPorCpf("111.444.777-35");

            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().matricula()).isEqualTo("M001");
        }

        @Test
        @DisplayName("CPF inexistente devolve Optional.empty()")
        void cpfInexistenteDevolveVazio() {
            Optional<Servidor> encontrado = repository.buscarPorCpf("52998224725");

            assertThat(encontrado).isEmpty();
        }

        @Test
        @DisplayName("CPF nulo lança IllegalArgumentException")
        void cpfNuloLancaExcecao() {
            assertThatThrownBy(() -> repository.buscarPorCpf(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // Listar todos
    @Nested
    @DisplayName("Listar todos")
    class ListarTodos {

        @Test
        @DisplayName("banco vazio devolve lista vazia")
        void bancoVazioDevolveListaVazia() {
            List<Servidor> lista = repository.listarTodos();

            assertThat(lista).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("banco com 3 servidores devolve lista com 3, ordenada por nome")
        void listaComTresOrdenadaPorNome() {
            repository.salvar(novoServidor("Carlos", "M003", "52998224725"));
            repository.salvar(novoServidor("Ana", "M001", "11144477735"));
            repository.salvar(novoServidor("Bruno", "M002", "12345678909"));

            List<Servidor> lista = repository.listarTodos();

            assertThat(lista).hasSize(3);
            assertThat(lista).extracting(Servidor::nome)
                    .containsExactly("Ana", "Bruno", "Carlos");
        }

        @Test
        @DisplayName("lista retornada é imutável")
        void listaRetornadaEhImutavel() {
            repository.salvar(servidorPadrao());

            List<Servidor> lista = repository.listarTodos();

            assertThatThrownBy(() -> lista.add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // Listar ativos
    @Nested
    @DisplayName("Listar ativos")
    class ListarAtivos {

        @Test
        @DisplayName("filtra apenas servidores ativos")
        void filtraApenasAtivos() {
            repository.salvar(novoServidor("Ana", "M001", "11144477735"));
            Servidor inativo = repository.salvar(
                    new Servidor(null, "Bruno", "M002", "52998224725", false));
            repository.salvar(novoServidor("Carlos", "M003", "12345678909"));

            List<Servidor> ativos = repository.listarAtivos();

            assertThat(ativos).hasSize(2);
            assertThat(ativos).extracting(Servidor::matricula)
                    .containsExactlyInAnyOrder("M001", "M003");
            assertThat(ativos).extracting(Servidor::id)
                    .doesNotContain(inativo.id());
        }

        @Test
        @DisplayName("banco sem ativos devolve lista vazia")
        void bancoSemAtivosDevolveVazio() {
            repository.salvar(new Servidor(null, "Ana", "M001", "11144477735", false));

            assertThat(repository.listarAtivos()).isEmpty();
        }
    }

    // Desativar
    @Nested
    @DisplayName("Desativar")
    class Desativar {

        @Test
        @DisplayName("após desativar, listarAtivos não inclui o servidor")
        void aposDesativarNaoApareceEmAtivos() {
            Servidor salvo = repository.salvar(servidorPadrao());

            repository.desativar(salvo.id());

            assertThat(repository.listarAtivos()).isEmpty();
        }

        @Test
        @DisplayName("após desativar, listarTodos ainda inclui o servidor")
        void aposDesativarAindaApareceEmTodos() {
            Servidor salvo = repository.salvar(servidorPadrao());

            repository.desativar(salvo.id());

            List<Servidor> todos = repository.listarTodos();
            assertThat(todos).hasSize(1);
            assertThat(todos.get(0).ativo()).isFalse();
        }

        @Test
        @DisplayName("desativar id inexistente lança PersistenceException")
        void desativarIdInexistenteLancaExcecao() {
            assertThatThrownBy(() -> repository.desativar(999L))
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("não encontrado");
        }

        @Test
        @DisplayName("desativar id nulo lança IllegalArgumentException")
        void desativarIdNuloLancaExcecao() {
            assertThatThrownBy(() -> repository.desativar(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}