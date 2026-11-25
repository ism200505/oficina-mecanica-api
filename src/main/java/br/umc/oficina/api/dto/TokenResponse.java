package br.umc.oficina.api.dto;

import br.umc.oficina.dominio.usuario.Papel;

public record TokenResponse(String token, String tipo, long expiraEmMinutos, String nome, Papel papel) {
}
