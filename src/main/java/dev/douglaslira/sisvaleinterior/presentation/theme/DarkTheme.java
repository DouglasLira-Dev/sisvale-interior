package dev.douglaslira.sisvaleinterior.presentation.theme;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

/**
 * Tema escuro customizado para o SisVale Interior.
 *
 * <p>Baseado no {@link FlatDarkLaf}, com customizações de cor e fonte:</p>
 * <ul>
 *   <li>Fundo geral preto puro ({@code #000000})</li>
 *   <li>Campos de entrada com fundo branco e texto preto (contraste máximo
 *       onde o usuário digita)</li>
 *   <li>Foco visível em azul ({@code #4A90E2}) — mantém acessibilidade</li>
 *   <li>Fonte padrão {@code Segoe UI 14}, com fallback para {@code SansSerif}</li>
 * </ul>
 *
 * <p>Deve ser aplicado no início do {@code main}, <strong>antes</strong> de
 * qualquer componente Swing ser criado.</p>
 */
public final class DarkTheme {

    // Cores
    private static final Color FUNDO = new Color(0x00, 0x00, 0x00);
    private static final Color TEXTO = new Color(0xFF, 0xFF, 0xFF);
    private static final Color CAMPO_FUNDO = new Color(0xFF, 0xFF, 0xFF);
    private static final Color CAMPO_TEXTO = new Color(0x00, 0x00, 0x00);
    private static final Color FOCO = new Color(0x4A, 0x90, 0xE2);
    private static final Color TABELA_FUNDO = new Color(0x1A, 0x1A, 0x1A);
    private static final Color TABELA_ALTERNADA = new Color(0x22, 0x22, 0x22);
    private static final Color CABECALHO_FUNDO = new Color(0x2A, 0x2A, 0x2A);

    private static final String FAMILIA_FONTE = "Segoe UI";
    private static final String FAMILIA_FALLBACK = "SansSerif";
    private static final int TAMANHO_FONTE = 14;
    private static final int TAMANHO_FONTE_TABELA = 13;

    private DarkTheme() {
        // classe utilitária — não instanciável
    }

    /**
     * Aplica o tema escuro. Deve ser chamado <strong>antes</strong> de criar
     * qualquer componente Swing.
     */
    public static void aplicar() {
        FlatDarkLaf.setup();
        configurarCores();
        configurarFontes();
    }

    private static void configurarCores() {
        // Painéis e labels
        UIManager.put("Panel.background", FUNDO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("OptionPane.background", FUNDO);
        UIManager.put("OptionPane.messageForeground", TEXTO);

        // Campos de texto
        UIManager.put("TextField.background", CAMPO_FUNDO);
        UIManager.put("TextField.foreground", CAMPO_TEXTO);
        UIManager.put("PasswordField.background", CAMPO_FUNDO);
        UIManager.put("PasswordField.foreground", CAMPO_TEXTO);
        UIManager.put("FormattedTextField.background", CAMPO_FUNDO);
        UIManager.put("FormattedTextField.foreground", CAMPO_TEXTO);
        UIManager.put("TextArea.background", CAMPO_FUNDO);
        UIManager.put("TextArea.foreground", CAMPO_TEXTO);

        // ComboBox
        UIManager.put("ComboBox.background", CAMPO_FUNDO);
        UIManager.put("ComboBox.foreground", CAMPO_TEXTO);

        // Tabela
        UIManager.put("Table.background", TABELA_FUNDO);
        UIManager.put("Table.foreground", TEXTO);
        UIManager.put("Table.alternateRowColor", TABELA_ALTERNADA);
        UIManager.put("Table.selectionBackground", FOCO);
        UIManager.put("Table.selectionForeground", TEXTO);
        UIManager.put("TableHeader.background", CABECALHO_FUNDO);
        UIManager.put("TableHeader.foreground", TEXTO);

        // Foco visível (acessibilidade — obrigatório)
        UIManager.put("Component.focusColor", FOCO);
        UIManager.put("TextField.focusedBorderColor", FOCO);
        UIManager.put("PasswordField.focusedBorderColor", FOCO);
        UIManager.put("FormattedTextField.focusedBorderColor", FOCO);
        UIManager.put("ComboBox.focusedBorderColor", FOCO);
    }

    private static void configurarFontes() {
        Font padrao = fonteSegura(TAMANHO_FONTE);
        Font tabela = fonteSegura(TAMANHO_FONTE_TABELA);

        UIManager.put("defaultFont", padrao);
        UIManager.put("Button.font", padrao);
        UIManager.put("Label.font", padrao);
        UIManager.put("TextField.font", padrao);
        UIManager.put("PasswordField.font", padrao);
        UIManager.put("FormattedTextField.font", padrao);
        UIManager.put("TextArea.font", padrao);
        UIManager.put("ComboBox.font", padrao);
        UIManager.put("OptionPane.font", padrao);
        UIManager.put("Menu.font", padrao);
        UIManager.put("MenuItem.font", padrao);
        UIManager.put("Table.font", tabela);
        UIManager.put("TableHeader.font", tabela);
    }

    /**
     * Cria uma fonte com a família preferida; se não existir, cai para o
     * fallback (SansSerif).
     */
    private static Font fonteSegura(int tamanho) {
        Font fonte = new Font(FAMILIA_FONTE, Font.PLAIN, tamanho);
        if (FAMILIA_FONTE.equals(fonte.getFamily())) {
            return fonte;
        }
        return new Font(FAMILIA_FALLBACK, Font.PLAIN, tamanho);
    }
}