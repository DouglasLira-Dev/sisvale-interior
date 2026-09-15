package dev.douglaslira.sisvaleinterior.presentation.component;

import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Campo de texto formatado para data no padrão {@code dd/MM/yyyy}.
 *
 * <p>Usa {@link MaskFormatter} para forçar o formato {@code ##/##/####} e
 * bloquear caracteres inválidos. O placeholder é um espaço, para não
 * confundir com uma data real.</p>
 *
 * <p>{@link #getData()} <strong>nunca lança</strong> — devolve {@code null}
 * para entrada vazia ou inválida (ex.: {@code "31/02/2026"} passa na máscara
 * mas fevereiro não tem 31 dias).</p>
 *
 * <p><strong>Formato interno:</strong> {@code dd/MM/uuuu} (ano próleptico),
 * não {@code yyyy} (ano da era). Mais correto para o {@link LocalDate}.</p>
 */
public class DateField extends JFormattedTextField {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final String MASCARA = "##/##/####";
    private static final int COLUNAS = 10;
    private static final String TOOLTIP = "Data no formato dd/MM/yyyy";

    /**
     * Cria o campo com máscara {@code ##/##/####}, placeholder em branco e tooltip.
     */
    public DateField() {
        super(criarFormatter());
        setColumns(COLUNAS);
        setToolTipText(TOOLTIP);
    }

    /**
     * Lê a data informada.
     *
     * @return a data, ou {@code null} se o campo estiver vazio ou o texto
     *         não corresponder a uma data válida
     */
    public LocalDate getData() {
        String texto = getText();
        if (texto == null) {
            return null;
        }
        texto = texto.trim();
        if (texto.isEmpty()) {
            return null;
        }
        // Campo só com separadores (ex.: "//", "  /  /  ") é considerado vazio
        if (texto.replace("/", "").trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(texto, FORMATO);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Define a data exibida.
     *
     * @param data data a exibir; se {@code null}, limpa o campo
     */
    public void setData(LocalDate data) {
        if (data == null) {
            setText("");
        } else {
            setText(data.format(FORMATO));
        }
    }

    /**
     * Cria o {@link MaskFormatter} do formato {@code ##/##/####}.
     *
     * <p>{@link MaskFormatter} só é instanciável via construtor que lança
     * {@link ParseException} — envolvida em {@link IllegalStateException}
     * porque a máscara é constante e não deve falhar.</p>
     */
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