package dev.douglaslira.sisvaleinterior.application.dto;

import dev.douglaslira.sisvaleinterior.domain.model.ResultadoDia;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoMes;
import dev.douglaslira.sisvaleinterior.domain.model.ResultadoValidacao;
import dev.douglaslira.sisvaleinterior.domain.model.Servidor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * DTO rico de leitura para a UI exibir o resumo de um mês.
 *
 * <p>Contém tudo que a tela precisa para renderizar sem calcular nada:
 * totais, contadores e a lista detalhada de dias. A UI só percorre e
 * desenha.</p>
 *
 * <p><strong>LGPD:</strong> não expõe CPF. O nome do servidor é o suficiente
 * para identificar no relatório.</p>
 */
public record ResumoMensalDTO(
        Long servidorId,
        String nomeServidor,
        YearMonth mesAno,
        BigDecimal valorTotalMes,
        int totalDias,
        int diasTotalmenteValidos,
        int diasParciais,
        int diasTotalmenteInvalidos,
        List<DiaResumoDTO> dias
) {

    /**
     * Converte um {@link ResultadoMes} + {@link Servidor} em {@link ResumoMensalDTO}.
     *
     * @param mes      resultado consolidado do mês (não pode ser nulo)
     * @param servidor servidor correspondente (não pode ser nulo)
     * @return DTO pronto para a UI
     * @throws IllegalArgumentException se algum argumento for nulo
     */
    public static ResumoMensalDTO de(ResultadoMes mes, Servidor servidor) {
        if (mes == null) {
            throw new IllegalArgumentException("ResultadoMes é obrigatório");
        }
        if (servidor == null) {
            throw new IllegalArgumentException("Servidor é obrigatório");
        }

        List<DiaResumoDTO> dias = mes.dias().stream()
                .map(DiaResumoDTO::de)
                .toList();

        int totalValidos = (int) dias.stream()
                .filter(d -> d.status() == DiaResumoDTO.StatusDia.VALIDO)
                .count();
        int totalParciais = (int) dias.stream()
                .filter(d -> d.status() == DiaResumoDTO.StatusDia.PARCIAL)
                .count();
        int totalInvalidos = (int) dias.stream()
                .filter(d -> d.status() == DiaResumoDTO.StatusDia.INVALIDO)
                .count();

        return new ResumoMensalDTO(
                servidor.id(),
                servidor.nome(),
                mes.mesAno(),
                mes.valorTotalMes(),
                dias.size(),
                totalValidos,
                totalParciais,
                totalInvalidos,
                dias
        );
    }

    // =====================================================================
    // DTO aninhado: dia individual do mês
    // =====================================================================

    /**
     * Representação de um dia no resumo mensal.
     *
     * <p>Já traz o {@link StatusDia} pronto — a UI não precisa recalcular
     * booleanos como {@code totalmenteValido()}. Os motivos e diferenças
     * em minutos permitem exibir mensagens como
     * "16 min fora da tolerância" sem reconsultar o domínio.</p>
     */
    public record DiaResumoDTO(
            LocalDate data,
            StatusDia status,
            BigDecimal valor,
            String motivoIda,
            long diferencaIdaMinutos,
            String motivoVolta,
            long diferencaVoltaMinutos
    ) {

        /**
         * Status consolidado do dia, derivado dos pares de validação.
         */
        public enum StatusDia {
            /** Ida e volta válidos. */
            VALIDO,
            /** Exatamente um dos pares válido. */
            PARCIAL,
            /** Nenhum par válido. */
            INVALIDO
        }

        /**
         * Converte um {@link ResultadoDia} em {@link DiaResumoDTO}.
         *
         * @param dia resultado diário (não pode ser nulo)
         * @return DTO do dia
         * @throws IllegalArgumentException se {@code dia} for nulo
         */
        public static DiaResumoDTO de(ResultadoDia dia) {
            if (dia == null) {
                throw new IllegalArgumentException("ResultadoDia é obrigatório");
            }

            ResultadoValidacao ida = dia.validacaoIda();
            ResultadoValidacao volta = dia.validacaoVolta();

            return new DiaResumoDTO(
                    dia.data(),
                    calcularStatus(dia),
                    dia.valorTotalDia(),
                    ida.motivo(),
                    ida.diferencaMinutos(),
                    volta.motivo(),
                    volta.diferencaMinutos()
            );
        }

        private static StatusDia calcularStatus(ResultadoDia dia) {
            if (dia.totalmenteValido()) {
                return StatusDia.VALIDO;
            }
            if (dia.parcialmenteValido()) {
                return StatusDia.PARCIAL;
            }
            return StatusDia.INVALIDO;
        }
    }
}