package dev.douglaslira.sisvaleinterior.domain.exception;

public class CpfInvalidoException extends DomainException{
    private static final long serialVersionUID = 1L;

    // Mensagem padrão da exceção
    private static final String MENSAGEM = "CPF informado é inválido";

    // Construtor padrão da exceção
    public CpfInvalidoException(){
        super(MENSAGEM);
    }

    // Construtor da exceção com causa
    public CpfInvalidoException(Throwable cause){
        super(MENSAGEM, cause);
    }

}
