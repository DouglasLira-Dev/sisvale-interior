package dev.douglaslira.sisvaleinterior.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * Leitor centralizado das configurações da aplicação.
 *
 * <p>Carrega {@code application.properties} do classpath uma única vez
 * (na primeira referência à classe) e expõe os valores de forma tipada.
 * Evita {@code Properties.getProperty(...)} espalhado pelo código.</p>
 *
 * <p>Classe utilitária estática — não instanciável.</p>
 */
public final class AppConfig {

    private static final String ARQUIVO = "application.properties";

    private static final String CHAVE_DB_URL = "db.url";
    private static final String CHAVE_TOLERANCIA = "regra.tolerancia.minutos";
    private static final String CHAVE_UI_TITULO = "ui.titulo";

    private static final int TOLERANCIA_DEFAULT_MINUTOS = 15;

    private static final Properties PROPS = carregar();

    private AppConfig() {
        // classe utilitária — não instanciável
    }

    /**
     * Carrega {@code application.properties} do classpath.
     *
     * @throws IllegalStateException se o arquivo não for encontrado ou não puder ser lido
     */
    private static Properties carregar() {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(ARQUIVO)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Arquivo " + ARQUIVO + " não encontrado no classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Falha ao ler " + ARQUIVO, e);
        }
        return props;
    }

    /**
     * @return URL JDBC do banco
     * @throws IllegalStateException se {@code db.url} não estiver configurada
     */
    public static String dbUrl() {
        String valor = PROPS.getProperty(CHAVE_DB_URL);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "Propriedade obrigatória ausente: " + CHAVE_DB_URL);
        }
        return valor.trim();
    }

    /**
     * @return tolerância em minutos; usa {@value #TOLERANCIA_DEFAULT_MINUTOS}
     *         se a propriedade não estiver presente
     * @throws IllegalStateException se a propriedade estiver presente mas inválida
     */
    public static int toleranciaMinutos() {
        String valor = PROPS.getProperty(CHAVE_TOLERANCIA);
        if (valor == null || valor.isBlank()) {
            return TOLERANCIA_DEFAULT_MINUTOS;
        }

        int minutos;
        try {
            minutos = Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    CHAVE_TOLERANCIA + " deve ser um número inteiro positivo, mas foi: " + valor, e);
        }

        if (minutos <= 0) {
            throw new IllegalStateException(
                    CHAVE_TOLERANCIA + " deve ser um número inteiro positivo, mas foi: " + minutos);
        }

        return minutos;
    }

    /**
     * @return tolerância como {@link Duration} — para uso direto com o domínio
     */
    public static Duration tolerancia() {
        return Duration.ofMinutes(toleranciaMinutos());
    }

    /**
     * @return título da aplicação exibido na UI;
     *         usa {@code "SisVale Interior"} se a propriedade não estiver presente
     */
    public static String uiTitulo() {
        String valor = PROPS.getProperty(CHAVE_UI_TITULO);
        return (valor == null || valor.isBlank()) ? "SisVale Interior" : valor.trim();
    }
}