package br.umc.oficina.aplicacao;

import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.cliente.Cliente;
import br.umc.oficina.dominio.comum.RegraDeNegocioException;
import br.umc.oficina.dominio.notificacao.Notificacao;
import br.umc.oficina.dominio.notificacao.Notificador;
import br.umc.oficina.dominio.repositorio.NotificacaoRepository;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Escolhe o {@link Notificador} pelo canal preferido do cliente e registra o histórico.
 * Recebe TODOS os notificadores como abstrações (DIP); qualquer um pode substituir
 * outro sem quebrar o fluxo (LSP). Sem if/else nem switch por canal (Calistenia).
 */
@Service
public class CentralDeNotificacoes {

    private final Map<CanalNotificacao, Notificador> notificadores = new EnumMap<>(CanalNotificacao.class);
    private final NotificacaoRepository historico;

    public CentralDeNotificacoes(List<Notificador> notificadores, NotificacaoRepository historico) {
        notificadores.forEach(n -> this.notificadores.put(n.canal(), n));
        this.historico = historico;
    }

    public Notificacao notificar(Cliente cliente, String mensagem) {
        Notificador notificador = Optional.ofNullable(notificadores.get(cliente.canalNotificacao()))
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Nenhum notificador configurado para " + cliente.canalNotificacao()));
        return historico.salvar(notificador.enviar(cliente, mensagem));
    }

    public List<Notificacao> historico(String clienteId) {
        if (clienteId == null || clienteId.isBlank()) {
            return historico.listarTodas();
        }
        return historico.listarPorCliente(clienteId);
    }
}
