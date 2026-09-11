package dev.douglaslira.sisvaleinterior.domain.exception;

/**
 * Exceção base para erros de domínio.
 */
public class DomainException extends RuntimeException {
    private static final long serialVersionUID = 1L; // Adicione um serialVersionUID para compatibilidade de serialização

    public DomainException(String message) {
        super(message);
    } // Adicione um construtor que aceita uma mensagem de erro
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    } // Adicione um construtor que aceita uma mensagem de erro e uma causa
}
