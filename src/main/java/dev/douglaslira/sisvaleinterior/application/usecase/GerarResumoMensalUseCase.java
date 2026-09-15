package dev.douglaslira.sisvaleinterior.application.usecase;

import dev.douglaslira.sisvaleinterior.application.dto.ResumoMensalDTO;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.domain.model.Servidor;
import dev.douglaslira.sisvaleinterior.domain.repository.ServidorRepository;
import dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Caso de uso: gerar o resumo mensal consolidado de todos os servidores ativos.
 *
 * <p>Reutiliza o {@link ValidarMesUseCase} para cada servidor — a lógica de
 * validação e cálculo não é duplicada.</p>
 *
 * <p><strong>Resiliência:</strong> se um servidor específico falhar, os
 * demais continuam sendo processados. A falha é logada em WARN. Isso evita
 * que um problema pontual derrube o relatório inteiro.</p>
 */
public final class GerarResumoMensalUseCase {

    private static final Logger log = LoggerFactory.getLogger(GerarResumoMensalUseCase.class);

    private final ServidorRepository servidorRepository;
    private final ValidarMesUseCase validarMesUseCase;

    /**
     * @param servidorRepository repositório de servidores (não pode ser nulo)
     * @param validarMesUseCase  caso de uso de validação mensal (não pode ser nulo)
     * @throws IllegalArgumentException se algum argumento for nulo
     */
    public GerarResumoMensalUseCase(ServidorRepository servidorRepository,
                                    ValidarMesUseCase validarMesUseCase) {
        if (servidorRepository == null) {
            throw new IllegalArgumentException("ServidorRepository é obrigatório");
        }
        if (validarMesUseCase == null) {
            throw new IllegalArgumentException("ValidarMesUseCase é obrigatório");
        }
        this.servidorRepository = servidorRepository;
        this.validarMesUseCase = validarMesUseCase;
    }

    /**
     * Gera o resumo mensal de todos os servidores ativos.
     *
     * <p>Servidores que falharem são omitidos do resultado — a falha é
     * logada. Se nenhum servidor ativo existir, devolve lista vazia.</p>
     *
     * @param mesAno mês/ano de referência (obrigatório)
     * @return lista de resumos, ordenada por nome do servidor
     * @throws ApplicationException se {@code mesAno} for nulo ou houver
     *                              falha ao listar servidores
     */
    public List<ResumoMensalDTO> executar(YearMonth mesAno) {
        validarEntrada(mesAno);

        List<Servidor> servidores = listarAtivos();
        List<ResumoMensalDTO> resumos = gerarResumos(servidores, mesAno);

        resumos.sort(Comparator.comparing(ResumoMensalDTO::nomeServidor));
        return List.copyOf(resumos);
    }

    // Passos
    private void validarEntrada(YearMonth mesAno) {
        if (mesAno == null) {
            throw new ApplicationException("Mês/ano é obrigatório");
        }
    }

    private List<Servidor> listarAtivos() {
        try {
            return servidorRepository.listarAtivos();
        } catch (PersistenceException e) {
            throw new ApplicationException("Erro ao listar servidores ativos", e);
        }
    }

    private List<ResumoMensalDTO> gerarResumos(List<Servidor> servidores, YearMonth mesAno) {
        List<ResumoMensalDTO> resumos = new ArrayList<>();

        for (Servidor servidor : servidores) {
            try {
                resumos.add(validarMesUseCase.executar(servidor.id(), mesAno));
            } catch (ApplicationException e) {
                log.warn("Falha ao gerar resumo do servidor {} em {}: {}",
                        servidor.id(), mesAno, e.getMessage());
            }
        }

        return resumos;
    }
}