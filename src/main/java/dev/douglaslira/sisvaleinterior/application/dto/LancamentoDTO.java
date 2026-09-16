package dev.douglaslira.sisvaleinterior.application.dto;

import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;
import dev.douglaslira.sisvaleinterior.domain.model.Trecho;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de leitura e escrita de {@link Lancamento} entre a UI e o domínio.
 *
 * <p>Carrega os dados básicos do lançamento (id, servidor, data) e a
 * <strong>lista de trechos</strong> — cada trecho com seus horários e valor.</p>
 *
 * <p>Não inclui status nem valor total — isso fica no
 * {@link LancamentoComStatusDTO}, que combina este DTO com o resultado da
 * validação.</p>
 */
public record LancamentoDTO(
        Long id,
        Long servidorId,
        LocalDate data,
        List<TrechoDTO> trechos
) {

    /**
     * Converte um {@link Lancamento} em {@link LancamentoDTO}.
     *
     * @param lancamento lançamento de domínio (não pode ser nulo)
     * @return DTO correspondente
     * @throws IllegalArgumentException se {@code lancamento} for nulo
     */
    public static LancamentoDTO de(Lancamento lancamento) {
        if (lancamento == null) {
            throw new IllegalArgumentException("Lançamento é obrigatório");
        }

        List<TrechoDTO> trechosDTO = lancamento.trechos().stream()
                .map(TrechoDTO::de)
                .toList();

        return new LancamentoDTO(
                lancamento.id(),
                lancamento.servidorId(),
                lancamento.data(),
                trechosDTO
        );
    }

    /**
     * Converte este DTO para um {@link Lancamento} do domínio.
     *
     * <p>O construtor do {@link Lancamento} valida os invariantes (nulos,
     * lista não vazia). Cada trecho é convertido via
     * {@link TrechoDTO#paraDominio()}.</p>
     *
     * @return lançamento de domínio
     * @throws IllegalArgumentException se algum campo obrigatório for nulo
     *                                  ou a lista de trechos estiver vazia
     */
    public Lancamento paraDominio() {
        List<Trecho> trechosDominio = trechos == null
                ? List.of()
                : trechos.stream()
                        .map(TrechoDTO::paraDominio)
                        .toList();

        return new Lancamento(id, servidorId, data, trechosDominio);
    }
}