package dev.douglaslira.sisvaleinterior.domain.service;

import dev.douglaslira.sisvaleinterior.domain.model.Horario;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoValidacao;

import java.time.Duration;

/**
 * Aplica a regra de tolerância a um par de horários (referência → comparada).
 *
 * <p>A tolerância vale <strong>apenas para horários anteriores</strong> ao
 * ponto de referência. Horários posteriores são sempre considerados válidos
 * (atrasos, trânsito, ônibus perdido).</p>
 *
 * <p>Nunca lança exceção para violação de regra — devolve sempre um
 * {@link ResultadoValidacao} com {@code valido} {@code true} ou {@code false}.</p>
 *
 * <p>Genérico em relação ao tipo de par: valida tanto um trecho de ida quanto
 * de volta, sem conhecer essas noções. Quem decide o que é referência e o
 * que é comparada é o chamador.</p>
 */
public final class ValidadorHorario {

    private static final Duration TOLERANCIA_PADRAO = Duration.ofMinutes(15);

    private final Duration tolerancia;

    /**
     * Cria um validador com a tolerância padrão de 15 minutos.
     *
     * <p>Atalho pensado para testes rápidos. Em produção, prefira o construtor
     * que recebe a tolerância lida de {@code application.properties}
     * ({@code regra.tolerancia.minutos}).</p>
     */
    public ValidadorHorario() {
        this(TOLERANCIA_PADRAO);
    }

    /**
     * Cria um validador com a tolerância informada.
     *
     * @param tolerancia tolerância para horários anteriores; deve ser positiva
     * @throws IllegalArgumentException se {@code tolerancia} for nula ou não positiva
     */
    public ValidadorHorario(Duration tolerancia) {
        if (tolerancia == null) {
            throw new IllegalArgumentException("Tolerância é obrigatória");
        }
        if (tolerancia.isZero() || tolerancia.isNegative()) {
            throw new IllegalArgumentException("Tolerância deve ser positiva");
        }
        this.tolerancia = tolerancia;
    }

    /**
     * Valida um par de horários: verifica se {@code comparada} não está antes
     * do limite {@code referencia - tolerancia}.
     *
     * <p>Horários posteriores ou iguais à referência são sempre válidos.
     * Horários anteriores são válidos se a diferença for menor ou igual à
     * tolerância.</p>
     *
     * @param referencia horário de referência (não pode ser nulo)
     * @param comparada  horário a validar (não pode ser nulo)
     * @return resultado da validação
     */
    public ResultadoValidacao validar(Horario referencia, Horario comparada) {
        long diferenca = referencia.diferencaEmMinutos(comparada);

        if (!comparada.antesDe(referencia)) {
            return ResultadoValidacao.valido(diferenca);
        }

        if (diferenca <= tolerancia.toMinutes()) {
            return ResultadoValidacao.valido(diferenca);
        }

        String motivo = "Horário divergente: " + diferenca
                + " min antes, ultrapassando a tolerância de "
                + tolerancia.toMinutes() + " min";

        return ResultadoValidacao.invalido(diferenca, motivo);
    }
}