package dev.douglaslira.sisvaleinterior;

/**
 * Ponto de entrada do SisVale Interior.
 *
 * <p>A montagem da aplicação (tema, banco, repositórios, use cases, views,
 * controllers e janela principal) fica em {@link ApplicationBootstrap},
 * mantendo esta classe como entry point puro.</p>
 */
public class Main {

    public static void main(String[] args) {
        new ApplicationBootstrap().iniciar();
    }
}