package dev.douglaslira.sisvaleinterior.domain.service;

import dev.douglaslira.sisvaleinterior.domain.exception.HorarioInvalidoException;
import dev.douglaslira.sisvaleinterior.domain.model.Horario;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoValidacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do Validador de Horário")
class ValidadorHorarioTest {

    private final ValidadorHorario validador = new ValidadorHorario();

    @Nested
    @DisplayName("Validação de ida")
    class ValidacaoIda {

        @ParameterizedTest(name = "[{index}] descida={0}, entrada={1} → esperado={2} ({3})")
        @CsvSource({
                "07:45, 07:30, true,  15 min antes (limite)",
                "07:45, 07:29, false, 16 min antes",
                "07:45, 07:00, false, 45 min antes",
                "07:30, 08:00, true,  depois (sem limite)",
                "07:00, 06:45, true,  15 min antes (limite)",
                "07:00, 06:44, false, 16 min antes",
                "07:00, 07:00, true,  igual",
                "08:00, 07:45, true,  15 min antes (limite)"
        })
        @DisplayName("deve validar corretamente o par de ida")
        void deveValidarParDeIda(String descida, String entrada, boolean esperado, String descricao) {
            ResultadoValidacao resultado = validador.validar(
                    Horario.parse(descida),
                    Horario.parse(entrada)
            );

            assertThat(resultado.valido())
                    .as(descricao)
                    .isEqualTo(esperado);
        }
    }

    @Nested
    @DisplayName("Validação de volta")
    class ValidacaoVolta {

        @ParameterizedTest(name = "[{index}] saída={0}, ônibus={1} → esperado={2}")
        @CsvSource({
                "17:00, 16:45, true",
                "17:00, 16:44, false",
                "17:00, 16:30, false",
                "17:00, 17:30, true",
                "18:00, 17:45, true",
                "18:00, 18:00, true"
        })
        @DisplayName("deve validar corretamente o par de volta")
        void deveValidarParDeVolta(String saida, String onibus, boolean esperado) {
            ResultadoValidacao resultado = validador.validar(
                    Horario.parse(saida),
                    Horario.parse(onibus)
            );

            assertThat(resultado.valido()).isEqualTo(esperado);
        }
    }

    @Nested
    @DisplayName("Tolerância customizada")
    class ToleranciaCustomizada {

        private final ValidadorHorario validador30 = new ValidadorHorario(Duration.ofMinutes(30));

        @Test
        @DisplayName("deve aceitar exatamente no limite da tolerância de 30 min")
        void deveAceitarNoLimiteDe30Minutos() {
            ResultadoValidacao resultado = validador30.validar(
                    Horario.parse("07:45"),
                    Horario.parse("07:15")
            );

            assertThat(resultado.valido()).isTrue();
            assertThat(resultado.diferencaMinutos()).isEqualTo(30);
        }

        @Test
        @DisplayName("deve rejeitar 1 minuto além da tolerância de 30 min")
        void deveRejeitarUmMinutoAlemDe30Minutos() {
            ResultadoValidacao resultado = validador30.validar(
                    Horario.parse("07:45"),
                    Horario.parse("07:14")
            );

            assertThat(resultado.valido()).isFalse();
            assertThat(resultado.diferencaMinutos()).isEqualTo(31);
        }
    }

    @Nested
    @DisplayName("Diferença em minutos")
    class DiferencaMinutos {

        @Test
        @DisplayName("deve retornar 15 para diferença dentro do limite")
        void deveRetornar15ParaDiferencaNoLimite() {
            ResultadoValidacao resultado = validador.validar(
                    Horario.parse("07:45"),
                    Horario.parse("07:30")
            );

            assertThat(resultado.diferencaMinutos()).isEqualTo(15);
        }

        @Test
        @DisplayName("deve retornar 16 para diferença fora do limite")
        void deveRetornar16ParaDiferencaForaDoLimite() {
            ResultadoValidacao resultado = validador.validar(
                    Horario.parse("07:45"),
                    Horario.parse("07:29")
            );

            assertThat(resultado.diferencaMinutos()).isEqualTo(16);
        }

        @Test
        @DisplayName("deve retornar 0 quando horários são iguais")
        void deveRetornarZeroQuandoIguais() {
            ResultadoValidacao resultado = validador.validar(
                    Horario.parse("07:45"),
                    Horario.parse("07:45")
            );

            assertThat(resultado.diferencaMinutos()).isZero();
        }
    }

    @Nested
    @DisplayName("Motivo da invalidação")
    class MotivoInvalido {

        @Test
        @DisplayName("deve conter 'tolerância' no motivo quando inválido")
        void deveConterToleranciaNoMotivo() {
            ResultadoValidacao resultado = validador.validar(
                    Horario.parse("07:45"),
                    Horario.parse("07:29")
            );

            assertThat(resultado.valido()).isFalse();
            assertThat(resultado.motivo()).contains("tolerância");
        }

        @Test
        @DisplayName("deve conter a diferença absoluta no motivo")
        void deveConterDiferencaNoMotivo() {
            ResultadoValidacao resultado = validador.validar(
                    Horario.parse("07:45"),
                    Horario.parse("07:00")
            );

            assertThat(resultado.motivo()).contains("45");
        }
    }

    @Nested
    @DisplayName("Construtor")
    class Construtor {

        @Test
        @DisplayName("deve rejeitar tolerância nula")
        void deveRejeitarToleranciaNula() {
            assertThatThrownBy(() -> new ValidadorHorario(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Tolerância");
        }

        @Test
        @DisplayName("deve rejeitar tolerância zero")
        void deveRejeitarToleranciaZero() {
            assertThatThrownBy(() -> new ValidadorHorario(Duration.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positiva");
        }

        @Test
        @DisplayName("deve rejeitar tolerância negativa")
        void deveRejeitarToleranciaNegativa() {
            assertThatThrownBy(() -> new ValidadorHorario(Duration.ofMinutes(-5)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positiva");
        }

        @Test
        @DisplayName("construtor sem args deve usar tolerância padrão de 15 min")
        void construtorSemArgsDeveUsarPadrao() {
            ValidadorHorario padrao = new ValidadorHorario();

            // 07:45 vs 07:30 → limite exato (15 min) → válido
            assertThat(padrao.validar(Horario.parse("07:45"), Horario.parse("07:30")).valido())
                    .isTrue();

            // 07:45 vs 07:29 → 16 min antes → inválido
            assertThat(padrao.validar(Horario.parse("07:45"), Horario.parse("07:29")).valido())
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("Entradas inválidas no VO Horario")
    class EntradasInvalidas {

        @Test
        @DisplayName("new Horario(null) deve lançar HorarioInvalidoException")
        void deveRejeitarHorarioNulo() {
            assertThatThrownBy(() -> new Horario(null))
                    .isInstanceOf(HorarioInvalidoException.class);
        }

        @Test
        @DisplayName("Horario.parse(null) deve lançar HorarioInvalidoException")
        void deveRejeitarParseNulo() {
            assertThatThrownBy(() -> Horario.parse(null))
                    .isInstanceOf(HorarioInvalidoException.class);
        }

        @Test
        @DisplayName("Horario.parse com formato inválido deve lançar HorarioInvalidoException")
        void deveRejeitarFormatoInvalido() {
            assertThatThrownBy(() -> Horario.parse("7:45"))
                    .isInstanceOf(HorarioInvalidoException.class);

            assertThatThrownBy(() -> Horario.parse("abc"))
                    .isInstanceOf(HorarioInvalidoException.class);

            assertThatThrownBy(() -> Horario.parse("25:00"))
                    .isInstanceOf(HorarioInvalidoException.class);
        }
    }
}