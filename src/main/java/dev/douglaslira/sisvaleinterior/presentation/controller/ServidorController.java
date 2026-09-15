package dev.douglaslira.sisvaleinterior.presentation.controller;

import dev.douglaslira.sisvaleinterior.application.dto.ServidorDTO;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.application.usecase.CadastrarServidorUseCase;
import dev.douglaslira.sisvaleinterior.application.usecase.ListarServidoresUseCase;
import dev.douglaslira.sisvaleinterior.presentation.view.TelaCadastroServidor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controller da tela de cadastro de servidores.
 *
 * <p>Orquestra a interação entre a {@link TelaCadastroServidor} e os
 * use cases {@link CadastrarServidorUseCase} e {@link ListarServidoresUseCase}.</p>
 *
 * <p>Segue o padrão MVP: a View não conhece use cases, o Controller não
 * conhece Swing. Toda comunicação com a UI passa por métodos da View.</p>
 */
public final class ServidorController {

    private static final Logger log = LoggerFactory.getLogger(ServidorController.class);

    private final TelaCadastroServidor view;
    private final CadastrarServidorUseCase cadastrar;
    private final ListarServidoresUseCase listar;

    /**
     * @param view      tela de cadastro (não pode ser nula)
     * @param cadastrar caso de uso de cadastro (não pode ser nulo)
     * @param listar    caso de uso de listagem (não pode ser nulo)
     * @throws IllegalArgumentException se algum argumento for nulo
     */
    public ServidorController(TelaCadastroServidor view,
                            CadastrarServidorUseCase cadastrar,
                            ListarServidoresUseCase listar) {
        if (view == null) {
            throw new IllegalArgumentException("Tela é obrigatória");
        }
        if (cadastrar == null) {
            throw new IllegalArgumentException("CadastrarServidorUseCase é obrigatório");
        }
        if (listar == null) {
            throw new IllegalArgumentException("ListarServidoresUseCase é obrigatório");
        }

        this.view = view;
        this.cadastrar = cadastrar;
        this.listar = listar;

        view.adicionarListenerSalvar(e -> onSalvar());
        view.adicionarListenerCancelar(e -> view.limparFormulario());

        carregarServidoresInicial();
    }

    // Ações
    private void onSalvar() {
        String nome = view.getNome();
        String matricula = view.getMatricula();
        String cpf = view.getCpf();

        try {
            ServidorDTO dto = cadastrar.executar(nome, matricula, cpf);
            view.limparFormulario();
            carregarServidores();
            view.mostrarMensagem("Sucesso", "Servidor cadastrado: " + dto.nome());
        } catch (ApplicationException e) {
            view.mostrarErro(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao cadastrar servidor", e);
            view.mostrarErro("Erro inesperado ao cadastrar servidor. Consulte o log.");
        }
    }

    private void carregarServidores() {
        List<ServidorDTO> servidores = listar.executar();
        view.popularTabela(servidores);
    }

    private void carregarServidoresInicial() {
        try {
            carregarServidores();
        } catch (ApplicationException e) {
            view.mostrarErro("Erro ao carregar servidores: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao carregar servidores", e);
            view.mostrarErro("Erro inesperado ao carregar servidores. Consulte o log.");
        }
    }
}