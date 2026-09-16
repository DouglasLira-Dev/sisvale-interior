package dev.douglaslira.sisvaleinterior.application.dto;

import dev.douglaslira.sisvaleinterior.domain.model.Horario;
import dev.douglaslira.sisvaleinterior.domain.model.Trecho;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * DTO de leitura e escrita de {@link Trecho} entre a UI e o domínio.
 *
 * <p>Expõe {@link LocalTime} cru (não o VO {@code Horario}) — a UI não
 * precisa conhecer os VOs do domínio.</p>
 *
 * <p>Diferente do {@code ServidorDTO}, este DTO é usado <strong>nos dois
 * sentidos</strong>: a UI envia trechos novos e recebe trechos existentes.
 * Por isso tem tanto {@link #de(Trecho)} (domínio → DTO) quanto
 * {@link #paraDominio()} (DTO → domínio).</p>
 */
public record TrechoDTO(
        LocalTime horaReferencia,
        LocalTime horaComparada,
        BigDecimal valor
) {

    /**
     * Converte um {@link Trecho} do domínio para DTO.
     *
     * @param trecho trecho de domínio (não pode ser nulo)
     * @return DTO correspondente
     * @throws IllegalArgumentException se {@code trecho} for nulo
     */
    public static TrechoDTO de(Trecho trecho) {
        if (trecho == null) {
            throw new IllegalArgumentException("Trecho é obrigatório");
        }
        return new TrechoDTO(
                trecho.horaReferencia().valor(),
                trecho.horaComparada().valor(),
                trecho.valor()
        );
    }

    /**
     * Converte este DTO para um {@link Trecho} do domínio.
     *
     * <p>O construtor do {@link Trecho} valida os invariantes (nulos, valor
     * não negativo) e normaliza a escala do valor.</p>
     *
     * @return trecho de domínio
     * @throws IllegalArgumentException se algum campo for nulo ou o valor
     *                                  for inválido
     */
    public Trecho paraDominio() {
        return new Trecho(
                new Horario(horaReferencia),
                new Horario(horaComparada),
                valor
        );
    }
}