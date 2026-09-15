package dev.douglaslira.sisvaleinterior.application.dto;

import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoDia;

import java.math.BigDecimal;

/**
 * DTO rico que combina o lançamento com o resultado da validação do dia.
 *
 * <p>Usado pela tela de lançamentos para exibir status (✅/⚠️/❌) e total
 * do dia já calculado pelo domínio. Diferente do {@link LancamentoDTO} puro,
 * que só transporta os dados cadastrados.</p>
 *
 * @param lancamento dados do lançamento
 * @param status     status consolidado do dia (ida + volta)
 * @param total      valor a ressarcir no dia (0.00 se totalmente inválido)
 */
public record LancamentoComStatusDTO(
        LancamentoDTO lancamento,
        StatusDia status,
        BigDecimal total
) {

    /**
     * Cria o DTO a partir do lançamento e do resultado da validação.
     *
     * @param lancamento lançamento de domínio (não pode ser nulo)
     * @param resultado  resultado da validação do dia (não pode ser nulo)
     * @return DTO pronto para a UI
     * @throws IllegalArgumentException se algum argumento for nulo
     */
    public static LancamentoComStatusDTO de(Lancamento lancamento, ResultadoDia resultado) {
        if (lancamento == null) {
            throw new IllegalArgumentException("Lançamento é obrigatório");
        }
        if (resultado == null) {
            throw new IllegalArgumentException("Resultado é obrigatório");
        }
        return new LancamentoComStatusDTO(
                LancamentoDTO.de(lancamento),
                calcularStatus(resultado),
                resultado.valorTotalDia()
        );
    }

    private static StatusDia calcularStatus(ResultadoDia resultado) {
        if (resultado.totalmenteValido()) {
            return StatusDia.VALIDO;
        }
        if (resultado.parcialmenteValido()) {
            return StatusDia.PARCIAL;
        }
        return StatusDia.INVALIDO;
    }
}