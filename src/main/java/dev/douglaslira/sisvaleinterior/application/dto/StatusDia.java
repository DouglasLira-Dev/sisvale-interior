package dev.douglaslira.sisvaleinterior.application.dto;

/**
 * Status consolidado de um dia de lançamento no resumo mensal.
 *
 * <p>Deriva dos pares de validação (ida e volta):</p>
 * <ul>
 *   <li>{@link #VALIDO} — ambos os pares válidos</li>
 *   <li>{@link #PARCIAL} — exatamente um par válido</li>
 *   <li>{@link #INVALIDO} — nenhum par válido</li>
 * </ul>
 */
public enum StatusDia {
    VALIDO,
    PARCIAL,
    INVALIDO
}