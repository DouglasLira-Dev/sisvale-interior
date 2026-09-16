package dev.douglaslira.sisvaleinterior.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Resultado da validação de um trecho individual.
 *
 * <p>Combina o {@link Trecho} original com o veredito da validação
 * ({@link ResultadoValidacao}) e o valor a contabilizar:</p>
 * <ul>
 *   <li>Se o trecho é <strong>válido</strong>, o {@code valorContabilizado}
 *       é o valor do trecho.</li>
 *   <li>Se o trecho é <strong>inválido</strong>, o {@code valorContabilizado}
 *       é {@code 0.00}.</li>
 * </ul>
 *
 * <p>É uma projeção imutável — não calcula regra de tolerância, apenas
 * transporta o resultado.</p>
 */
public record ResultadoTrecho(
        Trecho trecho,
        ResultadoValidacao validacao,
        BigDecimal valorContabilizado
) {

    /**
     * Cria o resultado a partir do trecho e da validação.
     *
     * @param trecho    trecho do lançamento (não pode ser nulo)
     * @param validacao resultado da validação do trecho (não pode ser nulo)
     * @return resultado com {@code valorContabilizado} calculado
     * @throws IllegalArgumentException se algum argumento for nulo
     */
    public static ResultadoTrecho de(Trecho trecho, ResultadoValidacao validacao) {
        if (trecho == null) {
            throw new IllegalArgumentException("Trecho é obrigatório");
        }
        if (validacao == null) {
            throw new IllegalArgumentException("Validação é obrigatória");
        }

        BigDecimal contabilizado = validacao.valido()
                ? trecho.valor().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        return new ResultadoTrecho(trecho, validacao, contabilizado);
    }
}