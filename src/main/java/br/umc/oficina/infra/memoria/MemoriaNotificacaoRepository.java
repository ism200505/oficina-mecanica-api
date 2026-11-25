package br.umc.oficina.infra.memoria;

import br.umc.oficina.dominio.notificacao.Notificacao;
import br.umc.oficina.dominio.repositorio.NotificacaoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
@Profile("memoria")
public class MemoriaNotificacaoRepository implements NotificacaoRepository {

    private final List<Notificacao> dados = new CopyOnWriteArrayList<>();

    @Override
    public Notificacao salvar(Notificacao notificacao) {
        dados.add(notificacao);
        return notificacao;
    }

    @Override
    public List<Notificacao> listarTodas() {
        return dados.stream().sorted(Comparator.comparing(Notificacao::dataHora).reversed()).toList();
    }

    @Override
    public List<Notificacao> listarPorCliente(String clienteId) {
        return listarTodas().stream().filter(n -> n.clienteId().equals(clienteId)).toList();
    }
}
