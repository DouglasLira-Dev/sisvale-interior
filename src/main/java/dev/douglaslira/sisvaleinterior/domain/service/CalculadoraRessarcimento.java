package dev.douglaslira.sisvaleinterior.domain.service;

import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoDia;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoMes;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoTrecho;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoValidacao;
import dev.douglaslira.sisvaleinterior.domain.model.Trecho;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Orquestra o cálculo de ressarcimento a partir de lançamentos.
 *
 * <p>Usa o {@link ValidadorHorario} para decidir quais trechos são válidos e
 * soma os valores correspondentes. Não aplica regra de negócio própria além
 * de orquestrar o validador.</p>
 */
public final class CalculadoraRessarcimento {

    private final ValidadorHorario validador;

    /**
     * Cria a calculadora com o validador padrão (tolerância de 15 minutos).
     */
    public CalculadoraRessarcimento() {
        this(new ValidadorHorario());
    }

    /**
     * Cria a calculadora com o validador informado.
     *
     * @param validador validador de horário (não pode ser nulo)
     * @throws IllegalArgumentException se {@code validador} for nulo
     */
    public CalculadoraRessarcimento(ValidadorHorario validador) {
        if (validador == null) {
            throw new IllegalArgumentException("Validador é obrigatório");
        }
        this.validador = validador;
    }

    /**
     * Calcula o resultado de um único dia de lançamento.
     *
     * <p>Valida cada trecho do lançamento individualmente, contabiliza o
     * valor apenas dos trechos válidos e consolida o total do dia.</p>
     *
     * @param lancamento lançamento a calcular (não pode ser nulo)
     * @return resultado do dia
     * @throws IllegalArgumentException se {@code lancamento} for nulo
     */
    public ResultadoDia calcularDia(Lancamento lancamento) {
        if (lancamento == null) {
            throw new IllegalArgumentException("Lançamento é obrigatório");
        }

        List<ResultadoTrecho> resultados = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Trecho trecho : lancamento.trechos()) {
            ResultadoValidacao validacao = validador.validar(
                    trecho.horaReferencia(),
                    trecho.horaComparada()
            );
            ResultadoTrecho resultado = ResultadoTrecho.de(trecho, validacao);
            resultados.add(resultado);
            total = total.add(resultado.valorContabilizado());
        }

        return new ResultadoDia(lancamento.data(), resultados, total);
    }

    /**
     * Calcula o resultado consolidado de um mês para um servidor.
     *
     * <p>Filtra os lançamentos cujo mês/ano seja igual a {@code mesAno},
     * calcula cada dia e soma o total do mês. A ordenação dos dias é
     * responsabilidade do {@link ResultadoMes}.</p>
     *
     * @param lancamentos lançamentos a considerar (não pode ser nulo)
     * @param mesAno      mês/ano de referência (não pode ser nulo)
     * @param servidorId  id do servidor (não pode ser nulo)
     * @return resultado consolidado do mês
     * @throws IllegalArgumentException se algum parâmetro for nulo
     */
    public ResultadoMes calcularMes(List<Lancamento> lancamentos, YearMonth mesAno, Long servidorId) {
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