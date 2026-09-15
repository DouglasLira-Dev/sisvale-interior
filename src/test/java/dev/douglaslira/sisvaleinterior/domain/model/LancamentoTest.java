package dev.douglaslira.sisvaleinterior.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes da entidade Lancamento")
class LancamentoTest {

    // Dados-base reutilizados em todos os testes
    private static final Long ID_NULO = null;
    private static final Long SERVIDOR_ID = 1L;
    private static final LocalDate DATA = LocalDate.of(2026, 9, 15);
    private static final Horario DESCIDA = Horario.parse("07:45");
    private static final Horario ENTRADA = Horario.parse("07:30");
    private static final BigDecimal VALOR_IDA = new BigDecimal("20.00");
    private static final Horario SAIDA = Horario.parse("17:00");
    private static final Horario ONIBUS = Horario.parse("16:45");
    private static final BigDecimal VALOR_VOLTA = new BigDecimal("22.00");

    private Lancamento lancamentoValido() {
        return new Lancamento(
                ID_NULO, SERVIDOR_ID, DATA,
                DESCIDA, ENTRADA, VALOR_IDA,
                SAIDA, ONIBUS, VALOR_VOLTA
        );
    }

    // Criação válida
    @Nested
    @DisplayName("Criação válida")
    class CriacaoValida {

        @Test
        @DisplayName("deve criar lançamento com todos os campos corretos")
        void deveCriarLancamentoValido() {
            Lancamento l = lancamentoValido();

            assertThat(l.id()).isNull();
            assertThat(l.servidorId()).isEqualTo(SERVIDOR_ID);
            assertThat(l.data()).isEqualTo(DATA);
            assertThat(l.horaDescida()).isEqualTo(DESCIDA);
            assertThat(l.horaEntrada()).isEqualTo(ENTRADA);
            assertThat(l.valorIda()).isEqualByComparingTo(VALOR_IDA);
            assertThat(l.horaSaida()).isEqualTo(SAIDA);
            assertThat(l.horaOnibus()).isEqualTo(ONIBUS);
            assertThat(l.valorVolta()).isEqualByComparingTo(VALOR_VOLTA);
        }

        @Test
        @DisplayName("deve aceitar id nulo (antes de persistir)")
        void deveAceitarIdNulo() {
            Lancamento l = lancamentoValido();
            assertThat(l.id()).isNull();
        }

        @Test
        @DisplayName("deve aceitar id preenchido (após persistir)")
        void deveAceitarIdPreenchido() {
            Lancamento l = new Lancamento(
                    99L, SERVIDOR_ID, DATA,
                    DESCIDA, ENTRADA, VALOR_IDA,
                    SAIDA, ONIBUS, VALOR_VOLTA
            );
            assertThat(l.id()).isEqualTo(99L);
        }

        @Test
        @DisplayName("deve aceitar valor zero")
        void deveAceitarValorZero() {
            Lancamento l = new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA,
                    DESCIDA, ENTRADA, BigDecimal.ZERO,
                    SAIDA, ONIBUS, BigDecimal.ZERO
            );
            assertThat(l.valorIda()).isEqualByComparingTo("0.00");
            assertThat(l.valorVolta()).isEqualByComparingTo("0.00");
        }
    }

    // Validações de nulos
    @Nested
    @DisplayName("Validações de nulos")
    class ValidacoesDeNulos {

        @Test
        @DisplayName("deve lançar exceção quando servidorId for nulo")
        void deveLancarExcecaoQuandoServidorIdForNulo() {
            assertThatThrownBy(() -> new Lancamento(
                    ID_NULO, null, DATA,
                    DESCIDA, ENTRADA, VALOR_IDA,
                    SAIDA, ONIBUS, VALOR_VOLTA
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Servidor");
        }

        @Test
        @DisplayName("deve lançar exceção quando data for nula")
        void deveLancarExcecaoQuandoDataForNula() {
            assertThatThrownBy(() -> new Lancamento(
                    ID_NULO, SERVIDOR_ID, null,
                    DESCIDA, ENTRADA, VALOR_IDA,
                    SAIDA, ONIBUS, VALOR_VOLTA
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Data");
        }

        @Test
        @DisplayName("deve lançar exceção quando horaDescida for nula")
        void deveLancarExcecaoQuandoHoraDescidaForNula() {
            assertThatThrownBy(() -> new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA,
                    null, ENTRADA, VALOR_IDA,
                    SAIDA, ONIBUS, VALOR_VOLTA
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("descida");
        }

        @Test
        @DisplayName("deve lançar exceção quando horaEntrada for nula")
        void deveLancarExcecaoQuandoHoraEntradaForNula() {
            assertThatThrownBy(() -> new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA,
                    DESCIDA, null, VALOR_IDA,
                    SAIDA, ONIBUS, VALOR_VOLTA
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("entrada");
        }

        @Test
        @DisplayName("deve lançar exceção quando horaSaida for nula")
        void deveLancarExcecaoQuandoHoraSaidaForNula() {
            assertThatThrownBy(() -> new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA,
                    DESCIDA, ENTRADA, VALOR_IDA,
                    null, ONIBUS, VALOR_VOLTA
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("saída");
        }

        @Test
        @DisplayName("deve lançar exceção quando horaOnibus for nula")
        void deveLancarExcecaoQuandoHoraOnibusForNula() {
            assertThatThrownBy(() -> new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA,
                    DESCIDA, ENTRADA, VALOR_IDA,
                    SAIDA, null, VALOR_VOLTA
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ônibus");
        }

        @Test
        @DisplayName("deve lançar exceção quando valorIda for nulo")
        void deveLancarExcecaoQuandoValorIdaForNulo() {
            assertThatThrownBy(() -> new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA,
                    DESCIDA, ENTRADA, null,
                    SAIDA, ONIBUS, VALOR_VOLTA
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Valor");
        }

        @Test
        @DisplayName("deve lançar exceção quando valorVolta for nulo")
        void deveLancarExcecaoQuandoValorVoltaForNulo() {
            assertThatThrownBy(() -> new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA,
                    DESCIDA, ENTRADA, VALOR_IDA,
                    SAIDA, ONIBUS, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Valor");
        }
    }

    // Validações de valores
    @Nested
    @DisplayName("Validações de valores")
    class ValidacoesDeValores {

        @Test
        @DisplayName("deve lançar exceção quando valorIda for negativo")
        void deveLancarExcecaoQuandoValorIdaForNegativo() {
            assertThatThrownBy(() -> new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA,
                    DESCIDA, ENTRADA, new BigDecimal("-0.01"),
                    SAIDA, ONIBUS, VALOR_VOLTA
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negativo");
        }

        @Test
        @DisplayName("deve lançar exceção quando valorVolta for negativo")
        void deveLancarExcecaoQuandoValorVoltaForNegativo() {
            assertThatThrownBy(() -> new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA,
                    DESCIDA, ENTRADA, VALOR_IDA,
                    SAIDA, ONIBUS, new BigDecimal("-0.01")
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negativo");
        }
    }

    // Normalização de valores (escala 2)
    @Nested
    @DisplayName("Normalização de valores")
    class NormalizacaoValores {

        private Lancamento comValorIda(String valor) {
            return new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA,
                    DESCIDA, ENTRADA, new BigDecimal(valor),
                    SAIDA, ONIBUS, VALOR_VOLTA
            );
        }

        @Test
        @DisplayName("deve normalizar 20.5 para 20.50")
        void deveNormalizarUmaCasaDecimal() {
            Lancamento l = comValorIda("20.5");
            assertThat(l.valorIda()).isEqualByComparingTo("20.50");
            assertThat(l.valorIda().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("deve normalizar 20 para 20.00")
        void deveNormalizarSemCasasDecimais() {
            Lancamento l = comValorIda("20");
            assertThat(l.valorIda()).isEqualByComparingTo("20.00");
            assertThat(l.valorIda().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("deve arredondar 20.555 para 20.56 (HALF_UP)")
        void deveArredondarTresCasas() {
            Lancamento l = comValorIda("20.555");
            assertThat(l.valorIda()).isEqualByComparingTo("20.56");
            assertThat(l.valorIda().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("deve arredondar 20.554 para 20.55 (HALF_UP)")
        void deveArredondarParaBaixo() {
            Lancamento l = comValorIda("20.554");
            assertThat(l.valorIda()).isEqualByComparingTo("20.55");
            assertThat(l.valorIda().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("deve manter escala 2 quando já está em escala 2")
        void deveManterEscala2() {
            Lancamento l = comValorIda("20.50");
            assertThat(l.valorIda()).isEqualTo(new BigDecimal("20.50"));
            assertThat(l.valorIda().scale()).isEqualTo(2);
        }
    }

    // equals e hashCode
    @Nested
    @DisplayName("equals e hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("deve considerar iguais lançamentos com mesmo servidorId e data")
        void deveConsiderarIguaisLancamentosComMesmoServidorIdEData() {
            Lancamento a = lancamentoValido();
            Lancamento b = lancamentoValido();

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("deve considerar diferentes lançamentos com servidorId diferente")
        void deveConsiderarDiferentesServidorIdDiferente() {
            Lancamento a = lancamentoValido();
            Lancamento b = new Lancamento(
                    99L, 2L, DATA,
                    DESCIDA, ENTRADA, VALOR_IDA,
                    SAIDA, ONIBUS, VALOR_VOLTA
            );

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("deve considerar diferentes lançamentos com data diferente")
        void deveConsiderarDiferentesDataDiferente() {
            Lancamento a = lancamentoValido();
            Lancamento b = new Lancamento(
                    ID_NULO, SERVIDOR_ID, LocalDate.of(2026, 9, 16),
                    DESCIDA, ENTRADA, VALOR_IDA,
                    SAIDA, ONIBUS, VALOR_VOLTA
            );

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("id diferente NÃO afeta igualdade (documenta comportamento)")
        void idDiferenteNaoAfetaIgualdade() {
            Lancamento a = new Lancamento(
                    1L, SERVIDOR_ID, DATA,
                    DESCIDA, ENTRADA, VALOR_IDA,
                    SAIDA, ONIBUS, VALOR_VOLTA
            );
            Lancamento b = new Lancamento(
                    2L, SERVIDOR_ID, DATA,
                    DESCIDA, ENTRADA, VALOR_IDA,
                    SAIDA, ONIBUS, VALOR_VOLTA
            );

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("horários diferentes NÃO afetam igualdade (só servidorId + data importam)")
        void horariosDiferentesNaoAfetamIgualdade() {
            Lancamento a = lancamentoValido();
            Lancamento b = new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA,
                    Horario.parse("08:00"), Horario.parse("08:15"), new BigDecimal("99.99"),
                    Horario.parse("18:00"), Horario.parse("18:15"), new BigDecimal("99.99")
            );

            assertThat(a).isEqualTo(b);
        }
    }

    // toString
    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("deve conter id, servidorId, data e valores")
        void deveConterDadosPrincipais() {
            Lancamento l = lancamentoValido();
            String s = l.toString();

            assertThat(s).contains("id=");
            assertThat(s).contains("servidorId=" + SERVIDOR_ID);
            assertThat(s).contains("data=" + DATA);
            assertThat(s).contains("valorIda=20.00");
            assertThat(s).contains("valorVolta=22.00");
        }

        @Test
        @DisplayName("não deve conter referência a horários (resumido)")
        void naoDeveConterHorarios() {
            Lancamento l = lancamentoValido();
            String s = l.toString();

            assertThat(s).doesNotContain("07:45");
            assertThat(s).doesNotContain("17:00");
        }
    }
}