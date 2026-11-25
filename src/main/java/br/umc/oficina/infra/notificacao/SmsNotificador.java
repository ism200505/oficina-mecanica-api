package br.umc.oficina.infra.notificacao;

import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.cliente.Cliente;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class SmsNotificador extends NotificadorRegistrado {

    public SmsNotificador(Clock relogio) {
        super(relogio);
    }

    @Override
    public CanalNotificacao canal() {
        return CanalNotificacao.SMS;
    }

    @Override
    protected String destinoDo(Cliente cliente) {
        return cliente.telefone().numero();
    }
}
