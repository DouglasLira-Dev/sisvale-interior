package dev.douglaslira.sisvaleinterior.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record ResultadoDia(
        LocalDate data,
        ResultadoValidacao validacaoIda,
        ResultadoValidacao validacaoVolta,
        BigDecimal valorTotalDia
) {
    public ResultadoDia {
        if (data == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }
        if (validacaoIda == null) {
            throw new IllegalArgumentException("Validação da ida é obrigatória");
        }
        if (validacaoVolta == null) {
            throw new IllegalArgumentException("Validação da volta é obrigatória");
        }
        if (valorTotalDia == null) {
            throw new IllegalArgumentException("Valor total é obrigatório");
        }
        // Normaliza para escala 2, evita quebra de equals por escala diferente.
        valorTotalDia = valorTotalDia.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean totalmenteValido() {
        return validacaoIda.valido() && validacaoVolta.valido();
    }

    public boolean parcialmenteValido() {
        return validacaoIda.valido() ^ validacaoVolta.valido();
    }

    public boolean totalmenteInvalido() {
        return !validacaoIda.valido() && !validacaoVolta.valido();
    }

    @Override
    public String toString() {
        return "ResultadoDia{" +
                "data=" + data +
                ", total=" + valorTotalDia +
                ", ida=" + rotulo(validacaoIda) +
                ", volta=" + rotulo(validacaoVolta) +
                '}';
    }

    private static String rotulo(ResultadoValidacao validacao) {
        return validacao.valido() ? "válido" : "inválido";
    }
}
