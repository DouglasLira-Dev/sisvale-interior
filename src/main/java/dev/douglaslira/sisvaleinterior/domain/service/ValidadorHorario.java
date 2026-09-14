package dev.douglaslira.sisvaleinterior.domain.service;

import dev.douglaslira.sisvaleinterior.domain.model.Horario;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoValidacao;

import java.time.Duration;

public final class ValidadorHorario {

    private static final Duration TOLERANCIA_PADRAO = Duration.ofMinutes(15);

    private final Duration tolerancia;


    public ValidadorHorario(){
        this(TOLERANCIA_PADRAO);
    }

    public ValidadorHorario(Duration tolerancia){
        if (tolerancia == null) {
            throw new IllegalArgumentException("Tolerância é obrigatória");
        }
        if (tolerancia.isZero() || tolerancia.isNegative()) {
            throw new IllegalArgumentException("Tolerância deve ser positiva");
        }
        this.tolerancia = tolerancia;
    }

    public ResultadoValidacao validarIda(Horario horaDescida, Horario horaEntrada) {
        return validar(horaDescida, horaEntrada);
    }

    public ResultadoValidacao validarVolta(Horario horaSaida, Horario horaOnibus) {
        return validar(horaSaida, horaOnibus);
    }

    private ResultadoValidacao validar(Horario referencia, Horario comparada) {
        long diferenca = referencia.diferencaEmMinutos(comparada);

        if (!comparada.antesDe(referencia)) {
            return ResultadoValidacao.valido(diferenca);
        }

        if (diferenca <= tolerancia.toMinutes()) {
            return ResultadoValidacao.valido(diferenca);
        }

        String motivo = "Horário divergente: " + diferenca
                + " min antes, ultrapassando a tolerância de "
                + tolerancia.toMinutes() + " min";

        return ResultadoValidacao.invalido(diferenca, motivo);
    }
}

