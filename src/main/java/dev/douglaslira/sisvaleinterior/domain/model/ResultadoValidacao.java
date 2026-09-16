package dev.douglaslira.sisvaleinterior.domain.model;
    
// Classe que representa o resultado da validação de um determinado processo, contendo informações sobre a validade, o motivo e a diferença em minutos.
public record ResultadoValidacao(boolean valido, String motivo, long diferencaMinutos) {
    // Construtor para validação do motivo
    public ResultadoValidacao {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("O motivo é obrigatório e não pode ser nulo ou vazio.");
        }
    }

    // Método estático para criar um resultado válido com o motivo "Dentro da tolerância"
    public static ResultadoValidacao valido(long diferencaMinutos) {
        return new ResultadoValidacao(true, "Dentro da tolerância", diferencaMinutos); 
    }

    // Método estático para criar um resultado inválido com o motivo fornecido
    public static ResultadoValidacao invalido(long diferencaMinutos, String motivo) {
        return new ResultadoValidacao(false, motivo, diferencaMinutos);
    }

    public static ResultadoValidacao naoInformado() {
        return new ResultadoValidacao(true, "Não lançado", 0);
    }
}
