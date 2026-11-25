package br.umc.oficina.infra.notificacao;

import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.cliente.Cliente;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class EmailNotificador extends NotificadorRegistrado {

    public EmailNotificador(Clock relogio) {
        super(relogio);
    }

    @Override
    public CanalNotificacao canal() {
        return CanalNotificacao.EMAIL;
    }

    @Override
    protected String destinoDo(Cliente cliente) {
        return cliente.email().endereco();
    }
}
