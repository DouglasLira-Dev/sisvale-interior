package dev.douglaslira.sisvaleinterior.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do value object ResultadoValidacao")
class ResultadoValidacaoTest {

    // Métodos de fábrica
    @Nested
    @DisplayName("Métodos de fábrica")
    class MetodosFabrica {

        @Test
        @DisplayName("valido(15) deve criar resultado válido com motivo consistente")
        void validoDeveCriarResultadoValido() {
            ResultadoValidacao r = ResultadoValidacao.valido(15);

            assertThat(r.valido()).isTrue();
            assertThat(r.diferencaMinutos()).isEqualTo(15);
            assertThat(r.motivo()).isNotNull().isNotBlank();
            assertThat(r.motivo()).containsIgnoringCase("tolerância");
        }

        @Test
        @DisplayName("valido(0) deve aceitar diferença zero")
        void validoComZero() {
            ResultadoValidacao r = ResultadoValidacao.valido(0);

            assertThat(r.valido()).isTrue();
            assertThat(r.diferencaMinutos()).isZero();
        }

        @Test
        @DisplayName("valido deve usar o mesmo motivo em chamadas diferentes")
        void validoUsaMesmoMotivoSempre() {
            ResultadoValidacao a = ResultadoValidacao.valido(15);
            ResultadoValidacao b = ResultadoValidacao.valido(99);

            assertThat(a.motivo()).isEqualTo(b.motivo());
        }

        @Test
        @DisplayName("invalido(-30, \"motivo\") deve criar resultado inválido")
        void invalidoDeveCriarResultadoInvalido() {
            ResultadoValidacao r = ResultadoValidacao.invalido(-30, "motivo");

            assertThat(r.valido()).isFalse();
            assertThat(r.motivo()).isEqualTo("motivo");
            assertThat(r.diferencaMinutos()).isEqualTo(-30);
        }

        @Test
        @DisplayName("invalido(30, \"outro motivo\") deve propagar o motivo")
        void invalidoPropagaMotivo() {
            ResultadoValidacao r = ResultadoValidacao.invalido(30, "outro motivo");

            assertThat(r.valido()).isFalse();
            assertThat(r.motivo()).isEqualTo("outro motivo");
            assertThat(r.diferencaMinutos()).isEqualTo(30);
        }

        @Test
        @DisplayName("invalido deve aceitar diferença negativa (uso em isolamento)")
        void invalidoAceitaDiferencaNegativa() {
            ResultadoValidacao r = ResultadoValidacao.invalido(-5, "fora do limite");

            assertThat(r.valido()).isFalse();
            assertThat(r.diferencaMinutos()).isEqualTo(-5);
        }
    }

    // Compact constructor
    @Nested
    @DisplayName("Compact constructor")
    class CompactConstructor {

        @Test
        @DisplayName("deve lançar exceção quando motivo for nulo")
        void deveLancarExcecaoQuandoMotivoForNulo() {
            assertThatThrownBy(() -> new ResultadoValidacao(true, null, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("motivo");
        }

        @Test
        @DisplayName("deve lançar exceção quando motivo for vazio")
        void deveLancarExcecaoQuandoMotivoForVazio() {
            assertThatThrownBy(() -> new ResultadoValidacao(true, "", 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("motivo");
        }

        @Test
        @DisplayName("deve lançar exceção quando motivo for só espaços")
        void deveLancarExcecaoQuandoMotivoForSoEspacos() {
            assertThatThrownBy(() -> new ResultadoValidacao(true, "   ", 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("motivo");
        }

        @Test
        @DisplayName("deve lançar exceção quando motivo for nulo mesmo em resultado inválido")
        void deveLancarExcecaoQuandoInvalidoComMotivoNulo() {
            assertThatThrownBy(() -> new ResultadoValidacao(false, null, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve criar resultado válido com todos os campos corretos")
        void deveCriarResultadoValidoComCamposCorretos() {
            ResultadoValidacao r = new ResultadoValidacao(true, "ok", 5);

            assertThat(r.valido()).isTrue();
            assertThat(r.motivo()).isEqualTo("ok");
            assertThat(r.diferencaMinutos()).isEqualTo(5);
        }

        @Test
        @DisplayName("deve criar resultado inválido com diferença negativa")
        void deveCriarResultadoInvalidoComDiferencaNegativa() {
            ResultadoValidacao r = new ResultadoValidacao(false, "erro", -5);

            assertThat(r.valido()).isFalse();
            assertThat(r.motivo()).isEqualTo("erro");
            assertThat(r.diferencaMinutos()).isEqualTo(-5);
        }
    }

    // Record basics — equals, hashCode, toString
    @Nested
    @DisplayName("Record basics")
    class RecordBasics {

        @Test
        @DisplayName("dois valido(15) devem ser iguais com mesmo hashCode")
        void doisValidosIguais() {
            ResultadoValidacao a = ResultadoValidacao.valido(15);
            ResultadoValidacao b = ResultadoValidacao.valido(15);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("valido(15) e valido(16) devem ser diferentes")
        void validosComDiferencasDiferentesSaoDiferentes() {
            ResultadoValidacao a = ResultadoValidacao.valido(15);
            ResultadoValidacao b = ResultadoValidacao.valido(16);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("dois invalido(30, \"x\") devem ser iguais")
        void doisInvalidosIguais() {
            ResultadoValidacao a = ResultadoValidacao.invalido(30, "x");
            ResultadoValidacao b = ResultadoValidacao.invalido(30, "x");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("valido e invalido com mesma diferença devem ser diferentes")
        void validoEInvalidoComMesmaDiferencaSaoDiferentes() {
            ResultadoValidacao a = ResultadoValidacao.valido(15);
            ResultadoValidacao b = ResultadoValidacao.invalido(15, "x");

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("toString deve conter os três campos")
        void toStringDeveConterCampos() {
            ResultadoValidacao r = new ResultadoValidacao(true, "ok", 5);
            String s = r.toString();

            assertThat(s).contains("valido=true");
            assertThat(s).contains("motivo=ok");
            assertThat(s).contains("diferencaMinutos=5");
        }

        @Test
        @DisplayName("acesso aos componentes via métodos do record")
        void acessoAosComponentes() {
            ResultadoValidacao r = new ResultadoValidacao(false, "erro", 42);

            assertThat(r.valido()).isFalse();
            assertThat(r.motivo()).isEqualTo("erro");
            assertThat(r.diferencaMinutos()).isEqualTo(42);
        }
    }
}