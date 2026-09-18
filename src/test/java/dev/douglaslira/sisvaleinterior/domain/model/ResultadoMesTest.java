package dev.douglaslira.sisvaleinterior.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do value object ResultadoMes")
class ResultadoMesTest {

    private static final Long SERVIDOR_ID = 1L;
    private static final YearMonth MES = YearMonth.of(2026, 9);
    
    // Helpers de ResultadoValidacao
    private ResultadoValidacao valido() {
        return ResultadoValidacao.valido(0);
    }

    private ResultadoValidacao invalido() {
        return ResultadoValidacao.invalido(-30, "fora da tolerância");
    }

    // Helpers de ResultadoDia (multi-trecho)

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

    private ResultadoDia diaTotalmenteValido(LocalDate data) {
        List<ResultadoTrecho> resultados = List.of(resultadoValido(), resultadoValido());
        return new ResultadoDia(data, resultados, new BigDecimal("42.00"));
    }

    private ResultadoDia diaParcial(LocalDate data) {
        List<ResultadoTrecho> resultados = List.of(resultadoValido(), resultadoInvalido());
        return new ResultadoDia(data, resultados, new BigDecimal("20.00"));
    }

    private ResultadoDia diaTotalmenteInvalido(LocalDate data) {
        List<ResultadoTrecho> resultados = List.of(resultadoInvalido(), resultadoInvalido());
        return new ResultadoDia(data, resultados, BigDecimal.ZERO);
    }

    private ResultadoMes comDias(List<ResultadoDia> dias) {
        BigDecimal total = dias.stream()
                .map(ResultadoDia::valorTotalDia)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResultadoMes(SERVIDOR_ID, MES, dias, total);
    }

    private ResultadoMes mesVazio() {
        return new ResultadoMes(SERVIDOR_ID, MES, List.of(), BigDecimal.ZERO);
    }

    // Criação válida
    @Nested
    @DisplayName("Criação válida")
    class CriacaoValida {

        @Test
        @DisplayName("deve criar com servidorId, mesAno, dias e valor total")
        void deveCriarComTodosOsCampos() {
            ResultadoDia dia = diaTotalmenteValido(LocalDate.of(2026, 9, 15));

            ResultadoMes r = new ResultadoMes(SERVIDOR_ID, MES, List.of(dia), new BigDecimal("42.00"));

            assertThat(r.servidorId()).isEqualTo(SERVIDOR_ID);
            assertThat(r.mesAno()).isEqualTo(MES);
            assertThat(r.dias()).hasSize(1);
            assertThat(r.valorTotalMes()).isEqualByComparingTo("42.00");
        }

        @Test
        @DisplayName("deve aceitar lista vazia")
        void deveAceitarListaVazia() {
            ResultadoMes r = mesVazio();

            assertThat(r.dias()).isEmpty();
            assertThat(r.valorTotalMes()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("deve aceitar vários dias")
        void deveAceitarVariosDias() {
            ResultadoMes r = comDias(List.of(
                    diaTotalmenteValido(LocalDate.of(2026, 9, 1)),
                    diaParcial(LocalDate.of(2026, 9, 2)),
                    diaTotalmenteInvalido(LocalDate.of(2026, 9, 3))
            ));

            assertThat(r.dias()).hasSize(3);
        }
    }

    // Validação de nulos
    @Nested
    @DisplayName("Validação de nulos")
    class ValidacaoDeNulos {

        @Test
        @DisplayName("deve lançar exceção quando servidorId for nulo")
        void deveLancarExcecaoQuandoServidorIdForNulo() {
            assertThatThrownBy(() -> new ResultadoMes(null, MES, List.of(), BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Servidor");
        }

        @Test
        @DisplayName("deve lançar exceção quando mesAno for nulo")
        void deveLancarExcecaoQuandoMesAnoForNulo() {
            assertThatThrownBy(() -> new ResultadoMes(SERVIDOR_ID, null, List.of(), BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Mês");
        }

        @Test
        @DisplayName("deve lançar exceção quando lista de dias for nula")
        void deveLancarExcecaoQuandoListaDeDiasForNula() {
            assertThatThrownBy(() -> new ResultadoMes(SERVIDOR_ID, MES, null, BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dias");
        }

        @Test
        @DisplayName("deve lançar exceção quando valorTotalMes for nulo")
        void deveLancarExcecaoQuandoValorTotalMesForNulo() {
            assertThatThrownBy(() -> new ResultadoMes(SERVIDOR_ID, MES, List.of(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Valor");
        }
    }

    // Imutabilidade
    @Nested
    @DisplayName("Imutabilidade")
    class Imutabilidade {

        @Test
        @DisplayName("lista dias() não pode ser modificada")
        void diasNaoPodeSerModificada() {
            ResultadoMes r = comDias(List.of(diaTotalmenteValido(LocalDate.of(2026, 9, 1))));

            assertThatThrownBy(() -> r.dias().add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("lista diasComAlgumValor() não pode ser modificada")
        void diasComAlgumValorNaoPodeSerModificada() {
            ResultadoMes r = comDias(List.of(diaTotalmenteValido(LocalDate.of(2026, 9, 1))));

            assertThatThrownBy(() -> r.diasComAlgumValor().add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("lista diasInvalidos() não pode ser modificada")
        void diasInvalidosNaoPodeSerModificada() {
            ResultadoMes r = comDias(List.of(diaTotalmenteInvalido(LocalDate.of(2026, 9, 1))));

            assertThatThrownBy(() -> r.diasInvalidos().add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("lista diasParciais() não pode ser modificada")
        void diasParciaisNaoPodeSerModificada() {
            ResultadoMes r = comDias(List.of(diaParcial(LocalDate.of(2026, 9, 1))));

            assertThatThrownBy(() -> r.diasParciais().add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("alterar a lista original após a criação não afeta o ResultadoMes")
        void alteracaoNaListaOriginalNaoAfeta() {
            List<ResultadoDia> original = new ArrayList<>();
            original.add(diaTotalmenteValido(LocalDate.of(2026, 9, 1)));

            ResultadoMes r = new ResultadoMes(SERVIDOR_ID, MES, original, new BigDecimal("42.00"));

            // Modifica a lista original
            original.add(diaTotalmenteValido(LocalDate.of(2026, 9, 2)));

            // O ResultadoMes não deve refletir a alteração
            assertThat(r.dias()).hasSize(1);
        }
    }

    // Filtro: diasComAlgumValor
    @Nested
    @DisplayName("Filtro diasComAlgumValor")
    class FiltroDiasComAlgumValor {

        @Test
        @DisplayName("2 válidos + 1 inválido → retorna 2")
        void doisValidosUmInvalido() {
            ResultadoMes r = comDias(List.of(
                    diaTotalmenteValido(LocalDate.of(2026, 9, 1)),
                    diaTotalmenteValido(LocalDate.of(2026, 9, 2)),
                    diaTotalmenteInvalido(LocalDate.of(2026, 9, 3))
            ));

            assertThat(r.diasComAlgumValor()).hasSize(2);
        }

        @Test
        @DisplayName("parciais contam como 'com algum valor'")
        void parciaisContam() {
            ResultadoMes r = comDias(List.of(
                    diaParcial(LocalDate.of(2026, 9, 1)),
                    diaParcial(LocalDate.of(2026, 9, 2))
            ));

            assertThat(r.diasComAlgumValor()).hasSize(2);
        }

        @Test
        @DisplayName("1 total válido + 2 parciais + 1 inválido → retorna 3")
        void misturado() {
            ResultadoMes r = comDias(List.of(
                    diaTotalmenteValido(LocalDate.of(2026, 9, 1)),
                    diaParcial(LocalDate.of(2026, 9, 2)),
                    diaParcial(LocalDate.of(2026, 9, 3)),
                    diaTotalmenteInvalido(LocalDate.of(2026, 9, 4))
            ));

            assertThat(r.diasComAlgumValor()).hasSize(3);
        }

        @Test
        @DisplayName("lista vazia → vazia")
        void listaVazia() {
            assertThat(mesVazio().diasComAlgumValor()).isEmpty();
        }
    }

    // Filtro: diasInvalidos
    @Nested
    @DisplayName("Filtro diasInvalidos")
    class FiltroDiasInvalidos {

        @Test
        @DisplayName("2 válidos + 1 inválido → retorna 1")
        void doisValidosUmInvalido() {
            ResultadoMes r = comDias(List.of(
                    diaTotalmenteValido(LocalDate.of(2026, 9, 1)),
                    diaTotalmenteValido(LocalDate.of(2026, 9, 2)),
                    diaTotalmenteInvalido(LocalDate.of(2026, 9, 3))
            ));

            assertThat(r.diasInvalidos()).hasSize(1);
        }

        @Test
        @DisplayName("só válidos → vazia")
        void soValidos() {
            ResultadoMes r = comDias(List.of(
                    diaTotalmenteValido(LocalDate.of(2026, 9, 1)),
                    diaParcial(LocalDate.of(2026, 9, 2))
            ));

            assertThat(r.diasInvalidos()).isEmpty();
        }

        @Test
        @DisplayName("lista vazia → vazia")
        void listaVazia() {
            assertThat(mesVazio().diasInvalidos()).isEmpty();
        }
    }

    // Filtro: diasParciais
    @Nested
    @DisplayName("Filtro diasParciais")
    class FiltroDiasParciais {

        @Test
        @DisplayName("1 total válido + 2 parciais + 1 inválido → retorna 2")
        void misturado() {
            ResultadoMes r = comDias(List.of(
                    diaTotalmenteValido(LocalDate.of(2026, 9, 1)),
                    diaParcial(LocalDate.of(2026, 9, 2)),
                    diaParcial(LocalDate.of(2026, 9, 3)),
                    diaTotalmenteInvalido(LocalDate.of(2026, 9, 4))
            ));

            assertThat(r.diasParciais()).hasSize(2);
        }

        @Test
        @DisplayName("só totais válidos → vazia")
        void soTotaisValidos() {
            ResultadoMes r = comDias(List.of(
                    diaTotalmenteValido(LocalDate.of(2026, 9, 1)),
                    diaTotalmenteValido(LocalDate.of(2026, 9, 2))
            ));

            assertThat(r.diasParciais()).isEmpty();
        }

        @Test
        @DisplayName("lista vazia → vazia")
        void listaVazia() {
            assertThat(mesVazio().diasParciais()).isEmpty();
        }
    }

    // Ordenação
    @Nested
    @DisplayName("Ordenação")
    class Ordenacao {

        @Test
        @DisplayName("deve ordenar os dias por data crescente")
        void deveOrdenarPorDataCrescente() {
            // Passa fora de ordem: 20, 5, 15
            ResultadoMes r = comDias(List.of(
                    diaTotalmenteValido(LocalDate.of(2026, 9, 20)),
                    diaTotalmenteValido(LocalDate.of(2026, 9, 5)),
                    diaTotalmenteValido(LocalDate.of(2026, 9, 15))
            ));

            assertThat(r.dias())
                    .extracting(ResultadoDia::data)
                    .containsExactly(
                            LocalDate.of(2026, 9, 5),
                            LocalDate.of(2026, 9, 15),
                            LocalDate.of(2026, 9, 20)
                    );
        }

        @Test
        @DisplayName("lista já ordenada continua ordenada")
        void listaJaOrdenada() {
            ResultadoMes r = comDias(List.of(
                    diaTotalmenteValido(LocalDate.of(2026, 9, 1)),
                    diaTotalmenteValido(LocalDate.of(2026, 9, 2)),
                    diaTotalmenteValido(LocalDate.of(2026, 9, 3))
            ));

            assertThat(r.dias())
                    .extracting(ResultadoDia::data)
                    .isSorted();
        }
    }

    // Normalização de valores
    @Nested
    @DisplayName("Normalização de valores")
    class Normalizacao {

        @Test
        @DisplayName("deve normalizar 100.5 para 100.50")
        void deveNormalizarUmaCasaDecimal() {
            ResultadoMes r = new ResultadoMes(SERVIDOR_ID, MES, List.of(), new BigDecimal("100.5"));

            assertThat(r.valorTotalMes()).isEqualByComparingTo("100.50");
            assertThat(r.valorTotalMes().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("deve arredondar 100.555 para 100.56 (HALF_UP)")
        void deveArredondar() {
            ResultadoMes r = new ResultadoMes(SERVIDOR_ID, MES, List.of(), new BigDecimal("100.555"));

            assertThat(r.valorTotalMes()).isEqualByComparingTo("100.56");
            assertThat(r.valorTotalMes().scale()).isEqualTo(2);
        }
    }

    // toString
    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("deve conter servidorId, mesAno, quantidade de dias e total")
        void deveConterCamposPrincipais() {
            ResultadoMes r = comDias(List.of(
                    diaTotalmenteValido(LocalDate.of(2026, 9, 1)),
                    diaTotalmenteValido(LocalDate.of(2026, 9, 2))
            ));

            String s = r.toString();

            assertThat(s).contains("servidorId=" + SERVIDOR_ID);
            assertThat(s).contains("mesAno=" + MES);
            assertThat(s).contains("dias=2");
            assertThat(s).contains("valorTotalMes=84.00");
        }
    }

    // equals e hashCode
    @Nested
    @DisplayName("equals e hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("mesmos valores → iguais com mesmo hashCode")
        void mesmosValores() {
            ResultadoDia dia = diaTotalmenteValido(LocalDate.of(2026, 9, 1));

            ResultadoMes a = new ResultadoMes(SERVIDOR_ID, MES, List.of(dia), new BigDecimal("42.00"));
            ResultadoMes b = new ResultadoMes(SERVIDOR_ID, MES, List.of(dia), new BigDecimal("42.00"));

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("servidorId diferente → diferentes")
        void servidorIdDiferente() {
            ResultadoMes a = mesVazio();
            ResultadoMes b = new ResultadoMes(99L, MES, List.of(), BigDecimal.ZERO);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("mesAno diferente → diferentes")
        void mesAnoDiferente() {
            ResultadoMes a = mesVazio();
            ResultadoMes b = new ResultadoMes(SERVIDOR_ID, YearMonth.of(2026, 10), List.of(), BigDecimal.ZERO);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("valor total diferente → diferentes")
        void valorTotalDiferente() {
            ResultadoMes a = new ResultadoMes(SERVIDOR_ID, MES, List.of(), new BigDecimal("10.00"));
            ResultadoMes b = new ResultadoMes(SERVIDOR_ID, MES, List.of(), new BigDecimal("20.00"));

            assertThat(a).isNotEqualTo(b);
        }
    }
}