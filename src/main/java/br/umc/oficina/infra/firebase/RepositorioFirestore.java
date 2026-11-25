package br.umc.oficina.infra.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/** Operações comuns às coleções do Firestore (reuso por herança, sem duplicação). */
public abstract class RepositorioFirestore {

    private final Firestore firestore;
    private final String nomeDaColecao;

    protected RepositorioFirestore(Firestore firestore, String nomeDaColecao) {
        this.firestore = firestore;
        this.nomeDaColecao = nomeDaColecao;
    }

    protected void gravar(String id, Documento documento) {
        aguardar(colecao().document(id).set(documento.comoMapa()));
    }

    protected Optional<Documento> ler(String id) {
        DocumentSnapshot snapshot = aguardar(colecao().document(id).get());
        return snapshot.exists() ? Optional.of(Documento.de(snapshot.getData())) : Optional.empty();
    }

    protected List<Documento> lerTodos() {
        return converter(aguardar(colecao().get()));
    }

    protected List<Documento> lerOnde(String campo, Object valor) {
        return converter(aguardar(colecao().whereEqualTo(campo, valor).get()));
    }

    protected void apagar(String id) {
        aguardar(colecao().document(id).delete());
    }

    private CollectionReference colecao() {
        return firestore.collection(nomeDaColecao);
    }

    private static List<Documento> converter(QuerySnapshot resultado) {
        return resultado.getDocuments().stream()
                .map(documento -> Documento.de(documento.getData()))
                .toList();
    }

    private static <T> T aguardar(ApiFuture<T> futuro) {
        try {
            return futuro.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FalhaDePersistenciaException(e);
        } catch (ExecutionException e) {
            throw new FalhaDePersistenciaException(e);
        }
    }
}
