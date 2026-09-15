package dev.douglaslira.sisvaleinterior.infrastructure.persistence.exception;

/**
 * Exceção lançada quando ocorre falha em operações de persistência
 * (SQLite, JDBC, arquivos, etc.).
 * <p>Existe para <strong>encapsular</strong> exceções técnicas como
 * {@link java.sql.SQLException}, impedindo que o domínio e a aplicação
 * dependam diretamente de detalhes de JDBC. Quem captura é a camada de
 * aplicação (use case) ou o handler global da UI.</p>
 * <p><strong>Não</strong> estende {@code DomainException}: não é um erro
 * de negócio, é um erro técnico de infraestrutura.</p>
 * <p>Ao lançar, <strong>sempre preserve a causa original</strong> — passar
 * o {@link Throwable} para o construtor adequado mantém a stack trace
 * intacta para debug:</p>
 * <pre>{@code
 * try {
 *     // operação JDBC
 * } catch (SQLException e) {
 *     throw new PersistenceException("Erro ao salvar servidor", e);
 * }
 * }</pre>
 */
public class PersistenceException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    /**
     * Cria a exceção com a mensagem informada.
     * @param message descrição do erro de persistência
     */
    public PersistenceException(String message) {
        super(message);
    }
    /**
     * Cria a exceção com a mensagem e a causa original.
     * <p>Use este construtor sempre que estiver encapsulando uma exceção
     * técnica (ex.: {@code SQLException}) — a causa preserva a stack trace.</p>
     * @param message descrição do erro de persistência
     * @param cause   exceção técnica original
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}