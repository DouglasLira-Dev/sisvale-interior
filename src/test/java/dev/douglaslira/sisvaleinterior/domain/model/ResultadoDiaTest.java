package dev.douglaslira.sisvaleinterior.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do value object ResultadoDia")
class ResultadoDiaTest {

    private static final LocalDate DATA = LocalDate.of(2026, 9, 15);

    // Helpers de ResultadoValidacao
    private ResultadoValidacao valido() {
        return ResultadoValidacao.valido(0);
    }

    private ResultadoValidacao invalido() {
        return ResultadoValidacao.invalido(-30, "fora da tolerância");
    }

    private ResultadoDia com(ResultadoValidacao ida, ResultadoValidacao volta) {
        return new ResultadoDia(DATA, ida, volta, new BigDecimal("10.00"));
    }

    // Criação válida
    @Nested
    @DisplayName("Criação válida")
    class CriacaoValida {

        @Test
        @DisplayName("deve criar com data, validações e valor total")
        void deveCriarComTodosOsCampos() {
            ResultadoDia r = new ResultadoDia(DATA, valido(), valido(), new BigDecimal("42.00"));

            assertThat(r.data()).isEqualTo(DATA);
            assertThat(r.validacaoIda()).isNotNull();
            assertThat(r.validacaoVolta()).isNotNull();
            assertThat(r.valorTotalDia()).isEqualByComparingTo("42.00");
        }

        @Test
        @DisplayName("deve aceitar valor zero")
        void deveAceitarValorZero() {
            ResultadoDia r = new ResultadoDia(DATA, valido(), invalido(), BigDecimal.ZERO);

            assertThat(r.valorTotalDia()).isEqualByComparingTo("0.00");
            assertThat(r.valorTotalDia().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("deve normalizar valor para escala 2")
        void deveNormalizarValorParaEscala2() {
            ResultadoDia r = new ResultadoDia(DATA, valido(), valido(), new BigDecimal("42.5"));

            assertThat(r.valorTotalDia()).isEqualByComparingTo("42.50");
            assertThat(r.valorTotalDia().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("deve arredondar valor com mais de 2 casas (HALF_UP)")
        void deveArredondarValor() {
            ResultadoDia r = new ResultadoDia(DATA, valido(), valido(), new BigDecimal("42.555"));

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
            assertThatThrownBy(() -> new ResultadoDia(null, valido(), valido(), BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Data");
        }

        @Test
        @DisplayName("deve lançar exceção quando validacaoIda for nula")
        void deveLancarExcecaoQuandoValidacaoIdaForNula() {
            assertThatThrownBy(() -> new ResultadoDia(DATA, null, valido(), BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ida");
        }

        @Test
        @DisplayName("deve lançar exceção quando validacaoVolta for nula")
        void deveLancarExcecaoQuandoValidacaoVoltaForNula() {
            assertThatThrownBy(() -> new ResultadoDia(DATA, valido(), null, BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("volta");
        }

        @Test
        @DisplayName("deve lançar exceção quando valorTotalDia for nulo")
        void deveLancarExcecaoQuandoValorTotalDiaForNulo() {
            assertThatThrownBy(() -> new ResultadoDia(DATA, valido(), valido(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Valor");
        }
    }

    // totalmenteValido — TT verdadeiro, o resto falso
    @Nested
    @DisplayName("totalmenteValido")
    class TotalmenteValido {

        @ParameterizedTest(name = "[{index}] ida={0}, volta={1} → esperado={2}")
        @CsvSource({
                "true,  true,  true",
                "true,  false, false",
                "false, true,  false",
                "false, false, false"
        })
        @DisplayName("deve retornar true apenas quando ida e volta são válidas")
        void deveValidarCombinacoes(boolean idaValida, boolean voltaValida, boolean esperado) {
            ResultadoValidacao ida = idaValida ? valido() : invalido();
            ResultadoValidacao volta = voltaValida ? valido() : invalido();

            ResultadoDia r = com(ida, volta);

            assertThat(r.totalmenteValido()).isEqualTo(esperado);
        }
    }

    // parcialmenteValido — TF e FT verdadeiros, TT e FF falsos
    @Nested
    @DisplayName("parcialmenteValido")
    class ParcialmenteValido {

        @ParameterizedTest(name = "[{index}] ida={0}, volta={1} → esperado={2}")
        @CsvSource({
                "true,  true,  false",
                "true,  false, true",
                "false, true,  true",
                "false, false, false"
        })
        @DisplayName("deve retornar true apenas quando exatamente um par é válido")
        void deveValidarCombinacoes(boolean idaValida, boolean voltaValida, boolean esperado) {
            ResultadoValidacao ida = idaValida ? valido() : invalido();
            ResultadoValidacao volta = voltaValida ? valido() : invalido();

            ResultadoDia r = com(ida, volta);

            assertThat(r.parcialmenteValido()).isEqualTo(esperado);
        }
    }

    // totalmenteInvalido — FF verdadeiro, o resto falso
    @Nested
    @DisplayName("totalmenteInvalido")
    class TotalmenteInvalido {

        @ParameterizedTest(name = "[{index}] ida={0}, volta={1} → esperado={2}")
        @CsvSource({
                "true,  true,  false",
                "true,  false, false",
                "false, true,  false",
                "false, false, true"
        })
        @DisplayName("deve retornar true apenas quando ida e volta são inválidas")
        void deveValidarCombinacoes(boolean idaValida, boolean voltaValida, boolean esperado) {
            ResultadoValidacao ida = idaValida ? valido() : invalido();
            ResultadoValidacao volta = voltaValida ? valido() : invalido();

            ResultadoDia r = com(ida, volta);

            assertThat(r.totalmenteInvalido()).isEqualTo(esperado);
        }
    }

    // Combinações mutuamente exclusivas
    @Nested
    @DisplayName("Combinações mutuamente exclusivas")
    class MutuamenteExclusivas {

        @ParameterizedTest(name = "[{index}] ida={0}, volta={1}")
        @CsvSource({
                "true,  true",
                "true,  false",
                "false, true",
                "false, false"
        })
        @DisplayName("exatamente um dos três métodos deve ser true")
        void exatamenteUmDosTresEhTrue(boolean idaValida, boolean voltaValida) {
            ResultadoValidacao ida = idaValida ? valido() : invalido();
            ResultadoValidacao volta = voltaValida ? valido() : invalido();

            ResultadoDia r = com(ida, volta);

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
            ResultadoDia r = new ResultadoDia(DATA, valido(), invalido(), new BigDecimal("20.00"));
            String s = r.toString();

            assertThat(s).contains(DATA.toString());
            assertThat(s).contains("20.00");
        }

        @Test
        @DisplayName("deve conter rótulos 'válido' e 'inválido'")
        void deveConterRotulos() {
            ResultadoDia r = new ResultadoDia(DATA, valido(), invalido(), new BigDecimal("20.00"));
            String s = r.toString();

            assertThat(s).contains("válido");
            assertThat(s).contains("inválido");
        }
    }

    // equals e hashCode (gerados pelo record)
    @Nested
    @DisplayName("equals e hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("dois resultados com mesmos valores são iguais")
        void mesmosValoresSaoIguais() {
            ResultadoDia a = new ResultadoDia(DATA, valido(), invalido(), new BigDecimal("20.00"));
            ResultadoDia b = new ResultadoDia(DATA, valido(), invalido(), new BigDecimal("20.00"));

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("datas diferentes tornam resultados diferentes")
        void datasDiferentesSaoDiferentes() {
            ResultadoDia a = new ResultadoDia(DATA, valido(), valido(), BigDecimal.ZERO);
            ResultadoDia b = new ResultadoDia(LocalDate.of(2026, 9, 16), valido(), valido(), BigDecimal.ZERO);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("valores totais diferentes tornam resultados diferentes")
        void valoresDiferentesSaoDiferentes() {
            ResultadoDia a = new ResultadoDia(DATA, valido(), valido(), new BigDecimal("10.00"));
            ResultadoDia b = new ResultadoDia(DATA, valido(), valido(), new BigDecimal("20.00"));

            assertThat(a).isNotEqualTo(b);
        }
    }
}