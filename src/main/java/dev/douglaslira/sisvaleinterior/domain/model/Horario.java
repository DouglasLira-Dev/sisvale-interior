package dev.douglaslira.sisvaleinterior.domain.model;

import dev.douglaslira.sisvaleinterior.domain.exception.HorarioInvalidoException;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public record Horario(LocalTime valor) {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("HH:mm");

    public Horario {
        if (valor == null) {
            throw new HorarioInvalidoException("Horário não pode ser nulo");
        }
    }

    public static Horario parse(String hhmm) {
        if (hhmm == null) {
            throw new HorarioInvalidoException("Horário não pode ser nulo");
        }
        try {
            return new Horario(LocalTime.parse(hhmm, FORMATO));
        } catch (DateTimeParseException e) {
            throw new HorarioInvalidoException("Horário em formato inválido: use HH:mm", e);
        }
    }

    public boolean antesDe(Horario outro) {
        return valor.isBefore(outro.valor);
    }

    public boolean depoisDe(Horario outro) {
        return valor.isAfter(outro.valor);
    }

    public long diferencaEmMinutos(Horario outro) {
        return Math.abs(Duration.between(valor, outro.valor).toMinutes());
    }

    public String formatado() {
        return valor.format(FORMATO);
    }

    @Override
    public String toString() {
        return formatado();
    }
}