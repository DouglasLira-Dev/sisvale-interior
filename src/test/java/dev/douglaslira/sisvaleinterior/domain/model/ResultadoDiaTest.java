package dev.douglaslira.sisvaleinterior.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do record ResultadoDia")
class ResultadoDiaTest {

    private static final LocalDate DATA = LocalDate.of(2026, 9, 15);

    // Helpers
    private ResultadoValidacao valido() {
        return ResultadoValidacao.valido(0);
    }

    private ResultadoValidacao invalido() {
        return ResultadoValidacao.invalido(-30, "fora da tolerância");
    }

    private Trecho trechoFake() {
        return new Trecho(
                Horario.parse("07:45"),
                Horario.parse("07:30"),
                new BigDecimal("10.00")
        );
    }

    private ResultadoTrecho resultadoValido() {
        return ResultadoTrecho.de(trechoFake(), valido());
    }

    private ResultadoTrecho resultadoInvalido() {
        return ResultadoTrecho.de(trechoFake(), invalido());
    }

    /** Monta uma lista com N resultados válidos e M inválidos. */
    private List<ResultadoTrecho> resultados(int qtdValidos, int qtdInvalidos) {
        List<ResultadoTrecho> lista = new ArrayList<>();
        for (int i = 0; i < qtdValidos; i++) {
            lista.add(resultadoValido());
        }
        for (int i = 0; i < qtdInvalidos; i++) {
            lista.add(resultadoInvalido());
        }
        return lista;
    }

    /** Cria um ResultadoDia a partir de uma lista de resultados, somando os valores. */
    private ResultadoDia com(List<ResultadoTrecho> resultados) {
        BigDecimal total = resultados.stream()
                .map(ResultadoTrecho::valorContabilizado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResultadoDia(DATA, resultados, total);
    }

    // Criação válida
    @Nested
    @DisplayName("Criação válida")
    class CriacaoValida {

        @Test
        @DisplayName("deve criar com data, resultados e valor total")
        void deveCriarComTodosOsCampos() {
            ResultadoDia r = new ResultadoDia(
                    DATA,
                    List.of(resultadoValido(), resultadoValido()),
                    new BigDecimal("42.00")
            );

            assertThat(r.data()).isEqualTo(DATA);
            assertThat(r.resultados()).hasSize(2);
            assertThat(r.valorTotalDia()).isEqualByComparingTo("42.00");
        }

        @Test
        @DisplayName("deve aceitar valor zero")
        void deveAceitarValorZero() {
            ResultadoDia r = new ResultadoDia(
                    DATA,
                    List.of(resultadoInvalido(), resultadoInvalido()),
                    BigDecimal.ZERO
            );

            assertThat(r.valorTotalDia()).isEqualByComparingTo("0.00");
            assertThat(r.valorTotalDia().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("deve normalizar valor para escala 2")
        void deveNormalizarValorParaEscala2() {
            ResultadoDia r = new ResultadoDia(
                    DATA,
                    List.of(resultadoValido()),
                    new BigDecimal("42.5")
            );

            assertThat(r.valorTotalDia()).isEqualByComparingTo("42.50");
            assertThat(r.valorTotalDia().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("deve arredondar valor com mais de 2 casas (HALF_UP)")
        void deveArredondarValor() {
            ResultadoDia r = new ResultadoDia(
                    DATA,
                    List.of(resultadoValido()),
                    new BigDecimal("42.555")
            );

            assertThat(r.valorTotalDia()).isEqualByComparingTo("42.56");
        }
    }

    // Validação de nulos
    @Nested
    @DisplayName("Validação de nulos")
    class ValidacaoDeNulos {

        @Test
        @DisplayName("deve lançar exceção quando data for nula")
        void deveLancarExcecaoQuandoDataForNula() {
            assertThatThrownBy(() -> new ResultadoDia(
                    null, List.of(resultadoValido()), BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Data");
        }

        @Test
        @DisplayName("deve lançar exceção quando resultados forem nulos")
        void deveLancarExcecaoQuandoResultadosForemNulos() {
            assertThatThrownBy(() -> new ResultadoDia(DATA, null, BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("esultados");
        }

        @Test
        @DisplayName("deve lançar exceção quando lista de resultados estiver vazia")
        void deveLancarExcecaoQuandoResultadosVazios() {
            assertThatThrownBy(() -> new ResultadoDia(
                    DATA, List.of(), BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando valorTotalDia for nulo")
        void deveLancarExcecaoQuandoValorTotalDiaForNulo() {
            assertThatThrownBy(() -> new ResultadoDia(
                    DATA, List.of(resultadoValido()), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Valor");
        }
    }
    // totalmenteValido
    @Nested
    @DisplayName("totalmenteValido")
    class TotalmenteValido {

        @ParameterizedTest(name = "[{index}] {0} válido(s), {1} inválido(s) → esperado={2}")
        @CsvSource({
                "2, 0, true",
                "1, 0, true",
                "1, 1, false",
                "2, 1, false",
                "0, 1, false",
                "0, 2, false"
        })
        @DisplayName("deve retornar true apenas quando todos são válidos")
        void deveValidarCombinacoes(int qtdValidos, int qtdInvalidos, boolean esperado) {
            ResultadoDia r = com(resultados(qtdValidos, qtdInvalidos));

            assertThat(r.totalmenteValido()).isEqualTo(esperado);
        }
    }

    // parcialmenteValido
    @Nested
    @DisplayName("parcialmenteValido")
    class ParcialmenteValido {

        @ParameterizedTest(name = "[{index}] {0} válido(s), {1} inválido(s) → esperado={2}")
        @CsvSource({
                "2, 0, false",
                "1, 0, false",
                "1, 1, true",
                "2, 1, true",
                "0, 1, false",
                "0, 2, false"
        })
        @DisplayName("deve retornar true apenas quando há válido e inválido ao mesmo tempo")
        void deveValidarCombinacoes(int qtdValidos, int qtdInvalidos, boolean esperado) {
            ResultadoDia r = com(resultados(qtdValidos, qtdInvalidos));

            assertThat(r.parcialmenteValido()).isEqualTo(esperado);
        }
    }

    // totalmenteInvalido
    @Nested
    @DisplayName("totalmenteInvalido")
    class TotalmenteInvalido {

        @ParameterizedTest(name = "[{index}] {0} válido(s), {1} inválido(s) → esperado={2}")
        @CsvSource({
                "2, 0, false",
                "1, 0, false",
                "1, 1, false",
                "0, 1, true",
                "0, 2, true"
        })
        @DisplayName("deve retornar true apenas quando nenhum é válido")
        void deveValidarCombinacoes(int qtdValidos, int qtdInvalidos, boolean esperado) {
            ResultadoDia r = com(resultados(qtdValidos, qtdInvalidos));

            assertThat(r.totalmenteInvalido()).isEqualTo(esperado);
        }
    }

    // Combinações mutuamente exclusivas
    @Nested
    @DisplayName("Combinações mutuamente exclusivas")
    class MutuamenteExclusivas {

        @ParameterizedTest(name = "[{index}] {0} válido(s), {1} inválido(s)")
        @CsvSource({
                "2, 0",
                "1, 0",
                "1, 1",
                "0, 1",
                "0, 2"
        })
        @DisplayName("exatamente um dos três métodos deve ser true")
        void exatamenteUmDosTresEhTrue(int qtdValidos, int qtdInvalidos) {
            ResultadoDia r = com(resultados(qtdValidos, qtdInvalidos));

            int total = (r.totalmenteValido() ? 1 : 0)
                    + (r.parcialmenteValido() ? 1 : 0)
                    + (r.totalmenteInvalido() ? 1 : 0);

            assertThat(total).isEqualTo(1);
        }
    }

    // toString
    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("deve conter data e valor total")
        void deveConterDataEValor() {
            ResultadoDia r = new ResultadoDia(
                    DATA,
                    List.of(resultadoValido(), resultadoInvalido()),
                    new BigDecimal("20.00")
            );
            String s = r.toString();

            assertThat(s).contains(DATA.toString());
            assertThat(s).contains("20.00");
        }

        @Test
        @DisplayName("deve conter a quantidade de trechos")
        void deveConterQuantidadeDeTrechos() {
            ResultadoDia r = new ResultadoDia(
                    DATA,
                    List.of(resultadoValido(), resultadoValido(), resultadoInvalido()),
                    new BigDecimal("20.00")
            );

            assertThat(r.toString()).contains("3");
        }
    }

    // equals e hashCode
    @Nested
    @DisplayName("equals e hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("dois resultados com mesmos valores são iguais")
        void mesmosValoresSaoIguais() {
            ResultadoDia a = new ResultadoDia(
                    DATA,
                    List.of(resultadoValido(), resultadoInvalido()),
                    new BigDecimal("20.00")
            );
            ResultadoDia b = new ResultadoDia(
                    DATA,
                    List.of(resultadoValido(), resultadoInvalido()),
                    new BigDecimal("20.00")
            );

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("datas diferentes tornam resultados diferentes")
        void datasDiferentesSaoDiferentes() {
            ResultadoDia a = new ResultadoDia(
                    DATA, List.of(resultadoValido()), BigDecimal.ZERO);
            ResultadoDia b = new ResultadoDia(
                    LocalDate.of(2026, 9, 16), List.of(resultadoValido()), BigDecimal.ZERO);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("valores totais diferentes tornam resultados diferentes")
        void valoresDiferentesSaoDiferentes() {
            ResultadoDia a = new ResultadoDia(
                    DATA, List.of(resultadoValido()), new BigDecimal("10.00"));
            ResultadoDia b = new ResultadoDia(
                    DATA, List.of(resultadoValido()), new BigDecimal("20.00"));

            assertThat(a).isNotEqualTo(b);
        }
    }
}