package dev.douglaslira.sisvaleinterior.application.exception;

/**
 * Exceção raiz da camada de aplicação.
 *
 * <p>Sinaliza falhas de <strong>orquestração</strong>: pré-condições violadas
 * em casos de uso, recurso não encontrado, fluxo inválido. Não representa
 * regra de negócio (isso é {@code DomainException}) nem falha técnica de
 * persistência (isso é {@code PersistenceException}).</p>
 *
 * <p>Se um caso de uso precisar propagar um erro de persistência como erro
 * de aplicação, <strong>envolva</strong> preservando a causa:</p>
 *
 * <pre>{@code
 * try {
 *     repository.salvar(servidor);
 * } catch (PersistenceException e) {
 *     throw new ApplicationException("Erro ao salvar servidor", e);
 * }
 * }</pre>
 */
public class ApplicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message descrição do erro de aplicação
     */
    public ApplicationException(String message) {
        super(message);
    }

    /**
     * Cria a exceção com a mensagem e a causa original.
     *
     * @param message descrição do erro de aplicação
     * @param cause   exceção que originou este erro
     */
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}