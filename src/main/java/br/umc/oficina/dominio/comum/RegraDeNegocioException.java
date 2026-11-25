package br.umc.oficina.dominio.comum;

/** Violação de uma regra de negócio (HTTP 422). */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
