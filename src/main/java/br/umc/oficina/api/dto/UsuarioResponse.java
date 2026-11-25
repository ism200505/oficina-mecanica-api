package br.umc.oficina.api.dto;

import br.umc.oficina.dominio.usuario.Papel;
import br.umc.oficina.dominio.usuario.Usuario;

/** Nunca devolve o hash da senha. */
public record UsuarioResponse(String id, String nome, String login, Papel papel, boolean ativo) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.id(), u.nome(), u.login(), u.papel(), u.ativo());
    }
}
