package br.umc.oficina.infra.notificacao;

import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.cliente.Cliente;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class WhatsAppNotificador extends NotificadorRegistrado {

    public WhatsAppNotificador(Clock relogio) {
        super(relogio);
    }

    @Override
    public CanalNotificacao canal() {
        return CanalNotificacao.WHATSAPP;
    }

    @Override
    protected String destinoDo(Cliente cliente) {
        return "+55" + cliente.telefone().numero();
    }
}
