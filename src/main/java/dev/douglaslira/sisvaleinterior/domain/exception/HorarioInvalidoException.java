package dev.douglaslira.sisvaleinterior.domain.exception;

/**
 * Exceção lançada quando um horário inválido é fornecido.
 */
public class HorarioInvalidoException extends DomainException {
    private static final long serialVersionUID = 1L; // Versão de serialização para compatibilidade

    public HorarioInvalidoException(String motivo) {
        super(motivo); // Chama o construtor da classe pai (DomainException) com a mensagem de motivo
    }

    public HorarioInvalidoException(String motivo, Throwable cause) {
        super(motivo, cause); // Chama o construtor da classe pai (DomainException) com a mensagem de motivo e a causa
    }
}
