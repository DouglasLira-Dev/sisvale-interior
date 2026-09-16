package dev.douglaslira.sisvaleinterior.application.dto;

import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoDia;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO rico que combina o lançamento com o resultado da validação de cada
 * trecho do dia.
 *
 * <p>Usado pela tela de lançamentos para exibir status por trecho
 * (✅/⚠️/❌), motivos e o total do dia já calculado pelo domínio.</p>
 *
 * @param lancamento     dados básicos do lançamento
 * @param resultados     veredito por trecho (mesma ordem dos trechos)
 * @param valorTotalDia  valor a ressarcir no dia (0.00 se nenhum trecho válido)
 * @param status         status consolidado do dia (VALIDO, PARCIAL, INVALIDO)
 */
public record LancamentoComStatusDTO(
        LancamentoDTO lancamento,
        List<ResultadoTrechoDTO> resultados,
        BigDecimal valorTotalDia,
        StatusDia status
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

        List<ResultadoTrechoDTO> resultadosDTO = resultado.resultados().stream()
                .map(ResultadoTrechoDTO::de)
                .toList();

        return new LancamentoComStatusDTO(
                LancamentoDTO.de(lancamento),
                resultadosDTO,
                resultado.valorTotalDia(),
                calcularStatus(resultado)
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