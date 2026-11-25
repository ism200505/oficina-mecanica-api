package br.umc.oficina.aplicacao;

/**
 * Login ou senha inválidos (HTTP 401). A mensagem é sempre a mesma para não revelar
 * se o login existe (mitigação de enumeração de usuários).
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("Login ou senha inválidos");
    }
}
