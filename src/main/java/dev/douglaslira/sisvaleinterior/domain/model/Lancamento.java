package dev.douglaslira.sisvaleinterior.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Entidade que representa um lançamento diário de deslocamento de um servidor.
 *
 * <p>Um lançamento contém <strong>N trechos</strong> — cada trecho é um par
 * de horários (referência → comparada) mais o valor pago. Os trechos podem
 * representar ida, volta, baldeações, etc., dependendo do uso.</p>
 *
 * <p>É um <strong>portador de dados</strong>: não aplica a regra de
 * tolerância (isso é papel do {@code ValidadorHorario}). Aqui só se validam
 * invariantes estruturais.</p>
 *
 * <p>Imutável. {@code equals}/{@code hashCode} por {@code (servidorId, data)}.</p>
 */
public final class Lancamento {

    private final Long id;
    private final Long servidorId;
    private final LocalDate data;
    private final List<Trecho> trechos;

    /**
     * Cria um lançamento com os trechos informados.
     *
     * @param id         identificador (pode ser {@code null} antes de persistir)
     * @param servidorId id do servidor (obrigatório)
     * @param data       data do lançamento (obrigatória)
     * @param trechos    trechos do dia (pelo menos 1, não nulo)
     * @throws IllegalArgumentException se algum campo obrigatório for nulo
     *                                  ou a lista de trechos estiver vazia
     */
    public Lancamento(Long id, Long servidorId, LocalDate data, List<Trecho> trechos) {
        if (servidorId == null) {
            throw new IllegalArgumentException("Servidor é obrigatório");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }
        if (trechos == null) {
            throw new IllegalArgumentException("Trechos são obrigatórios");
        }
        if (trechos.isEmpty()) {
            throw new IllegalArgumentException("Lançamento precisa de pelo menos um trecho");
        }

        this.id = id;
        this.servidorId = servidorId;
        this.data = data;
        this.trechos = List.copyOf(trechos);
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

    public List<Trecho> trechos() {
        return trechos;
    }

    /**
     * @return a quantidade de trechos deste lançamento
     */
    public int getQuantidadeTrechos() {
        return trechos.size();
    }

    /**
     * Igualdade baseada em {@code (servidorId, data)} — combinação única
     * garantida por constraint no banco. O {@code id} não participa porque
     * pode ser {@code null} antes da persistência.
     */
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

    /**
     * Representação resumida — não despeja a lista completa de trechos.
     */
    @Override
    public String toString() {
        return "Lancamento{" +
                "id=" + id +
                ", servidorId=" + servidorId +
                ", data=" + data +
                ", trechos=" + trechos.size() +
                '}';
    }
}