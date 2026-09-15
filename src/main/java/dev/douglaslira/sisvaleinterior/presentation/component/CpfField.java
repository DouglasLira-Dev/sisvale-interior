package dev.douglaslira.sisvaleinterior.presentation.component;

import dev.douglaslira.sisvaleinterior.domain.service.ValidadorCpf;

import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;

/**
 * Campo de texto formatado para CPF no padrão {@code ###.###.###-##}.
 *
 * <p>Usa {@link MaskFormatter} para forçar o formato e bloquear caracteres
 * inválidos. O placeholder é um espaço, para não confundir com zeros.</p>
 *
 * <p>{@link #getCpf()} <strong>sempre devolve o CPF puro</strong> (11 dígitos
 * sem máscara), pronto para o domínio. {@link #setCpf(String)} aceita tanto
 * puro quanto mascarado.</p>
 */
public class CpfField extends JFormattedTextField {

    private static final String MASCARA = "###.###.###-##";
    private static final int COLUNAS = 14;
    private static final String TOOLTIP = "CPF no formato 000.000.000-00";

    /**
     * Cria o campo com máscara {@code ###.###.###-##}, placeholder em branco e tooltip.
     */
    public CpfField() {
        super(criarFormatter());
        setColumns(COLUNAS);
        setToolTipText(TOOLTIP);
    }

    /**
     * Lê o CPF informado, já sem máscara.
     *
     * @return CPF com 11 dígitos (sem pontos nem traço), ou string vazia
     *         se o campo estiver vazio
     */
    public String getCpf() {
        String texto = getText();
        if (texto == null) {
            return "";
        }
        return ValidadorCpf.normalizar(texto);
    }

    /**
     * Define o CPF exibido. Aceita com ou sem máscara.
     *
     * @param cpf CPF (puro ou mascarado); se {@code null} ou vazio, limpa o campo
     */
    public void setCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            setText("");
            return;
        }
        String puro = ValidadorCpf.normalizar(cpf);
        if (puro.length() != 11) {
            setText("");
            return;
        }
        String formatado = puro.substring(0, 3)
                + "." + puro.substring(3, 6)
                + "." + puro.substring(6, 9)
                + "-" + puro.substring(9);
        setText(formatado);
    }

    private static MaskFormatter criarFormatter() {
        try {
            MaskFormatter formatter = new MaskFormatter(MASCARA);
            formatter.setPlaceholderCharacter(' ');
            formatter.setAllowsInvalid(false);
            return formatter;
        } catch (ParseException e) {
            throw new IllegalStateException("Máscara inválida: " + MASCARA, e);
        }
    }
}