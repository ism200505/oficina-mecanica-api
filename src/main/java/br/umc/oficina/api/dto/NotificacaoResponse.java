package br.umc.oficina.api.dto;

import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.notificacao.Notificacao;

import java.time.LocalDateTime;

public record NotificacaoResponse(String id, String clienteId, CanalNotificacao canal, String destino,
                                  String mensagem, LocalDateTime dataHora) {

    public static NotificacaoResponse de(Notificacao n) {
        return new NotificacaoResponse(n.id(), n.clienteId(), n.canal(), n.destino(), n.mensagem(), n.dataHora());
    }
}
