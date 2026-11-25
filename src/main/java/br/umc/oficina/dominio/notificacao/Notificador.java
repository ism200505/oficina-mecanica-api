package br.umc.oficina.dominio.notificacao;

import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.cliente.Cliente;

/**
 * Abstração do envio de notificações (DIP - mesma ideia do desafio "Sistema de Notificações Engessado").
 * As regras de negócio dependem desta interface, e não de E-mail/WhatsApp/SMS concretos.
 *
 * Contrato (LSP): para qualquer cliente válido, envia a mensagem pelo seu canal e
 * devolve a {@link Notificacao} registrada; nenhuma implementação lança "não suportado".
 */
public interface Notificador {

    CanalNotificacao canal();

    Notificacao enviar(Cliente cliente, String mensagem);
}
