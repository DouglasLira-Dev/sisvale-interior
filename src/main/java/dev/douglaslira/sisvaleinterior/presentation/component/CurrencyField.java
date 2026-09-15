package dev.douglaslira.sisvaleinterior.presentation.component;

import javax.swing.JTextField;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Campo de texto para valores monetários em Real.
 *
 * <p>Sem formatação automática — usa {@link JTextField} simples e faz
 * parsing manual no {@link #getValor()}. Precisão garantida via
 * {@link BigDecimal}.</p>
 *
 * <p><strong>Formatos aceitos na leitura:</strong></p>
 * <ul>
 *   <li>{@code 20} → {@code 20.00}</li>
 *   <li>{@code 20,00} → {@code 20.00}</li>
 *   <li>{@code 20.00} → {@code 20.00}</li>
 *   <li>{@code 1234,56} → {@code 1234.56}</li>
 * </ul>
 *
 * <p><strong>Formatos rejeitados:</strong> {@code R$ 20,00} (com símbolo),
 * {@code 1.234,56} (separador de milhar), {@code 1,234.56} (formato
 * americano misturado), textos não numéricos.</p>
 *
 * <p>O valor é sempre normalizado para escala 2 ({@code HALF_UP}).</p>
 */
public class CurrencyField extends JTextField {

    private static final int COLUNAS = 12;
    private static final String TOOLTIP = "Valor em reais (ex: 20,00)";

    public CurrencyField() {
        setColumns(COLUNAS);
        setToolTipText(TOOLTIP);
    }

    /**
     * Lê o valor informado.
     *
     * @return o valor em {@link BigDecimal} (escala 2), ou {@code null} se
     *         o campo estiver vazio ou o texto for inválido
     */
    public BigDecimal getValor() {
        String texto = getText();
        if (texto == null) {
            return null;
        }
        texto = texto.trim();
        if (texto.isEmpty()) {
            return null;
        }

        // Aceita "20", "20,00", "20.00"; converte vírgula para ponto.
        String normalizado = texto.replace(',', '.');
        try {
            BigDecimal valor = new BigDecimal(normalizado);
            if (valor.signum() < 0) {
                return null;
            }
            return valor.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Define o valor exibido, formatado como {@code #0,00} (sem símbolo {@code R$}).
     *
     * @param valor valor a exibir; se {@code null}, limpa o campo
     */
    public void setValor(BigDecimal valor) {
        if (valor == null) {
            setText("");
            return;
        }
        setText(valor.setScale(2, RoundingMode.HALF_UP)
                .toPlainString()
                .replace('.', ','));
    }
}