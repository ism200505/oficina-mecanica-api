package br.umc.oficina.infra.notificacao;

import br.umc.oficina.dominio.cliente.Cliente;
import br.umc.oficina.dominio.notificacao.Notificacao;
import br.umc.oficina.dominio.notificacao.Notificador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Base comum dos notificadores (Template Method). Cada subclasse só informa o destino e o
 * "transporte"; o envio e o registro seguem sempre o mesmo contrato, então qualquer
 * subclasse pode substituir outra sem efeitos colaterais (LSP).
 *
 * Em ambiente acadêmico o envio é simulado no log; para produção basta trocar o
 * método {@link #transmitir} pela chamada da API real (SendGrid, Meta, operadora SMS).
 */
public abstract class NotificadorRegistrado implements Notificador {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorRegistrado.class);

    private final Clock relogio;

    protected NotificadorRegistrado(Clock relogio) {
        this.relogio = relogio;
    }

    @Override
    public final Notificacao enviar(Cliente cliente, String mensagem) {
        String destino = destinoDo(cliente);
        transmitir(destino, mensagem);
        return new Notificacao(UUID.randomUUID().toString(), cliente.id(), canal(), destino, mensagem,
                LocalDateTime.now(relogio).truncatedTo(ChronoUnit.SECONDS));
    }

    protected abstract String destinoDo(Cliente cliente);

    protected void transmitir(String destino, String mensagem) {
        LOG.info("[{}] para {}: {}", canal(), destino, mensagem);
    }
}
