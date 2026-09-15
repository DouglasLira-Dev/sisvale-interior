package dev.douglaslira.sisvaleinterior.application.dto;

import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO de leitura de {@link Lancamento} para a UI.
 *
 * <p>Mantém os tipos do domínio ({@link LocalDate}, {@link LocalTime},
 * {@link BigDecimal}) — sem formatação. A UI decide como exibir cada um.</p>
 *
 * <p>Diferente do {@code ServidorDTO}, <strong>não</strong> há mascaramento:
 * os dados de lançamento não são sensíveis por LGPD. Apenas a formatação
 * visual fica a cargo da UI.</p>
 */
public record LancamentoDTO(
        Long id,
        Long servidorId,
        LocalDate data,
        LocalTime horaDescida,
        LocalTime horaEntrada,
        BigDecimal valorIda,
        LocalTime horaSaida,
        LocalTime horaOnibus,
        BigDecimal valorVolta
) {

    /**
     * Converte um {@link Lancamento} em {@link LancamentoDTO}.
     *
     * @param lancamento lançamento de domínio (não pode ser nulo)
     * @return DTO correspondente
     * @throws IllegalArgumentException se {@code lancamento} for nulo
     */
    public static LancamentoDTO de(Lancamento lancamento) {
        if (lancamento == null) {
            throw new IllegalArgumentException("Lançamento é obrigatório");
        }
        return new LancamentoDTO(
                lancamento.id(),
                lancamento.servidorId(),
                lancamento.data(),
                lancamento.horaDescida().valor(),
                lancamento.horaEntrada().valor(),
                lancamento.valorIda(),
                lancamento.horaSaida().valor(),
                lancamento.horaOnibus().valor(),
                lancamento.valorVolta()
        );
    }
}