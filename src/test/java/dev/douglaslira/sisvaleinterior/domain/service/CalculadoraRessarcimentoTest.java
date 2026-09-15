package dev.douglaslira.sisvaleinterior.domain.service;

import dev.douglaslira.sisvaleinterior.domain.model.Horario;
import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoDia;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoMes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes da Calculadora de Ressarcimento")
class CalculadoraRessarcimentoTest {

    private static final Long SERVIDOR_ID = 1L;
    private static final YearMonth MES = YearMonth.of(2026, 9);

    private final CalculadoraRessarcimento calculadora = new CalculadoraRessarcimento();

    // Helpers — evitam repetir 9 parâmetros em cada teste
    private Lancamento lancamento(int dia, String descida, String entrada, String valorIda, String saida, String onibus, String valorVolta) {
        return new Lancamento(
                null, SERVIDOR_ID, LocalDate.of(MES.getYear(), MES.getMonth(), dia),
                Horario.parse(descida), Horario.parse(entrada), new BigDecimal(valorIda),
                Horario.parse(saida), Horario.parse(onibus), new BigDecimal(valorVolta)
        );
    }

    /** Ida válida (07:45 → 07:30) + volta válida (17:00 → 16:45). */
    private Lancamento diaAmbosValidos(int dia) {
        return lancamento(dia, "07:45", "07:30", "20.00", "17:00", "16:45", "22.00");
    }

    /** Ida válida + volta inválida (17:00 → 16:30 = 30 min antes). */
    private Lancamento diaSoIda(int dia) {
        return lancamento(dia, "07:45", "07:30", "20.00", "17:00", "16:30", "22.00");
    }

    /** Ida inválida (07:45 → 07:00 = 45 min antes) + volta válida. */
    private Lancamento diaSoVolta(int dia) {
        return lancamento(dia, "07:45", "07:00", "20.00", "17:00", "16:45", "22.00");
    }

    /** Ida inválida + volta inválida. */
    private Lancamento diaNenhumValido(int dia) {
        return lancamento(dia, "07:45", "07:00", "20.00", "17:00", "16:30", "22.00");
    }

    // Cálculo do dia
    @Nested
    @DisplayName("Cálculo do dia")
    class CalculoDia {

        @Test
        @DisplayName("ambos válidos → soma os dois valores")
        void ambosValidosSoma() {
            ResultadoDia resultado = calculadora.calcularDia(diaAmbosValidos(15));

            assertThat(resultado.validacaoIda().valido()).isTrue();
            assertThat(resultado.validacaoVolta().valido()).isTrue();
            assertThat(resultado.valorTotalDia()).isEqualByComparingTo("42.00");
            assertThat(resultado.totalmenteValido()).isTrue();
        }

        @Test
        @DisplayName("só ida válida → soma apenas o valor da ida")
        void soIdaValidaSoma() {
            ResultadoDia resultado = calculadora.calcularDia(diaSoIda(15));

            assertThat(resultado.validacaoIda().valido()).isTrue();
            assertThat(resultado.validacaoVolta().valido()).isFalse();
            assertThat(resultado.valorTotalDia()).isEqualByComparingTo("20.00");
            assertThat(resultado.parcialmenteValido()).isTrue();
        }

        @Test
        @DisplayName("só volta válida → soma apenas o valor da volta")
        void soVoltaValidaSoma() {
            ResultadoDia resultado = calculadora.calcularDia(diaSoVolta(15));

            assertThat(resultado.validacaoIda().valido()).isFalse();
            assertThat(resultado.validacaoVolta().valido()).isTrue();
            assertThat(resultado.valorTotalDia()).isEqualByComparingTo("22.00");
            assertThat(resultado.parcialmenteValido()).isTrue();
        }

        @Test
        @DisplayName("nenhum válido → total zero")
        void nenhumValidoTotalZero() {
            ResultadoDia resultado = calculadora.calcularDia(diaNenhumValido(15));

            assertThat(resultado.validacaoIda().valido()).isFalse();
            assertThat(resultado.validacaoVolta().valido()).isFalse();
            assertThat(resultado.valorTotalDia()).isEqualByComparingTo("0.00");
            assertThat(resultado.totalmenteInvalido()).isTrue();
        }

        @Test
        @DisplayName("deve preservar a data do lançamento")
        void devePreservarData() {
            ResultadoDia resultado = calculadora.calcularDia(diaAmbosValidos(15));

            assertThat(resultado.data()).isEqualTo(LocalDate.of(2026, 9, 15));
        }

        @Test
        @DisplayName("deve lançar exceção para lançamento nulo")
        void deveRejeitarLancamentoNulo() {
            assertThatThrownBy(() -> calculadora.calcularDia(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Lançamento");
        }
    }

    // Cálculo do mês
    @Nested
    @DisplayName("Cálculo do mês")
    class CalculoMes {

        @Test
        @DisplayName("mês com 3 dias válidos → soma correta")
        void tresDiasValidosSomaCorreta() {
            List<Lancamento> lancamentos = List.of(
                    diaAmbosValidos(1),
                    diaAmbosValidos(2),
                    diaAmbosValidos(3)
            );

            ResultadoMes resultado = calculadora.calcularMes(lancamentos, MES, SERVIDOR_ID);

            assertThat(resultado.dias()).hasSize(3);
            assertThat(resultado.valorTotalMes()).isEqualByComparingTo("126.00");
        }

        @Test
        @DisplayName("mês com dias mistos → soma só o que tem valor")
        void diasMistosSomaParcial() {
            List<Lancamento> lancamentos = List.of(
                    diaAmbosValidos(1),     // 42.00
                    diaSoIda(2),            // 20.00
                    diaSoVolta(3),          // 22.00
                    diaNenhumValido(4)      // 0.00
            );

            ResultadoMes resultado = calculadora.calcularMes(lancamentos, MES, SERVIDOR_ID);

            assertThat(resultado.dias()).hasSize(4);
            assertThat(resultado.valorTotalMes()).isEqualByComparingTo("84.00");
        }

        @Test
        @DisplayName("mês sem lançamentos → total zero e lista vazia")
        void mesSemLancamentosTotalZero() {
            ResultadoMes resultado = calculadora.calcularMes(List.of(), MES, SERVIDOR_ID);

            assertThat(resultado.dias()).isEmpty();
            assertThat(resultado.valorTotalMes()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("lançamentos de outro mês são filtrados")
        void filtraLancamentosDeOutroMes() {
            Lancamento setembro = diaAmbosValidos(15);
            Lancamento outubro = new Lancamento(
                    null, SERVIDOR_ID, LocalDate.of(2026, 10, 15),
                    Horario.parse("07:45"), Horario.parse("07:30"), new BigDecimal("20.00"),
                    Horario.parse("17:00"), Horario.parse("16:45"), new BigDecimal("22.00")
            );

            ResultadoMes resultado = calculadora.calcularMes(
                    List.of(setembro, outubro), MES, SERVIDOR_ID);

            assertThat(resultado.dias()).hasSize(1);
            assertThat(resultado.valorTotalMes()).isEqualByComparingTo("42.00");
        }

        @Test
        @DisplayName("ResultadoMes preserva servidorId e mesAno")
        void preservaServidorEMes() {
            ResultadoMes resultado = calculadora.calcularMes(
                    List.of(diaAmbosValidos(1)), MES, SERVIDOR_ID);

            assertThat(resultado.servidorId()).isEqualTo(SERVIDOR_ID);
            assertThat(resultado.mesAno()).isEqualTo(MES);
        }

        @Test
        @DisplayName("lista nula lança IllegalArgumentException")
        void listaNulaLancaExcecao() {
            assertThatThrownBy(() -> calculadora.calcularMes(null, MES, SERVIDOR_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("lançamentos");
        }

        @Test
        @DisplayName("mês/ano nulo lança IllegalArgumentException")
        void mesAnoNuloLancaExcecao() {
            assertThatThrownBy(() -> calculadora.calcularMes(List.of(), null, SERVIDOR_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Mês");
        }

        @Test
        @DisplayName("servidor nulo lança IllegalArgumentException")
        void servidorNuloLancaExcecao() {
            assertThatThrownBy(() -> calculadora.calcularMes(List.of(), MES, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Servidor");
        }
    }

    // Ordenação da lista de dias
    @Nested
    @DisplayName("Ordenação dos dias")
    class Ordenacao {

        @Test
        @DisplayName("dias devem ficar ordenados por data crescente")
        void diasOrdenadosPorData() {
            List<Lancamento> fora = List.of(
                    diaAmbosValidos(20),
                    diaAmbosValidos(5),
                    diaAmbosValidos(15)
            );

            ResultadoMes resultado = calculadora.calcularMes(fora, MES, SERVIDOR_ID);

            assertThat(resultado.dias())
                    .extracting(ResultadoDia::data)
                    .containsExactly(
                            LocalDate.of(2026, 9, 5),
                            LocalDate.of(2026, 9, 15),
                            LocalDate.of(2026, 9, 20)
                    );
        }
    }

    // Filtros do ResultadoMes
    @Nested
    @DisplayName("Filtros do resultado do mês")
    class Filtros {

        private ResultadoMes mesMisto() {
            List<Lancamento> lancamentos = List.of(
                    diaAmbosValidos(1),     // totalmente válido
                    diaSoIda(2),            // parcial
                    diaSoVolta(3),          // parcial
                    diaNenhumValido(4)      // totalmente inválido
            );
            return calculadora.calcularMes(lancamentos, MES, SERVIDOR_ID);
        }

        @Test
        @DisplayName("diasComAlgumValor deve trazer válidos e parciais")
        void diasComAlgumValor() {
            ResultadoMes resultado = mesMisto();

            assertThat(resultado.diasComAlgumValor()).hasSize(3);
        }

        @Test
        @DisplayName("diasParciais deve trazer apenas os parcialmente válidos")
        void diasParciais() {
            ResultadoMes resultado = mesMisto();

            assertThat(resultado.diasParciais()).hasSize(2);
        }

        @Test
        @DisplayName("diasInvalidos deve trazer apenas os totalmente inválidos")
        void diasInvalidos() {
            ResultadoMes resultado = mesMisto();

            assertThat(resultado.diasInvalidos()).hasSize(1);
        }

        @Test
        @DisplayName("filtros retornam listas imutáveis")
        void filtrosRetornamListasImutaveis() {
            ResultadoMes resultado = mesMisto();

            assertThatThrownBy(() -> resultado.diasComAlgumValor().add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> resultado.diasParciais().add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> resultado.diasInvalidos().add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // Casos de borda
    @Nested
    @DisplayName("Casos de borda")
    class CasosDeBorda {

        @Test
        @DisplayName("valores com centavos somam corretamente")
        void valoresComCentavos() {
            Lancamento lancamento = lancamento(
                    15, "07:45", "07:30", "20.50",
                    "17:00", "16:45", "22.75"
            );

            ResultadoDia resultado = calculadora.calcularDia(lancamento);

            assertThat(resultado.valorTotalDia()).isEqualByComparingTo("43.25");
        }

        @Test
        @DisplayName("mês com muitos dias soma corretamente")
        void mesComMuitosDias() {
            List<Lancamento> lancamentos = List.of(
                    diaAmbosValidos(1),
                    diaAmbosValidos(2),
                    diaAmbosValidos(3),
                    diaAmbosValidos(4),
                    diaAmbosValidos(5),
                    diaAmbosValidos(8),
                    diaAmbosValidos(9),
                    diaAmbosValidos(10),
                    diaAmbosValidos(11),
                    diaAmbosValidos(12)
            );

            ResultadoMes resultado = calculadora.calcularMes(lancamentos, MES, SERVIDOR_ID);

            assertThat(resultado.dias()).hasSize(10);
            // 10 * 42.00 = 420.00
            assertThat(resultado.valorTotalMes()).isEqualByComparingTo("420.00");
        }

        @Test
        @DisplayName("total do mês é a soma dos totais dos dias")
        void totalDoMesEhSomaDosDias() {
            List<Lancamento> lancamentos = List.of(
                    diaAmbosValidos(1),
                    diaSoIda(2),
                    diaNenhumValido(3)
            );

            ResultadoMes resultado = calculadora.calcularMes(lancamentos, MES, SERVIDOR_ID);

            BigDecimal somaDosDias = resultado.dias().stream()
                    .map(ResultadoDia::valorTotalDia)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            assertThat(resultado.valorTotalMes()).isEqualByComparingTo(somaDosDias);
            assertThat(resultado.valorTotalMes()).isEqualByComparingTo("62.00");
        }
    }
}