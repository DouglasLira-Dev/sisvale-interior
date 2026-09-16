package dev.douglaslira.sisvaleinterior.presentation.controller;

import dev.douglaslira.sisvaleinterior.application.dto.ServidorDTO;
import dev.douglaslira.sisvaleinterior.application.exception.ApplicationException;
import dev.douglaslira.sisvaleinterior.application.usecase.CadastrarServidorUseCase;
import dev.douglaslira.sisvaleinterior.application.usecase.DesativarServidorUseCase;
import dev.douglaslira.sisvaleinterior.application.usecase.ListarServidoresUseCase;
import dev.douglaslira.sisvaleinterior.application.usecase.RemoverServidorUseCase;
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
    private final DesativarServidorUseCase desativar;
    private final RemoverServidorUseCase remover;

    /**
     * @param view      tela de cadastro (não pode ser nula)
     * @param cadastrar caso de uso de cadastro (não pode ser nulo)
     * @param listar    caso de uso de listagem (não pode ser nulo)
     * @throws IllegalArgumentException se algum argumento for nulo
     */
    public ServidorController(TelaCadastroServidor view,
                            CadastrarServidorUseCase cadastrar,
                            ListarServidoresUseCase listar,
                            DesativarServidorUseCase desativar,
                            RemoverServidorUseCase remover) {
        if (view == null) {
            throw new IllegalArgumentException("Tela é obrigatória");
        }
        if (cadastrar == null) {
            throw new IllegalArgumentException("CadastrarServidorUseCase é obrigatório");
        }
        if (listar == null) {
            throw new IllegalArgumentException("ListarServidoresUseCase é obrigatório");
        }
        if (desativar == null) {
            throw new IllegalArgumentException("DesativarServidorUseCase é obrigatório");
        }
        if (remover == null ){
            throw new IllegalArgumentException("RemoverServidorUseCase é obrigatório");
        }

        this.view = view;
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.desativar = desativar;
        this.remover = remover;

        view.adicionarListenerSalvar(e -> onSalvar());
        view.adicionarListenerDesativar(e -> onDesativar());
        view.adicionarListenerExcluir(e -> onExcluir());
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

    private void onDesativar() {
        ServidorDTO selecionado = view.getServidorSelecionado();
        if (selecionado == null) {
            view.mostrarErro("Selecione um servidor na tabela.");
            return;
        }
        if (!view.confirmar("Desativar o servidor " + selecionado.nome() + "?")) {
            return;
        }
        try {
            desativar.executar(selecionado.id());
            carregarServidores();
            view.mostrarMensagem("Sucesso", "Servidor desativado.");
        } catch (ApplicationException e) {
            view.mostrarErro(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao desativar servidor", e);
            view.mostrarErro("Erro inesperado ao desativar servidor. Consulte o log.");
        }
    }

    private void onExcluir() {
        ServidorDTO selecionado = view.getServidorSelecionado();
        if (selecionado == null) {
            view.mostrarErro("Selecione um servidor na tabela.");
            return;
        }
        if (!view.confirmar("Excluir definitivamente o servidor "
                + selecionado.nome() + "? Essa ação não pode ser desfeita.")) {
            return;
        }
        try {
            remover.executar(selecionado.id());
            carregarServidores();
            view.mostrarMensagem("Sucesso", "Servidor excluído.");
        } catch (ApplicationException e) {
            view.mostrarErro(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao excluir servidor", e);
            view.mostrarErro("Erro inesperado ao excluir servidor. Consulte o log.");
        }
    }
}