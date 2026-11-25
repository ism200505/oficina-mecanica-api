package br.umc.oficina.infra.firebase;

import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.notificacao.Notificacao;
import br.umc.oficina.dominio.repositorio.NotificacaoRepository;
import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

/** Coleção "notificacoes": histórico das mensagens enviadas automaticamente. */
@Repository
@Profile("!memoria")
public class FirestoreNotificacaoRepository extends RepositorioFirestore implements NotificacaoRepository {

    public FirestoreNotificacaoRepository(Firestore firestore) {
        super(firestore, "notificacoes");
    }

    @Override
    public Notificacao salvar(Notificacao n) {
        gravar(n.id(), Documento.novo()
                .com("id", n.id()).com("clienteId", n.clienteId()).com("canal", n.canal())
                .com("destino", n.destino()).com("mensagem", n.mensagem()).com("dataHora", n.dataHora()));
        return n;
    }

    @Override
    public List<Notificacao> listarTodas() {
        return ordenar(lerTodos());
    }

    @Override
    public List<Notificacao> listarPorCliente(String clienteId) {
        return ordenar(lerOnde("clienteId", clienteId));
    }

    private List<Notificacao> ordenar(List<Documento> documentos) {
        return documentos.stream()
                .map(this::paraNotificacao)
                .sorted(Comparator.comparing(Notificacao::dataHora).reversed())
                .toList();
    }

    private Notificacao paraNotificacao(Documento d) {
        return new Notificacao(d.texto("id"), d.texto("clienteId"), d.enumeracao("canal", CanalNotificacao.class),
                d.texto("destino"), d.texto("mensagem"), d.dataHora("dataHora"));
    }
}
