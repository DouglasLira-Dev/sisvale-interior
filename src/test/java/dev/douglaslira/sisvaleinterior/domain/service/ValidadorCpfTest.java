package dev.douglaslira.sisvaleinterior.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do Validador de CPF")
class ValidadorCpfTest {

    @Nested
    @DisplayName("CPFs válidos")
    class CpfsValidos {

        @ParameterizedTest(name = "[{index}] {0} deve ser válido")
        @ValueSource(strings = {
                "11144477735",       // sem máscara
                "111.444.777-35",    // com máscara
                "12345678909",       // válido
                "52998224725"        // válido
        })
        @DisplayName("deve retornar true para CPF válido")
        void deveRetornarTrueParaCpfValido(String cpf) {
            assertThat(ValidadorCpf.isValido(cpf)).isTrue();
        }
    }

    @Nested
    @DisplayName("CPFs inválidos")
    class CpfsInvalidos {

        @ParameterizedTest(name = "[{index}] {0} deve ser inválido")
        @ValueSource(strings = {
                "11144477736",       // dígito verificador errado (DV2)
                "1234567890",        // 10 dígitos
                "123456789012",      // 12 dígitos
                "111.444.777-3A",    // contém letra → normaliza para 10 dígitos
                "11111111111",       // sequência repetida
                "00000000000",       // sequência repetida
                "99999999999"        // sequência repetida
        })
        @DisplayName("deve retornar false para CPF inválido")
        void deveRetornarFalseParaCpfInvalido(String cpf) {
            assertThat(ValidadorCpf.isValido(cpf)).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para dígito verificador errado")
        void deveRetornarFalseParaCpfComDigitoVerificadorErrado() {
            assertThat(ValidadorCpf.isValido("11144477736")).isFalse();
        }
    }

    @Nested
    @DisplayName("Casos de borda")
    class CasosDeBorda {

        @Test
        @DisplayName("deve retornar false para null")
        void deveRetornarFalseParaNull() {
            assertThat(ValidadorCpf.isValido(null)).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para string vazia")
        void deveRetornarFalseParaStringVazia() {
            assertThat(ValidadorCpf.isValido("")).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para string só com espaços")
        void deveRetornarFalseParaStringComEspacos() {
            assertThat(ValidadorCpf.isValido(" ")).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para string só com máscara")
        void deveRetornarFalseParaSoMascara() {
            assertThat(ValidadorCpf.isValido("...---")).isFalse();
        }

        @Test
        @DisplayName("deve aceitar CPF com espaço no final (normalização)")
        void deveAceitarCpfComEspacoNoFinal() {
            assertThat(ValidadorCpf.isValido("111.444.777-35 ")).isTrue();
        }

        @Test
        @DisplayName("deve aceitar CPF com espaço no início (normalização)")
        void deveAceitarCpfComEspacoNoInicio() {
            assertThat(ValidadorCpf.isValido(" 11144477735")).isTrue();
        }

        @Test
        @DisplayName("deve aceitar CPF com newline ao redor (normalização)")
        void deveAceitarCpfComNewline() {
            assertThat(ValidadorCpf.isValido("\n11144477735\n")).isTrue();
        }
    }
}