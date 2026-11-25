package br.umc.oficina.dominio.comum;

/** Conflito com o estado atual do recurso (HTTP 409). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
