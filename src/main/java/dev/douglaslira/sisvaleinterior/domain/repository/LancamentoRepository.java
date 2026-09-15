package dev.douglaslira.sisvaleinterior.domain.repository;

import dev.douglaslira.sisvaleinterior.domain.model.Lancamento;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de {@link Lancamento}.
 *
 * <p>Vive no domínio — define <strong>o que</strong> precisa ser persistido,
 * não <strong>como</strong>. A implementação concreta (JDBC, SQLite, etc.)
 * fica na camada de infraestrutura.</p>
 *
 * <p>Nenhuma operação aqui expõe detalhes técnicos (Connection, ResultSet,
 * SQLException). Falhas de persistência são sinalizadas via
 * {@code PersistenceException} (runtime).</p>
 */
public interface LancamentoRepository {

    /**
     * Salva o lançamento — <strong>insere</strong> se o {@code id} for nulo,
     * <strong>atualiza</strong> caso contrário.
     *
     * <p>Respeita a constraint {@code UNIQUE(servidor_id, data)}: tentar
     * salvar um lançamento duplicado para o mesmo servidor no mesmo dia
     * resulta em falha de persistência.</p>
     *
     * @param lancamento lançamento a salvar (não pode ser nulo)
     * @return o lançamento salvo, com {@code id} preenchido após inserção
     */
    Lancamento salvar(Lancamento lancamento);

    /**
     * @param id identificador do lançamento
     * @return o lançamento, se existir
     */
    Optional<Lancamento> buscarPorId(Long id);

    /**
     * Busca todos os lançamentos de um servidor em um mês específico.
     * <p>Este é o método consumido pela {@code CalculadoraRessarcimento}
     * para consolidar o mês.</p>
     * @param servidorId identificador do servidor
     * @param mes        mês/ano de referência
     * @return lista de lançamentos no mês (possivelmente vazia)
     */
    List<Lancamento> buscarPorServidorEMes(Long servidorId, YearMonth mes);

    /**
     * Busca o lançamento de um servidor em uma data específica.
     * <p>Útil para verificar duplicidade antes de salvar — respeita a
     * constraint {@code UNIQUE(servidor_id, data)}.</p>
     * @param servidorId identificador do servidor
     * @param data       data do lançamento
     * @return o lançamento, se existir
     */
    Optional<Lancamento> buscarPorServidorEData(Long servidorId, LocalDate data);

    /**
     * @param servidorId identificador do servidor
     * @return todos os lançamentos do servidor, em qualquer mês
     */
    List<Lancamento> listarPorServidor(Long servidorId);

    /**
     * Remove o lançamento definitivamente (hard delete).
     * <p>Diferente do {@code ServidorRepository}, lançamento é dado
     * transacional — não faz sentido manter histórico de lançamento
     * removido. Se o objetivo for desfazer um lançamento errado,  remove-se
     * a linha.</p>
     * @param id identificador do lançamento a remover
     */
    void remover(Long id);
}