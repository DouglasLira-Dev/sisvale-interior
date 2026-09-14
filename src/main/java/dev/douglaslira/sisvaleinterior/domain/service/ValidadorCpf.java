package dev.douglaslira.sisvaleinterior.domain.service;

// Classe utilitária para validação de CPF
public final class ValidadorCpf {
    private ValidadorCpf() {
        // classe utilitária - não deve ser instanciada
    }

    public static boolean isValido(String cpf) {
        // Implementação da validação de CPF
        if (cpf == null) {
            return false;
        }
        // Remove caracteres não numéricos
        String cpfLimpo = normalizar(cpf);

        // Verifica se o CPF possui 11 dígitos
        if (cpfLimpo.length() !=11) {
            return false;
        }
        // Verifica se todos os dígitos são iguais (sequência inválida)
        if (isSequenciaInvalida(cpfLimpo)) {
            return false;
        }
        // Calcula o primeiro dígito verificador
        int dv1 = calcularDigitoVerificador(cpfLimpo.substring(0, 9), 10);
        // Calcula o segundo dígito verificador
        if (dv1 != Character.getNumericValue(cpfLimpo.charAt(9))) {
            return false;
        }
        // Calcula o segundo dígito verificador
        int dv2 = calcularDigitoVerificador(cpfLimpo.substring(0, 10), 11);
        return dv2 == Character.getNumericValue(cpfLimpo.charAt(10));
    }

    // Método auxiliar para verificar se todos os dígitos são iguais
    public static String normalizar(String cpf) {
        // Remove caracteres não numéricos 
        return cpf.replaceAll("[^\\d]", "");
    }

    // Método auxiliar para calcular o dígito verificador
    private static boolean isSequenciaInvalida(String cpf) {
        
        char primeiroDigito = cpf.charAt(0);
        // Verifica se todos os dígitos são iguais
        for (int i = 1; i < cpf.length(); i++) {
            if (cpf.charAt(i) != primeiroDigito) {
                return false;
            } // Se encontrar um dígito diferente, não é uma sequência inválida
        }
        return true; // Todos os dígitos são iguais, é uma sequência inválida
    }

    // Método auxiliar para calcular o dígito verificador
    private static int calcularDigitoVerificador(String cpfParcial, int pesoInicial) {
        int soma = 0; // Inicializa a soma dos produtos dos dígitos pelo peso
        int peso = pesoInicial; // Inicializa o peso com o valor fornecido

        // Itera sobre cada dígito do CPF parcial
        for (int i = 0; i < cpfParcial.length(); i++) {
            int digito = Character.getNumericValue(cpfParcial.charAt(i)); // Converte o caractere para um valor numérico
            soma += digito * peso; // Adiciona o produto do dígito pelo peso à soma
            peso--; // Decrementa o peso para o próximo dígito
        }
        int resto = soma % 11; // Calcula o resto da divisão da soma por 11
        return (resto < 2) ? 0 : 11 - resto; // Retorna o dígito verificador calculado
    }

    public static boolean isValid(String cpf) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isValid'");
    }
}
