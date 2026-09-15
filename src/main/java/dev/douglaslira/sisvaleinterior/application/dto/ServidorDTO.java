package dev.douglaslira.sisvaleinterior.application.dto;

import dev.douglaslira.sisvaleinterior.domain.model.Servidor;

/**
 * DTO de leitura de {@link Servidor} para a UI.
 *
 * <p><strong>LGPD:</strong> o CPF é exposto <strong>mascarado</strong>
 * ({@code ***.***.477-35}) — nunca completo. A UI raramente precisa do CPF
 * inteiro para identificar um servidor (nome + matrícula bastam).</p>
 *
 * <p>É um DTO apenas de <strong>saída</strong>. Não use para entrada de dados —
 * os use cases recebem parâmetros soltos.</p>
 */
public record ServidorDTO(
        Long id,
        String nome,
        String matricula,
        String cpf,
        boolean ativo
) {

    /**
     * Converte um {@link Servidor} em {@link ServidorDTO}, mascarando o CPF.
     *
     * @param servidor servidor de domínio (não pode ser nulo)
     * @return DTO correspondente
     * @throws IllegalArgumentException se {@code servidor} for nulo
     */
    public static ServidorDTO de(Servidor servidor) {
        if (servidor == null) {
            throw new IllegalArgumentException("Servidor é obrigatório");
        }
        return new ServidorDTO(
                servidor.id(),
                servidor.nome(),
                servidor.matricula(),
                mascararCpf(servidor.cpf()),
                servidor.ativo()
        );
    }

    /**
     * Mascara o CPF, mantendo visíveis apenas os 3 dígitos centrais
     * e os 2 dígitos verificadores.
     *
     * <p>Exemplo: {@code 11144477735} → {@code ***.***.477-35}</p>
     */
    private static String mascararCpf(String cpf) {
        return "***.***." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}