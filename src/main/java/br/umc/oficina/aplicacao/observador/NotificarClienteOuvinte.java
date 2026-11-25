package br.umc.oficina.aplicacao.observador;

import br.umc.oficina.aplicacao.CentralDeNotificacoes;
import br.umc.oficina.dominio.ordem.OrdemServico;
import br.umc.oficina.dominio.repositorio.ClienteRepository;
import org.springframework.stereotype.Component;

/** AUTOMAÇÃO: avisa o cliente, pelo canal que ele prefere, a cada mudança de status da OS. */
@Component
public class NotificarClienteOuvinte implements OuvinteDeStatus {

    private final ClienteRepository clientes;
    private final CentralDeNotificacoes central;

    public NotificarClienteOuvinte(ClienteRepository clientes, CentralDeNotificacoes central) {
        this.clientes = clientes;
        this.central = central;
    }

    @Override
    public void statusAlterado(OrdemServico ordem) {
        clientes.buscarPorId(ordem.clienteId())
                .ifPresent(cliente -> central.notificar(cliente, ordem.mensagemDeAcompanhamento()));
    }
}
