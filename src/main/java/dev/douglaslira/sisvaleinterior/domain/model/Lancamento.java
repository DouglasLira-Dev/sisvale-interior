package dev.douglaslira.sisvaleinterior.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;


public final class Lancamento {

    private final Long id;
    private final Long servidorId;
    private final LocalDate data;

    private final Horario horaDescida;
    private final Horario horaEntrada;
    private final BigDecimal valorIda;

    private final Horario horaSaida;
    private final Horario horaOnibus;
    private final BigDecimal valorVolta;

    
    public Lancamento(Long id,
                      Long servidorId,
                      LocalDate data,
                      Horario horaDescida,
                      Horario horaEntrada,
                      BigDecimal valorIda,
                      Horario horaSaida,
                      Horario horaOnibus,
                      BigDecimal valorVolta) {

        if (servidorId == null) {
            throw new IllegalArgumentException("Servidor é obrigatório");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }
        if (horaDescida == null) {
            throw new IllegalArgumentException("Hora de descida é obrigatória");
        }
        if (horaEntrada == null) {
            throw new IllegalArgumentException("Hora de entrada é obrigatória");
        }
        if (horaSaida == null) {
            throw new IllegalArgumentException("Hora de saída é obrigatória");
        }
        if (horaOnibus == null) {
            throw new IllegalArgumentException("Hora do ônibus é obrigatória");
        }
        validarValor(valorIda);
        validarValor(valorVolta);

        this.id = id;
        this.servidorId = servidorId;
        this.data = data;
        this.horaDescida = horaDescida;
        this.horaEntrada = horaEntrada;
        this.valorIda = valorIda.setScale(2, RoundingMode.HALF_UP);
        this.horaSaida = horaSaida;
        this.horaOnibus = horaOnibus;
        this.valorVolta = valorVolta.setScale(2, RoundingMode.HALF_UP);
    }

    private static void validarValor(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Valor é obrigatório");
        }
        if (valor.signum() < 0) {
            throw new IllegalArgumentException("Valor não pode ser negativo");
        }
    }

    public Long id() {
        return id;
    }

    public Long servidorId() {
        return servidorId;
    }

    public LocalDate data() {
        return data;
    }

    public Horario horaDescida() {
        return horaDescida;
    }

    public Horario horaEntrada() {
        return horaEntrada;
    }

    public BigDecimal valorIda() {
        return valorIda;
    }

    public Horario horaSaida() {
        return horaSaida;
    }

    public Horario horaOnibus() {
        return horaOnibus;
    }

    public BigDecimal valorVolta() {
        return valorVolta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lancamento outro)) return false;
        return servidorId.equals(outro.servidorId) && data.equals(outro.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servidorId, data);
    }

    @Override
    public String toString() {
        return "Lancamento{" +
                "id=" + id +
                ", servidorId=" + servidorId +
                ", data=" + data +
                ", valorIda=" + valorIda +
                ", valorVolta=" + valorVolta +
                '}';
    }
}