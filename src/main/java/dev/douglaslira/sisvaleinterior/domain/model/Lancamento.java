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

        boolean idaCompleta = horaDescida != null && horaEntrada != null && valorIda != null;
        boolean idaParcial = !idaCompleta && (horaDescida != null || horaEntrada != null || valorIda != null);
        if (idaParcial){
            throw new IllegalArgumentException("Para lançar a ida, informe a hora de descida, hora de entrada e valor.");
        }

        boolean voltaCompleta = horaSaida != null && horaOnibus != null && valorVolta != null;
        boolean voltaParcial = !voltaCompleta && (horaSaida != null || horaOnibus != null || valorVolta != null);
        if (voltaParcial){
            throw new IllegalArgumentException("Para lançar a volta, informe hora de saída, hora do ôninbus e valor");
        }
        if (!idaCompleta && !voltaCompleta) {
            throw new IllegalArgumentException("Informe ao menos a ida ou a volta completas.");
        }

        if (idaCompleta) validarValor(valorIda);
        if (voltaCompleta) validarValor(valorVolta);

        this.id = id;
        this.servidorId = servidorId;
        this.data = data;
        this.horaDescida = idaCompleta ? horaDescida : null;
        this.horaEntrada = idaCompleta ? horaEntrada : null;
        this.valorIda = idaCompleta ? valorIda.setScale(2, RoundingMode.HALF_UP) : null;
        this.horaSaida = voltaCompleta ? horaSaida : null;
        this.horaOnibus = voltaCompleta ? horaOnibus : null;
        this.valorVolta = voltaCompleta ? valorVolta.setScale(2, RoundingMode.HALF_UP) : null;
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

    public boolean temIda(){
        return horaDescida != null;
    }

    public boolean temVolta(){
        return horaSaida != null;
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