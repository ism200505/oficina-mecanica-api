package br.umc.oficina.dominio.notificacao;

import br.umc.oficina.dominio.cliente.CanalNotificacao;

import java.time.LocalDateTime;

/** Registro (histórico) de uma notificação enviada automaticamente a um cliente. */
public record Notificacao(String id, String clienteId, CanalNotificacao canal,
                          String destino, String mensagem, LocalDateTime dataHora) {
}
