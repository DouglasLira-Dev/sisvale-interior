package dev.douglaslira.sisvaleinterior.domain.service;

import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoDia;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoValidacao;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoMes;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;


public final class CalculadoraRessarcimento {
    private final ValidadorHorario validador;

    public CalculadoraRessarcimento() {
        this(new ValidadorHorario());
    }

    public CalculadoraRessarcimento(ValidadorHorario validador) {
        if (validador == null) {
            throw new IllegalArgumentException("Validador é obrigatório");
        }
        this.validador = validador;
    }

    public ResultadoDia calcularDia(Lancamento lancamento) {
        if (lancamento == null) {
            throw new IllegalArgumentException("Lançamento é obrigatório");
        }

        ResultadoValidacao validacaoIda = validador.validarIda(
                lancamento.horaDescida(),
                lancamento.horaEntrada()
        );

        ResultadoValidacao validacaoVolta = validador.validarVolta(
                lancamento.horaSaida(),
                lancamento.horaOnibus()
        );

        BigDecimal total = BigDecimal.ZERO;
        if (validacaoIda.valido()) {
            total = total.add(lancamento.valorIda());
        }
        if (validacaoVolta.valido()) {
            total = total.add(lancamento.valorVolta());
        }

        return new ResultadoDia(
                lancamento.data(),
                validacaoIda,
                validacaoVolta,
                total
        );
    }

    public ResultadoMes calcularMes(List<Lancamento> lancamentos, YearMonth mesAno, Long servidorId){

        if (lancamentos == null) {
            throw new IllegalArgumentException("Lista de lançamentos é obrigatória");
        }
        if (mesAno == null) {
            throw new IllegalArgumentException("Mês/ano é obrigatório");
        }
        if (servidorId == null) {
            throw new IllegalArgumentException("Servidor é obrigatório");
        }

        List<ResultadoDia> dias = new ArrayList<>();
        for (Lancamento l : lancamentos) {
            if (YearMonth.from(l.data()).equals(mesAno)) {
                dias.add(calcularDia(l));
            }
        }

        BigDecimal total = BigDecimal.ZERO;
        for (ResultadoDia dia : dias) {
            total = total.add(dia.valorTotalDia());
        }

        return new ResultadoMes(servidorId, mesAno, dias, total);
    }
}
