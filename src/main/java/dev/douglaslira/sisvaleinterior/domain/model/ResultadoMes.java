package dev.douglaslira.sisvaleinterior.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record ResultadoMes(
        Long servidorId,
        YearMonth mesAno,
        List<ResultadoDia> dias,
        BigDecimal valorTotalMes
) {
    public ResultadoMes {
        if (servidorId == null) {
            throw new IllegalArgumentException("Servidor é obrigatório");
        }
        if (mesAno == null) {
            throw new IllegalArgumentException("Mês/ano é obrigatório");
        }
        if (dias == null) {
            throw new IllegalArgumentException("Lista de dias é obrigatória");
        }
        if (valorTotalMes == null) {
            throw new IllegalArgumentException("Valor total é obrigatório");
        }

        List<ResultadoDia> copia = new ArrayList<>(dias);
        copia.sort(Comparator.comparing(ResultadoDia::data));
        dias = List.copyOf(copia);

        valorTotalMes = valorTotalMes.setScale(2, RoundingMode.HALF_UP);
    }

    public List<ResultadoDia> diasComAlgumValor() {
        return dias.stream()
                .filter(d -> d.totalmenteValido() || d.parcialmenteValido())
                .toList();
    }

    public List<ResultadoDia> diasParciais() {
        return dias.stream()
                .filter(ResultadoDia::parcialmenteValido)
                .toList();
    }

    public List<ResultadoDia> diasInvalidos() {
        return dias.stream()
                .filter(ResultadoDia::totalmenteInvalido)
                .toList();
    }

    @Override
    public String toString() {
        return "ResultadoMes{" +
                "servidorId=" + servidorId +
                ", mesAno=" + mesAno +
                ", dias=" + dias.size() +
                ", valorTotalMes=" + valorTotalMes +
                '}';
    }
}
