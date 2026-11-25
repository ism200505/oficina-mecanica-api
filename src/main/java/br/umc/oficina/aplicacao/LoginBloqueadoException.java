package br.umc.oficina.aplicacao;

/** Muitas tentativas de login com falha (HTTP 429). */
public class LoginBloqueadoException extends RuntimeException {

    public LoginBloqueadoException() {
        super("Muitas tentativas de login inválidas. Tente novamente em alguns minutos.");
    }
}
