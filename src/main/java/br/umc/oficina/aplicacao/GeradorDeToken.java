package br.umc.oficina.aplicacao;

import br.umc.oficina.dominio.usuario.Usuario;

/** Abstração para emissão de tokens (DIP): a autenticação não conhece a biblioteca JWT. */
public interface GeradorDeToken {

    String gerar(Usuario usuario);

    long validadeEmMinutos();
}
