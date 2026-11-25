package br.umc.oficina.dominio.usuario;

import java.util.Objects;

/** Usuário do sistema. A senha é guardada somente como hash BCrypt (nunca em texto puro). */
public record Usuario(String id, String nome, String login, String senhaHash, Papel papel, boolean ativo) {

    public Usuario {
        Objects.requireNonNull(id, "id é obrigatório");
        Objects.requireNonNull(nome, "nome é obrigatório");
        Objects.requireNonNull(login, "login é obrigatório");
        Objects.requireNonNull(senhaHash, "senha é obrigatória");
        Objects.requireNonNull(papel, "papel é obrigatório");
        login = login.trim().toLowerCase();
    }
}
