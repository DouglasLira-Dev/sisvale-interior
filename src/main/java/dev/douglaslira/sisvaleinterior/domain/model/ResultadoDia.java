package dev.douglaslira.sisvaleinterior.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Resultado consolidado da validação de um dia de lançamento.
 *
 * <p>Carrega o veredito de <strong>cada trecho</strong> do dia (como
 * {@link ResultadoTrecho}) e o valor total a ressarcir, já calculado pela
 * {@code CalculadoraRessarcimento}.</p>
 *
 * <p>É uma projeção imutável — não calcula regra de tolerância, apenas
 * consolida o resultado dos trechos.</p>
 */
public record ResultadoDia(
        LocalDate data,
        List<ResultadoTrecho> resultados,
        BigDecimal valorTotalDia
) {

    /**
     * Compact constructor — valida invariantes e normaliza a escala do total.
     *
     * @throws IllegalArgumentException se algum campo for nulo ou a lista
     *                                  de trechos estiver vazia
     */
    public ResultadoDia {
        if (data == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }
        if (resultados == null) {
            throw new IllegalArgumentException("Resultados são obrigatórios");
        }
        if (resultados.isEmpty()) {
            throw new IllegalArgumentException("Dia precisa de pelo menos um trecho");
        }
        if (valorTotalDia == null) {
            throw new IllegalArgumentException("Valor total é obrigatório");
        }

        resultados = List.copyOf(resultados);
        valorTotalDia = valorTotalDia.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @return {@code true} se <strong>todos</strong> os trechos são válidos
     */
    public boolean totalmenteValido() {
        return resultados.stream()
                .allMatch(r -> r.validacao().valido());
    }

    /**
     * @return {@code true} se há pelo menos <strong>um</strong> trecho válido
     *         e pelo menos <strong>um</strong> inválido
     */
    public boolean parcialmenteValido() {
        boolean algumValido = resultados.stream()
                .anyMatch(r -> r.validacao().valido());
        boolean algumInvalido = resultados.stream()
                .anyMatch(r -> !r.validacao().valido());
        return algumValido && algumInvalido;
    }

    /**
     * @return {@code true} se <strong>nenhum</strong> trecho é válido
     */
    public boolean totalmenteInvalido() {
        return resultados.stream()
                .noneMatch(r -> r.validacao().valido());
    }

    /**
     * Representação resumida — não despeja a lista completa de trechos.
     */
    @Override
    public String toString() {
        return "ResultadoDia{" +
                "data=" + data +
                ", total=" + valorTotalDia +
                ", trechos=" + resultados.size() +
                '}';
    }
}