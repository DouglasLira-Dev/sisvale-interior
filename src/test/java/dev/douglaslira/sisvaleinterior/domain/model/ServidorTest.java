package dev.douglaslira.sisvaleinterior.domain.model;

import dev.douglaslira.sisvaleinterior.domain.exception.CpfInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes da entidade Servidor")
class ServidorTest {

    private static final String NOME = "João Silva";
    private static final String MATRICULA = "12345";
    private static final String CPF_SEM_MASCARA = "11144477735";
    private static final String CPF_COM_MASCARA = "111.444.777-35";

    private Servidor servidorValido() {
        return new Servidor(null, NOME, MATRICULA, CPF_SEM_MASCARA, true);
    }

    // Criação válida
    @Nested
    @DisplayName("Criação válida")
    class CriacaoValida {

        @Test
        @DisplayName("deve criar servidor com todos os campos válidos")
        void deveCriarServidorValido() {
            Servidor s = servidorValido();

            assertThat(s.id()).isNull();
            assertThat(s.nome()).isEqualTo(NOME);
            assertThat(s.matricula()).isEqualTo(MATRICULA);
            assertThat(s.cpf()).isEqualTo(CPF_SEM_MASCARA);
            assertThat(s.ativo()).isTrue();
        }

        @Test
        @DisplayName("deve aceitar id nulo (antes de persistir)")
        void deveAceitarIdNulo() {
            Servidor s = servidorValido();
            assertThat(s.id()).isNull();
        }

        @Test
        @DisplayName("deve aceitar id preenchido (após persistir)")
        void deveAceitarIdPreenchido() {
            Servidor s = new Servidor(99L, NOME, MATRICULA, CPF_SEM_MASCARA, true);
            assertThat(s.id()).isEqualTo(99L);
        }

        @Test
        @DisplayName("deve aceitar ativo = false")
        void deveAceitarAtivoFalse() {
            Servidor s = new Servidor(null, NOME, MATRICULA, CPF_SEM_MASCARA, false);
            assertThat(s.ativo()).isFalse();
        }

        @Test
        @DisplayName("deve aceitar CPF com máscara")
        void deveAceitarCpfComMascara() {
            Servidor s = new Servidor(null, NOME, MATRICULA, CPF_COM_MASCARA, true);
            assertThat(s.cpf()).isEqualTo(CPF_SEM_MASCARA);
        }

        @Test
        @DisplayName("deve aceitar CPF sem máscara")
        void deveAceitarCpfSemMascara() {
            Servidor s = new Servidor(null, NOME, MATRICULA, CPF_SEM_MASCARA, true);
            assertThat(s.cpf()).isEqualTo(CPF_SEM_MASCARA);
        }
    }

    // Validação de nome
    @Nested
    @DisplayName("Validação de nome")
    class ValidacaoDeNome {

        @Test
        @DisplayName("deve lançar exceção quando nome for nulo")
        void deveLancarExcecaoQuandoNomeForNulo() {
            assertThatThrownBy(() -> new Servidor(null, null, MATRICULA, CPF_SEM_MASCARA, true))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nome");
        }

        @Test
        @DisplayName("deve lançar exceção quando nome for vazio")
        void deveLancarExcecaoQuandoNomeForVazio() {
            assertThatThrownBy(() -> new Servidor(null, "", MATRICULA, CPF_SEM_MASCARA, true))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nome");
        }

        @Test
        @DisplayName("deve lançar exceção quando nome for só espaços")
        void deveLancarExcecaoQuandoNomeForSoEspacos() {
            assertThatThrownBy(() -> new Servidor(null, "   ", MATRICULA, CPF_SEM_MASCARA, true))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nome");
        }

        @Test
        @DisplayName("deve aceitar nome válido")
        void deveAceitarNomeValido() {
            Servidor s = servidorValido();
            assertThat(s.nome()).isEqualTo(NOME);
        }
    }

    // Validação de matrícula
    @Nested
    @DisplayName("Validação de matrícula")
    class ValidacaoDeMatricula {

        @Test
        @DisplayName("deve lançar exceção quando matrícula for nula")
        void deveLancarExcecaoQuandoMatriculaForNula() {
            assertThatThrownBy(() -> new Servidor(null, NOME, null, CPF_SEM_MASCARA, true))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("matrícula");
        }

        @Test
        @DisplayName("deve lançar exceção quando matrícula for vazia")
        void deveLancarExcecaoQuandoMatriculaForVazia() {
            assertThatThrownBy(() -> new Servidor(null, NOME, "", CPF_SEM_MASCARA, true))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("matrícula");
        }

        @Test
        @DisplayName("deve lançar exceção quando matrícula for só espaços")
        void deveLancarExcecaoQuandoMatriculaForSoEspacos() {
            assertThatThrownBy(() -> new Servidor(null, NOME, "   ", CPF_SEM_MASCARA, true))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("matrícula");
        }

        @Test
        @DisplayName("deve aceitar matrícula válida")
        void deveAceitarMatriculaValida() {
            Servidor s = servidorValido();
            assertThat(s.matricula()).isEqualTo(MATRICULA);
        }
    }

    // Validação de CPF
    @Nested
    @DisplayName("Validação de CPF")
    class ValidacaoDeCpf {

        @Test
        @DisplayName("deve lançar exceção quando CPF for nulo")
        void deveLancarExcecaoQuandoCpfForNulo() {
            assertThatThrownBy(() -> new Servidor(null, NOME, MATRICULA, null, true))
                    .isInstanceOf(CpfInvalidoException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando CPF for vazio")
        void deveLancarExcecaoQuandoCpfForVazio() {
            assertThatThrownBy(() -> new Servidor(null, NOME, MATRICULA, "", true))
                    .isInstanceOf(CpfInvalidoException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando CPF tiver dígito verificador errado")
        void deveLancarExcecaoQuandoCpfComDvErrado() {
            assertThatThrownBy(() -> new Servidor(null, NOME, MATRICULA, "11144477736", true))
                    .isInstanceOf(CpfInvalidoException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando CPF for sequência inválida")
        void deveLancarExcecaoQuandoCpfForSequencia() {
            assertThatThrownBy(() -> new Servidor(null, NOME, MATRICULA, "111.111.111-11", true))
                    .isInstanceOf(CpfInvalidoException.class);
        }

        @Test
        @DisplayName("deve aceitar CPF válido")
        void deveAceitarCpfValido() {
            Servidor s = servidorValido();
            assertThat(s.cpf()).isEqualTo(CPF_SEM_MASCARA);
        }
    }

    // Normalização de campos
    @Nested
    @DisplayName("Normalização de campos")
    class Normalizacao {

        @Test
        @DisplayName("deve armazenar CPF sem máscara mesmo se recebido com máscara")
        void deveNormalizarCpfComMascara() {
            Servidor s = new Servidor(null, NOME, MATRICULA, CPF_COM_MASCARA, true);
            assertThat(s.cpf()).isEqualTo(CPF_SEM_MASCARA);
        }

        @Test
        @DisplayName("deve aplicar trim no nome")
        void deveAplicarTrimNoNome() {
            Servidor s = new Servidor(null, "  João Silva  ", MATRICULA, CPF_SEM_MASCARA, true);
            assertThat(s.nome()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("deve aplicar trim na matrícula")
        void deveAplicarTrimNaMatricula() {
            Servidor s = new Servidor(null, NOME, "  12345  ", CPF_SEM_MASCARA, true);
            assertThat(s.matricula()).isEqualTo("12345");
        }
    }

    // Método de fábrica Servidor.novo
    @Nested
    @DisplayName("Método de fábrica novo")
    class MetodoNovo {

        @Test
        @DisplayName("deve criar servidor com id nulo e ativo true")
        void deveCriarComIdNuloEAtivoTrue() {
            Servidor s = Servidor.novo(NOME, MATRICULA, CPF_SEM_MASCARA);

            assertThat(s.id()).isNull();
            assertThat(s.ativo()).isTrue();
            assertThat(s.nome()).isEqualTo(NOME);
            assertThat(s.matricula()).isEqualTo(MATRICULA);
            assertThat(s.cpf()).isEqualTo(CPF_SEM_MASCARA);
        }
    }

    // equals e hashCode
    @Nested
    @DisplayName("equals e hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("deve considerar iguais servidores com mesma matrícula")
        void deveConsiderarIguaisComMesmaMatricula() {
            Servidor a = servidorValido();
            Servidor b = servidorValido();

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("deve considerar diferentes servidores com matrículas diferentes")
        void deveConsiderarDiferentesComMatriculasDiferentes() {
            Servidor a = servidorValido();
            Servidor b = new Servidor(null, NOME, "99999", CPF_SEM_MASCARA, true);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("mesma matrícula, nomes diferentes → iguais (matrícula é o identificador)")
        void mesmaMatriculaNomesDiferentesSaoIguais() {
            Servidor a = new Servidor(null, "João Silva", MATRICULA, CPF_SEM_MASCARA, true);
            Servidor b = new Servidor(null, "Maria Souza", MATRICULA, CPF_SEM_MASCARA, true);

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("id diferente NÃO afeta igualdade")
        void idDiferenteNaoAfetaIgualdade() {
            Servidor a = new Servidor(1L, NOME, MATRICULA, CPF_SEM_MASCARA, true);
            Servidor b = new Servidor(2L, NOME, MATRICULA, CPF_SEM_MASCARA, true);

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("comparação com null → false")
        void comparacaoComNullEhFalse() {
            Servidor a = servidorValido();
            assertThat(a).isNotEqualTo(null);
        }

        @Test
        @DisplayName("comparação com objeto de outra classe → false")
        void comparacaoComOutraClasseEhFalse() {
            Servidor a = servidorValido();
            assertThat(a).isNotEqualTo("não sou um servidor");
        }

        @Test
        @DisplayName("servidor é igual a si mesmo (reflexividade)")
        void reflexividade() {
            Servidor a = servidorValido();
            assertThat(a).isEqualTo(a);
        }
    }

    // toString
    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("deve conter nome, matrícula e ativo")
        void deveConterDadosPrincipais() {
            Servidor s = servidorValido();
            String str = s.toString();

            assertThat(str).contains(NOME);
            assertThat(str).contains(MATRICULA);
            assertThat(str).contains("ativo=true");
        }

        @Test
        @DisplayName("NÃO deve conter o CPF completo (LGPD)")
        void naoDeveConterCpfCompleto() {
            Servidor s = servidorValido();
            String str = s.toString();

            assertThat(str).doesNotContain(CPF_SEM_MASCARA);
            assertThat(str).doesNotContain(CPF_COM_MASCARA);
            assertThat(str).doesNotContain("111");
        }

        @Test
        @DisplayName("NÃO deve conter o campo cpf")
        void naoDeveConterCampoCpf() {
            Servidor s = servidorValido();
            String str = s.toString();

            assertThat(str).doesNotContain("cpf=");
        }
    }
}