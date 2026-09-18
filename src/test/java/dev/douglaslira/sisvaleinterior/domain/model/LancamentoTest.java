package dev.douglaslira.sisvaleinterior.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes da entidade Lancamento")
class LancamentoTest {

    // Dados-base reutilizados em todos os testes
    private static final Long ID_NULO = null;
    private static final Long SERVIDOR_ID = 1L;
    private static final LocalDate DATA = LocalDate.of(2026, 9, 15);

    // Helpers
    private Trecho trecho(String referencia, String comparada, String valor) {
        return new Trecho(
                Horario.parse(referencia),
                Horario.parse(comparada),
                new BigDecimal(valor)
        );
    }

    private Trecho ida() {
        return trecho("07:45", "07:30", "20.00");
    }

    private Trecho volta() {
        return trecho("17:00", "16:45", "22.00");
    }

    private Lancamento lancamentoValido() {
        return new Lancamento(ID_NULO, SERVIDOR_ID, DATA, List.of(ida(), volta()));
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
            assertThat(l.trechos()).hasSize(2);
            assertThat(l.getQuantidadeTrechos()).isEqualTo(2);
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
                    99L, SERVIDOR_ID, DATA, List.of(ida(), volta()));
            assertThat(l.id()).isEqualTo(99L);
        }

        @Test
        @DisplayName("deve aceitar 1 trecho (só ida)")
        void deveAceitarUmTrecho() {
            Lancamento l = new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA, List.of(ida()));

            assertThat(l.trechos()).hasSize(1);
            assertThat(l.getQuantidadeTrechos()).isEqualTo(1);
        }

        @Test
        @DisplayName("deve aceitar 3 trechos")
        void deveAceitarTresTrechos() {
            Trecho conexao = trecho("12:00", "11:45", "15.00");

            Lancamento l = new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA, List.of(ida(), conexao, volta()));

            assertThat(l.trechos()).hasSize(3);
        }

        @Test
        @DisplayName("deve aceitar 4 trechos")
        void deveAceitarQuatroTrechos() {
            Trecho conexao1 = trecho("12:00", "11:45", "15.00");
            Trecho conexao2 = trecho("14:00", "13:45", "10.00");

            Lancamento l = new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA,
                    List.of(ida(), conexao1, conexao2, volta()));

            assertThat(l.trechos()).hasSize(4);
        }

        @Test
        @DisplayName("deve preservar a ordem dos trechos")
        void devePreservarOrdemDosTrechos() {
            Trecho primeira = ida();
            Trecho segunda = volta();

            Lancamento l = new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA, List.of(primeira, segunda));

            assertThat(l.trechos().get(0)).isEqualTo(primeira);
            assertThat(l.trechos().get(1)).isEqualTo(segunda);
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
                    ID_NULO, null, DATA, List.of(ida(), volta())))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Servidor");
        }

        @Test
        @DisplayName("deve lançar exceção quando data for nula")
        void deveLancarExcecaoQuandoDataForNula() {
            assertThatThrownBy(() -> new Lancamento(
                    ID_NULO, SERVIDOR_ID, null, List.of(ida(), volta())))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Data");
        }

        @Test
        @DisplayName("deve lançar exceção quando lista de trechos for nula")
        void deveLancarExcecaoQuandoTrechosForemNulos() {
            assertThatThrownBy(() -> new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Trechos");
        }

        @Test
        @DisplayName("deve lançar exceção quando lista de trechos for vazia")
        void deveLancarExcecaoQuandoTrechosForemVazios() {
            assertThatThrownBy(() -> new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA, List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("pelo menos um trecho");
        }
    }

    // Acesso a trechos
    @Nested
    @DisplayName("Acesso a trechos")
    class AcessoATrechos {

        @Test
        @DisplayName("l.trechos() retorna lista imutável")
        void trechosRetornaListaImutavel() {
            Lancamento l = lancamentoValido();

            assertThatThrownBy(() -> l.trechos().add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("alterar a lista original após a criação não afeta o Lancamento")
        void alterarListaOriginalNaoAfeta() {
            List<Trecho> original = new java.util.ArrayList<>();
            original.add(ida());

            Lancamento l = new Lancamento(ID_NULO, SERVIDOR_ID, DATA, original);

            original.add(volta());

            assertThat(l.trechos()).hasSize(1);
        }
    }

    // Normalização de valores (escala 2)
    @Nested
    @DisplayName("Normalização de valores")
    class NormalizacaoValores {

        private Lancamento comValorDoTrecho(String valor) {
            Trecho trecho = trecho("07:45", "07:30", valor);
            return new Lancamento(ID_NULO, SERVIDOR_ID, DATA, List.of(trecho));
        }

        @Test
        @DisplayName("deve normalizar 20.5 para 20.50")
        void deveNormalizarUmaCasaDecimal() {
            Lancamento l = comValorDoTrecho("20.5");
            Trecho t = l.trechos().get(0);
            assertThat(t.valor()).isEqualByComparingTo("20.50");
            assertThat(t.valor().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("deve normalizar 20 para 20.00")
        void deveNormalizarSemCasasDecimais() {
            Lancamento l = comValorDoTrecho("20");
            Trecho t = l.trechos().get(0);
            assertThat(t.valor()).isEqualByComparingTo("20.00");
            assertThat(t.valor().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("deve arredondar 20.555 para 20.56 (HALF_UP)")
        void deveArredondarTresCasas() {
            Lancamento l = comValorDoTrecho("20.555");
            Trecho t = l.trechos().get(0);
            assertThat(t.valor()).isEqualByComparingTo("20.56");
            assertThat(t.valor().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("deve arredondar 20.554 para 20.55 (HALF_UP)")
        void deveArredondarParaBaixo() {
            Lancamento l = comValorDoTrecho("20.554");
            Trecho t = l.trechos().get(0);
            assertThat(t.valor()).isEqualByComparingTo("20.55");
            assertThat(t.valor().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("deve manter escala 2 quando já está em escala 2")
        void deveManterEscala2() {
            Lancamento l = comValorDoTrecho("20.50");
            Trecho t = l.trechos().get(0);
            assertThat(t.valor()).isEqualTo(new BigDecimal("20.50"));
            assertThat(t.valor().scale()).isEqualTo(2);
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
                    99L, 2L, DATA, List.of(ida(), volta()));

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("deve considerar diferentes lançamentos com data diferente")
        void deveConsiderarDiferentesDataDiferente() {
            Lancamento a = lancamentoValido();
            Lancamento b = new Lancamento(
                    ID_NULO, SERVIDOR_ID, LocalDate.of(2026, 9, 16),
                    List.of(ida(), volta()));

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("id diferente NÃO afeta igualdade (documenta comportamento)")
        void idDiferenteNaoAfetaIgualdade() {
            Lancamento a = new Lancamento(
                    1L, SERVIDOR_ID, DATA, List.of(ida(), volta()));
            Lancamento b = new Lancamento(
                    2L, SERVIDOR_ID, DATA, List.of(ida(), volta()));

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("trechos diferentes NÃO afetam igualdade (só servidorId + data importam)")
        void trechosDiferentesNaoAfetamIgualdade() {
            Trecho t1 = trecho("08:00", "08:15", "99.99");
            Trecho t2 = trecho("18:00", "18:15", "99.99");

            Lancamento a = lancamentoValido();
            Lancamento b = new Lancamento(
                    ID_NULO, SERVIDOR_ID, DATA, List.of(t1, t2));

            assertThat(a).isEqualTo(b);
        }
    }

    // toString
    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("deve conter id, servidorId, data e quantidade de trechos")
        void deveConterDadosPrincipais() {
            Lancamento l = lancamentoValido();
            String s = l.toString();

            assertThat(s).contains("id=");
            assertThat(s).contains("servidorId=" + SERVIDOR_ID);
            assertThat(s).contains("data=" + DATA);
            assertThat(s).contains("trechos=2");
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