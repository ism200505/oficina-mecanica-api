package br.umc.oficina.dominio.repositorio;

import br.umc.oficina.dominio.notificacao.Notificacao;

import java.util.List;

public interface NotificacaoRepository {

    Notificacao salvar(Notificacao notificacao);

    List<Notificacao> listarTodas();

    List<Notificacao> listarPorCliente(String clienteId);
}
