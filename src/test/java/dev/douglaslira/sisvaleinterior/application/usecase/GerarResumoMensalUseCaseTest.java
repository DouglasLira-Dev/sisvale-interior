package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.dto.ResumoMensalDTO;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.domain.model.Horario;
import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;
import dev.douglaslira.sisvaleinterior.domain.model.Servidor;
import dev.douglaslira.sisvaleinterior.domain.service.CalculadoraRessarcimento;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.ConnectionFactory;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.DatabaseInitializer;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.LancamentoRepositoryJdbc;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.ServidorRepositoryJdbc;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do GerarResumoMensalUseCase")
class GerarResumoMensalUseCaseTest {

    private static final YearMonth MES = YearMonth.of(2026, 9);

    private Connection ancora;
    private ServidorRepositoryJdbc servidorRepository;
    private LancamentoRepositoryJdbc lancamentoRepository;
    private GerarResumoMensalUseCase useCase;

    @BeforeEach
    void setup() throws SQLException {
        String url = "jdbc:sqlite:file:usecase_gerar_" + UUID.randomUUID()
                + "?mode=memory&cache=shared";
        ancora = DriverManager.getConnection(url);

        ConnectionFactory factory = new ConnectionFactory(url);
        new DatabaseInitializer(factory).inicializar();

        servidorRepository = new ServidorRepositoryJdbc(factory);
        lancamentoRepository = new LancamentoRepositoryJdbc(factory);
        CalculadoraRessarcimento calculadora = new CalculadoraRessarcimento();

        ValidarMesUseCase validarMesUseCase =
                new ValidarMesUseCase(lancamentoRepository, servidorRepository, calculadora);

        useCase = new GerarResumoMensalUseCase(servidorRepository, validarMesUseCase);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (ancora != null) {
            ancora.close();
        }
    }

    // Helpers
    private Servidor criarServidorAtivo(String nome, String matricula, String cpf) {
        return servidorRepository.salvar(Servidor.novo(nome, matricula, cpf));
    }

    private Servidor criarServidorInativo(String nome, String matricula, String cpf) {
        return servidorRepository.salvar(new Servidor(null, nome, matricula, cpf, false));
    }

        private void criarLancamentoValido(Long servidorId, int dia) {
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

        lancamentoRepository.salvar(new Lancamento(
                null,
                servidorId,
                LocalDate.of(MES.getYear(), MES.getMonth(), dia),
                List.of(ida, volta)
        ));
    }

    // Sem servidores
    @Nested
    @DisplayName("Sem servidores")
    class SemServidores {

        @Test
        @DisplayName("sem servidores ativos devolve lista vazia")
        void semServidoresAtivosDevolveListaVazia() {
            List<ResumoMensalDTO> resumos = useCase.executar(MES);

            assertThat(resumos).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("só servidores inativos devolve lista vazia")
        void soServidoresInativosDevolveVazio() {
            criarServidorInativo("Ana", "M001", "11144477735");

            assertThat(useCase.executar(MES)).isEmpty();
        }
    }

    // Com servidores
    @Nested
    @DisplayName("Com servidores")
    class ComServidores {

        @Test
        @DisplayName("devolve um resumo por servidor ativo")
        void devolveUmResumoPorServidor() {
            criarServidorAtivo("Ana", "M001", "11144477735");
            criarServidorAtivo("Bruno", "M002", "52998224725");
            criarServidorAtivo("Carlos", "M003", "12345678909");

            List<ResumoMensalDTO> resumos = useCase.executar(MES);

            assertThat(resumos).hasSize(3);
        }

        @Test
        @DisplayName("resumos vêm ordenados por nome do servidor")
        void resumosOrdenadosPorNome() {
            criarServidorAtivo("Carlos", "M003", "52998224725");
            criarServidorAtivo("Ana", "M001", "11144477735");
            criarServidorAtivo("Bruno", "M002", "12345678909");

            List<ResumoMensalDTO> resumos = useCase.executar(MES);

            assertThat(resumos)
                    .extracting(ResumoMensalDTO::nomeServidor)
                    .containsExactly("Ana", "Bruno", "Carlos");
        }

        @Test
        @DisplayName("servidor inativo é ignorado")
        void servidorInativoEhIgnorado() {
            criarServidorAtivo("Ana", "M001", "11144477735");
            criarServidorInativo("Bruno", "M002", "52998224725");

            List<ResumoMensalDTO> resumos = useCase.executar(MES);

            assertThat(resumos).hasSize(1);
            assertThat(resumos.get(0).nomeServidor()).isEqualTo("Ana");
        }

        @Test
        @DisplayName("resumos têm o mesAno correto")
        void resumosTemMesAnoCorreto() {
            criarServidorAtivo("Ana", "M001", "11144477735");

            List<ResumoMensalDTO> resumos = useCase.executar(MES);

            assertThat(resumos.get(0).mesAno()).isEqualTo(MES);
        }
    }

    // Conteúdo dos resumos
    @Nested
    @DisplayName("Conteúdo dos resumos")
    class ConteudoDosResumos {

        @Test
        @DisplayName("servidor sem lançamentos tem resumo com zeros")
        void servidorSemLancamentosTemZeros() {
            criarServidorAtivo("Ana", "M001", "11144477735");

            List<ResumoMensalDTO> resumos = useCase.executar(MES);

            ResumoMensalDTO resumo = resumos.get(0);
            assertThat(resumo.totalDias()).isZero();
            assertThat(resumo.diasTotalmenteValidos()).isZero();
            assertThat(resumo.diasParciais()).isZero();
            assertThat(resumo.diasTotalmenteInvalidos()).isZero();
            assertThat(resumo.valorTotalMes()).isEqualByComparingTo("0.00");
            assertThat(resumo.dias()).isEmpty();
        }

        @Test
        @DisplayName("servidor com lançamentos tem valores calculados")
        void servidorComLancamentosTemValores() {
            Servidor ana = criarServidorAtivo("Ana", "M001", "11144477735");
            criarLancamentoValido(ana.id(), 1);
            criarLancamentoValido(ana.id(), 2);
            criarLancamentoValido(ana.id(), 3);

            List<ResumoMensalDTO> resumos = useCase.executar(MES);

            ResumoMensalDTO resumo = resumos.get(0);
            assertThat(resumo.totalDias()).isEqualTo(3);
            assertThat(resumo.diasTotalmenteValidos()).isEqualTo(3);
            assertThat(resumo.valorTotalMes()).isEqualByComparingTo("126.00");   // 3 * 42
        }

        @Test
        @DisplayName("cada servidor tem seus próprios valores, sem misturar")
        void cadaServidorTemSeusValores() {
            Servidor ana = criarServidorAtivo("Ana", "M001", "11144477735");
            Servidor bruno = criarServidorAtivo("Bruno", "M002", "52998224725");

            criarLancamentoValido(ana.id(), 1);
            criarLancamentoValido(bruno.id(), 2);
            criarLancamentoValido(bruno.id(), 3);

            List<ResumoMensalDTO> resumos = useCase.executar(MES);

            assertThat(resumos).hasSize(2);
            ResumoMensalDTO resumoAna = resumos.get(0);
            ResumoMensalDTO resumoBruno = resumos.get(1);

            assertThat(resumoAna.nomeServidor()).isEqualTo("Ana");
            assertThat(resumoAna.totalDias()).isEqualTo(1);
            assertThat(resumoAna.valorTotalMes()).isEqualByComparingTo("42.00");

            assertThat(resumoBruno.nomeServidor()).isEqualTo("Bruno");
            assertThat(resumoBruno.totalDias()).isEqualTo(2);
            assertThat(resumoBruno.valorTotalMes()).isEqualByComparingTo("84.00");
        }
    }

    // Validação de entrada
    @Nested
    @DisplayName("Validação de entrada")
    class ValidacaoDeEntrada {

        @Test
        @DisplayName("mesAno nulo lança ApplicationException")
        void mesAnoNuloLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(null))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Mês");
        }
    }

    // Construtor
    @Nested
    @DisplayName("Construtor")
    class Construtor {

        @Test
        @DisplayName("servidorRepository nulo lança IllegalArgumentException")
        void servidorRepositoryNulo() {
            assertThatThrownBy(() -> new GerarResumoMensalUseCase(null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}