package dev.douglaslira.sisvaleinterior.application.dto;

import dev.douglaslira.sisvaleinterior.domain.model.ResultadoTrecho;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoValidacao;
import dev.douglaslira.sisvaleinterior.domain.model.Trecho;

import java.math.BigDecimal;

/**
 * DTO de leitura que combina um trecho com o resultado da sua validação.
 *
 * <p>Isola a UI do domínio: expõe apenas dados primitivos e o
 * {@link TrechoDTO}, sem revelar VOs como {@code ResultadoValidacao} ou
 * {@code Trecho}.</p>
 *
 * @param trecho               dados do trecho
 * @param valido               se o trecho passou na validação
 * @param motivo               motivo da validação (ex.: "Dentro da tolerância")
 * @param diferencaMinutos     diferença absoluta em minutos entre os horários
 * @param valorContabilizado   valor a contabilizar (0.00 se inválido)
 */
public record ResultadoTrechoDTO(
        TrechoDTO trecho,
        boolean valido,
        String motivo,
        long diferencaMinutos,
        BigDecimal valorContabilizado
) {

    /**
     * Converte um {@link ResultadoTrecho} do domínio em DTO.
     *
     * @param resultado resultado de domínio (não pode ser nulo)
     * @return DTO correspondente
     * @throws IllegalArgumentException se {@code resultado} for nulo
     */
    public static ResultadoTrechoDTO de(ResultadoTrecho resultado) {
        if (resultado == null) {
            throw new IllegalArgumentException("Resultado é obrigatório");
        }

        Trecho trecho = resultado.trecho();
        ResultadoValidacao validacao = resultado.validacao();

        return new ResultadoTrechoDTO(
                TrechoDTO.de(trecho),
                validacao.valido(),
                validacao.motivo(),
                validacao.diferencaMinutos(),
                resultado.valorContabilizado()
        );
    }
}