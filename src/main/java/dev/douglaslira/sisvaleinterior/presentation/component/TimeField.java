package dev.douglaslira.sisvaleinterior.presentation.component;

import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Campo de texto formatado para horário no padrão {@code HH:mm} (24h).
 *
 * <p>Usa {@link MaskFormatter} para forçar o formato {@code ##:##} e
 * bloquear caracteres inválidos. O placeholder é um espaço, para não
 * confundir com {@code 00:00}.</p>
 *
 * <p>{@link #getTime()} <strong>nunca lança</strong> — devolve {@code null}
 * para entrada vazia ou inválida (ex.: {@code "25:99"} passa na máscara mas
 * não é um horário real).</p>
 */
public class TimeField extends JFormattedTextField {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("HH:mm");
    private static final String MASCARA = "##:##";
    private static final int COLUNAS = 5;
    private static final String TOOLTIP = "Horário no formato HH:mm (24h)";

    /**
     * Cria o campo com máscara {@code ##:##}, placeholder em branco e tooltip.
     */
    public TimeField() {
        super(criarFormatter());
        setColumns(COLUNAS);
        setToolTipText(TOOLTIP);
    }

    /**
     * Lê o horário informado.
     *
     * @return o horário, ou {@code null} se o campo estiver vazio ou o texto
     *         não corresponder a um horário válido
     */
    public LocalTime getTime() {
        String texto = getText();
        if (texto == null) {
            return null;
        }
        texto = texto.trim();
        if (texto.isEmpty() || texto.equals(":")) {
            return null;
        }
        try {
            return LocalTime.parse(texto, FORMATO);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Define o horário exibido.
     *
     * @param hora horário a exibir; se {@code null}, limpa o campo
     */
    public void setTime(LocalTime hora) {
        if (hora == null) {
            setText("");
        } else {
            setText(hora.format(FORMATO));
        }
    }

    /**
     * Cria o {@link MaskFormatter} do formato {@code ##:##}.
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