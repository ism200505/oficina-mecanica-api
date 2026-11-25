package br.umc.oficina.infra.firebase;

/** Erro de comunicação com o Firestore (HTTP 503). */
public class FalhaDePersistenciaException extends RuntimeException {

    public FalhaDePersistenciaException(Throwable causa) {
        super("Falha ao acessar o banco de dados", causa);
    }
}
