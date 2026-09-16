package dev.douglaslira.sisvaleinterior.presentation.component;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Fábrica de botões padronizados (Salvar, Cancelar, Novo, Excluir).
 *
 * <p>Cada botão já vem com texto, tooltip e atalho de teclado configurados.
 * Os atalhos usam {@code setMnemonic} (Alt + letra), que dá feedback visual
 * sublinhando a letra no texto do botão. O {@code Esc} para Cancelar usa
 * {@link InputMap} / {@link ActionMap} em {@code WHEN_IN_FOCUSED_WINDOW}.</p>
 *
 * <p>Classe utilitária — não instanciável.</p>
 */
public final class ButtonFactory {

    private ButtonFactory() {
        // classe utilitária — não instanciável
    }

    /**
     * Botão "Salvar" com atalho {@code Alt+S}.
     */
    public static JButton criarSalvar() {
        return configurar("Salvar", "Salvar (Alt+S)", KeyEvent.VK_S);
    }

    /**
     * Botão "Novo" com atalho {@code Alt+N}.
     */
    public static JButton criarNovo() {
        return configurar("Novo", "Novo (Alt+N)", KeyEvent.VK_N);
    }

    /**
     * Botão "Excluir" com atalho {@code Alt+E}.
     */
    public static JButton criarExcluir() {
        return configurar("Excluir", "Excluir (Alt+E)", KeyEvent.VK_E);
    }

    /**
     * Botão "Desativar" com atalho {@code alt+D}.
     */
    public static JButton criarDesativar(){
        return configurar("Desativar", "Desativar (alt+D)", KeyEvent.VK_D);
    }

    /**
     * Botão "Cancelar" com atalho {@code Esc}.
     *
     * <p>O atalho {@code Esc} é registrado em {@code WHEN_IN_FOCUSED_WINDOW}:
     * funciona em qualquer lugar da janela enquanto o botão estiver visível.
     * Em diálogos modais, o {@code Esc} do diálogo tem prioridade.</p>
     */
    public static JButton criarCancelar() {
        JButton botao = new JButton("Cancelar");
        botao.setToolTipText("Cancelar (Esc)");
        registrarEsc(botao);
        return botao;
    }

    // Helpers privados
    private static JButton configurar(String texto, String tooltip, int mnemonic) {
        JButton botao = new JButton(texto);
        botao.setToolTipText(tooltip);
        botao.setMnemonic(mnemonic);
        return botao;
    }

    /**
     * Registra {@code Esc} no botão, disparando {@code doClick()} quando
     * pressionado em qualquer lugar da janela.
     */
    private static void registrarEsc(JButton botao) {
        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

        InputMap inputMap = botao.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(esc, "cancelar");

        ActionMap actionMap = botao.getActionMap();
        actionMap.put("cancelar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                botao.doClick();
            }
        });
    }
}