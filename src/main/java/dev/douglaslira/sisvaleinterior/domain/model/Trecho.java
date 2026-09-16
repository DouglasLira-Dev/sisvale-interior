package dev.douglaslira.sisvaleinterior.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object que representa um trecho de deslocamento dentro de um
 * lançamento diário.
 *
 * <p>Cada trecho tem um par de horários — {@code horaReferencia} (o ponto de
 * referência, ex.: descida do ônibus) e {@code horaComparada} (o horário a
 * validar, ex.: entrada na unidade) — mais o valor pago nesse trecho.</p>
 *
 * <p>Imutável. O valor é normalizado para escala 2 ({@code HALF_UP}) no
 * construtor, consistente com o resto do domínio e com o
 * {@code NUMERIC(10,2)} do banco.</p>
 */
public record Trecho(Horario horaReferencia, Horario horaComparada, BigDecimal valor) {

    /**
     * Compact constructor — valida invariantes e normaliza a escala do valor.
     *
     * @throws IllegalArgumentException se algum campo for nulo ou o valor for negativo
     */
    public Trecho {
        if (horaReferencia == null) {
            throw new IllegalArgumentException("Hora de referência é obrigatória");
        }
        if (horaComparada == null) {
            throw new IllegalArgumentException("Hora comparada é obrigatória");
        }
        if (valor == null) {
            throw new IllegalArgumentException("Valor é obrigatório");
        }
        if (valor.signum() < 0) {
            throw new IllegalArgumentException("Valor não pode ser negativo");
        }
        valor = valor.setScale(2, RoundingMode.HALF_UP);
    }
}