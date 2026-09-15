package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.dto.ResumoMensalDTO;
import dev.douglaslira.sisvaleinterior.application.dto.StatusDia;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;

import dev.douglaslira.sisvaleinterior.domain.model.Horario;
import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;
import dev.douglaslira.sisvaleinterior.domain.model.Servidor;
import dev.douglaslira.sisvaleinterior.domain.service.CalculadoraRessarcimento;

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
import java.time.YearMonth;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do ValidarMesUseCase")
class ValidarMesUseCaseTest {

    private static final YearMonth MES = YearMonth.of(2026, 9);

    private Connection ancora;
    private ServidorRepositoryJdbc servidorRepository;
    private LancamentoRepositoryJdbc lancamentoRepository;
    private ValidarMesUseCase useCase;
    private Long servidorId;

    @BeforeEach
    void setup() throws SQLException {
        String url = "jdbc:sqlite:file:usecase_validar_" + UUID.randomUUID()
                + "?mode=memory&cache=shared";
        ancora = DriverManager.getConnection(url);

        ConnectionFactory factory = new ConnectionFactory(url);
        new DatabaseInitializer(factory).inicializar();

        servidorRepository = new ServidorRepositoryJdbc(factory);
        lancamentoRepository = new LancamentoRepositoryJdbc(factory);
        CalculadoraRessarcimento calculadora = new CalculadoraRessarcimento();

        useCase = new ValidarMesUseCase(lancamentoRepository, servidorRepository, calculadora);

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
    /**
     * Lançamento com ida e volta válidos.
     * Ida:   07:45 → 07:30 (15 min antes, limite)
     * Volta: 17:00 → 16:45 (15 min antes, limite)
     * Total: 20.00 + 22.00 = 42.00
     */
    private void criarLancamentoAmbosValidos(int dia) {
        lancamentoRepository.salvar(new Lancamento(
                null, servidorId, LocalDate.of(2026, 9, dia),
                Horario.parse("07:45"), Horario.parse("07:30"), new BigDecimal("20.00"),
                Horario.parse("17:00"), Horario.parse("16:45"), new BigDecimal("22.00")
        ));
    }

    /**
     * Ida válida, volta inválida (30 min antes do limite).
     * Total: 20.00 (só ida)
     */
    private void criarLancamentoSoIdaValida(int dia) {
        lancamentoRepository.salvar(new Lancamento(
                null, servidorId, LocalDate.of(2026, 9, dia),
                Horario.parse("07:45"), Horario.parse("07:30"), new BigDecimal("20.00"),
                Horario.parse("17:00"), Horario.parse("16:30"), new BigDecimal("22.00")
        ));
    }

    /**
     * Ida inválida (45 min antes do limite), volta válida.
     * Total: 22.00 (só volta)
     */
    private void criarLancamentoSoVoltaValida(int dia) {
        lancamentoRepository.salvar(new Lancamento(
                null, servidorId, LocalDate.of(2026, 9, dia),
                Horario.parse("07:45"), Horario.parse("07:00"), new BigDecimal("20.00"),
                Horario.parse("17:00"), Horario.parse("16:45"), new BigDecimal("22.00")
        ));
    }

    /**
     * Ida e volta inválidos.
     * Total: 0.00
     */
    private void criarLancamentoNenhumValido(int dia) {
        lancamentoRepository.salvar(new Lancamento(
                null, servidorId, LocalDate.of(2026, 9, dia),
                Horario.parse("07:45"), Horario.parse("07:00"), new BigDecimal("20.00"),
                Horario.parse("17:00"), Horario.parse("16:30"), new BigDecimal("22.00")
        ));
    }

    // Mês sem lançamentos
    @Nested
    @DisplayName("Mês sem lançamentos")
    class MesSemLancamentos {

        @Test
        @DisplayName("devolve resumo com total zero e listas vazias")
        void devolveResumoComZeros() {
            ResumoMensalDTO resumo = useCase.executar(servidorId, MES);

            assertThat(resumo.totalDias()).isZero();
            assertThat(resumo.diasTotalmenteValidos()).isZero();
            assertThat(resumo.diasParciais()).isZero();
            assertThat(resumo.diasTotalmenteInvalidos()).isZero();
            assertThat(resumo.valorTotalMes()).isEqualByComparingTo("0.00");
            assertThat(resumo.dias()).isEmpty();
        }

        @Test
        @DisplayName("preserva servidorId, nome e mesAno")
        void preservaDadosDoServidor() {
            ResumoMensalDTO resumo = useCase.executar(servidorId, MES);

            assertThat(resumo.servidorId()).isEqualTo(servidorId);
            assertThat(resumo.nomeServidor()).isEqualTo("João Silva");
            assertThat(resumo.mesAno()).isEqualTo(MES);
        }
    }

    // Mês com lançamentos válidos
    @Nested
    @DisplayName("Mês com lançamentos válidos")
    class MesComLancamentosValidos {

        @Test
        @DisplayName("3 lançamentos válidos somam 126.00")
        void tresLancamentosValidosSomam126() {
            criarLancamentoAmbosValidos(1);
            criarLancamentoAmbosValidos(2);
            criarLancamentoAmbosValidos(3);

            ResumoMensalDTO resumo = useCase.executar(servidorId, MES);

            assertThat(resumo.totalDias()).isEqualTo(3);
            assertThat(resumo.diasTotalmenteValidos()).isEqualTo(3);
            assertThat(resumo.diasParciais()).isZero();
            assertThat(resumo.diasTotalmenteInvalidos()).isZero();
            assertThat(resumo.valorTotalMes()).isEqualByComparingTo("126.00");
        }

        @Test
        @DisplayName("lista de dias vem ordenada por data")
        void diasOrdenadosPorData() {
            criarLancamentoAmbosValidos(20);
            criarLancamentoAmbosValidos(5);
            criarLancamentoAmbosValidos(15);

            ResumoMensalDTO resumo = useCase.executar(servidorId, MES);

            assertThat(resumo.dias())
                    .extracting(ResumoMensalDTO.DiaResumoDTO::data)
                    .containsExactly(
                            LocalDate.of(2026, 9, 5),
                            LocalDate.of(2026, 9, 15),
                            LocalDate.of(2026, 9, 20)
                    );
        }

        @Test
        @DisplayName("todos os dias têm status VALIDO")
        void diasTemStatusValido() {
            criarLancamentoAmbosValidos(1);
            criarLancamentoAmbosValidos(2);

            ResumoMensalDTO resumo = useCase.executar(servidorId, MES);

            assertThat(resumo.dias())
                    .extracting(ResumoMensalDTO.DiaResumoDTO::status)
                    .containsOnly(StatusDia.VALIDO);
        }
    }

    // Mês com lançamentos mistos
    @Nested
    @DisplayName("Mês com lançamentos mistos")
    class MesComLancamentosMistos {

        @Test
        @DisplayName("soma apenas o que tem valor válido")
        void somaApenasOValido() {
            criarLancamentoAmbosValidos(1);      // 42.00
            criarLancamentoSoIdaValida(2);       // 20.00
            criarLancamentoSoVoltaValida(3);     // 22.00
            criarLancamentoNenhumValido(4);      // 0.00

            ResumoMensalDTO resumo = useCase.executar(servidorId, MES);

            assertThat(resumo.totalDias()).isEqualTo(4);
            assertThat(resumo.diasTotalmenteValidos()).isEqualTo(1);
            assertThat(resumo.diasParciais()).isEqualTo(2);
            assertThat(resumo.diasTotalmenteInvalidos()).isEqualTo(1);
            assertThat(resumo.valorTotalMes()).isEqualByComparingTo("84.00");
        }

        @Test
        @DisplayName("status de cada dia é o correto")
        void statusCorretoPorDia() {
            criarLancamentoAmbosValidos(1);      // VALIDO
            criarLancamentoSoIdaValida(2);       // PARCIAL
            criarLancamentoSoVoltaValida(3);     // PARCIAL
            criarLancamentoNenhumValido(4);      // INVALIDO

            ResumoMensalDTO resumo = useCase.executar(servidorId, MES);

            assertThat(resumo.dias()).hasSize(4);
            assertThat(resumo.dias().get(0).status())
                    .isEqualTo(StatusDia.VALIDO);
            assertThat(resumo.dias().get(1).status())
                    .isEqualTo(StatusDia.PARCIAL);
            assertThat(resumo.dias().get(2).status())
                    .isEqualTo(StatusDia.PARCIAL);
            assertThat(resumo.dias().get(3).status())
                    .isEqualTo(StatusDia.INVALIDO);
        }

        @Test
        @DisplayName("valores por dia correspondem ao que foi validado")
        void valoresPorDia() {
            criarLancamentoAmbosValidos(1);      // 42.00
            criarLancamentoSoIdaValida(2);       // 20.00
            criarLancamentoSoVoltaValida(3);     // 22.00
            criarLancamentoNenhumValido(4);      // 0.00

            ResumoMensalDTO resumo = useCase.executar(servidorId, MES);

            assertThat(resumo.dias().get(0).valor()).isEqualByComparingTo("42.00");
            assertThat(resumo.dias().get(1).valor()).isEqualByComparingTo("20.00");
            assertThat(resumo.dias().get(2).valor()).isEqualByComparingTo("22.00");
            assertThat(resumo.dias().get(3).valor()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("contadores somam o total de dias")
        void contadoresSomamTotal() {
            criarLancamentoAmbosValidos(1);
            criarLancamentoSoIdaValida(2);
            criarLancamentoSoVoltaValida(3);
            criarLancamentoNenhumValido(4);

            ResumoMensalDTO resumo = useCase.executar(servidorId, MES);

            int soma = resumo.diasTotalmenteValidos()
                    + resumo.diasParciais()
                    + resumo.diasTotalmenteInvalidos();
            assertThat(soma).isEqualTo(resumo.totalDias());
        }
    }

    // Filtro por mês
    @Nested
    @DisplayName("Filtro por mês")
    class FiltroPorMes {

        @Test
        @DisplayName("lançamentos de outro mês são ignorados")
        void lancaamentosDeOutroMesSaoIgnorados() {
            criarLancamentoAmbosValidos(15);

            lancamentoRepository.salvar(new Lancamento(
                    null, servidorId, LocalDate.of(2026, 10, 15),
                    Horario.parse("07:45"), Horario.parse("07:30"), new BigDecimal("20.00"),
                    Horario.parse("17:00"), Horario.parse("16:45"), new BigDecimal("22.00")
            ));

            ResumoMensalDTO resumo = useCase.executar(servidorId, MES);

            assertThat(resumo.totalDias()).isEqualTo(1);
            assertThat(resumo.valorTotalMes()).isEqualByComparingTo("42.00");
        }
    }

    // Servidor inexistente
    @Nested
    @DisplayName("Servidor inexistente")
    class ServidorInexistente {

        @Test
        @DisplayName("servidorId inexistente lança ApplicationException")
        void servidorInexistenteLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(999L, MES))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Servidor não encontrado");
        }
    }

    // Validação de entrada
    @Nested
    @DisplayName("Validação de entrada")
    class ValidacaoDeEntrada {

        @Test
        @DisplayName("servidorId nulo lança ApplicationException")
        void servidorIdNuloLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(null, MES))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Servidor");
        }

        @Test
        @DisplayName("mesAno nulo lança ApplicationException")
        void mesAnoNuloLancaExcecao() {
            assertThatThrownBy(() -> useCase.executar(servidorId, null))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining("Mês");
        }
    }

    // Construtor
    @Nested
    @DisplayName("Construtor")
    class Construtor {

        @Test
        @DisplayName("lancamentoRepository nulo lança IllegalArgumentException")
        void lancamentoRepositoryNulo() {
            assertThatThrownBy(() -> new ValidarMesUseCase(
                    null, servidorRepository, new CalculadoraRessarcimento()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("servidorRepository nulo lança IllegalArgumentException")
        void servidorRepositoryNulo() {
            assertThatThrownBy(() -> new ValidarMesUseCase(
                    lancamentoRepository, null, new CalculadoraRessarcimento()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("calculadora nula lança IllegalArgumentException")
        void calculadoraNula() {
            assertThatThrownBy(() -> new ValidarMesUseCase(
                    lancamentoRepository, servidorRepository, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}