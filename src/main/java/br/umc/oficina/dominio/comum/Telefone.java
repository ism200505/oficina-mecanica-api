package br.umc.oficina.dominio.comum;

import java.util.Objects;

/** Objeto de valor telefone (DDD + número, 10 ou 11 dígitos). */
public record Telefone(String numero) {

    public Telefone {
        Objects.requireNonNull(numero, "Telefone é obrigatório");
        numero = numero.replaceAll("\\D", "");
        if (numero.length() < 10 || numero.length() > 11) {
            throw new RegraDeNegocioException("Telefone deve ter DDD e 8 ou 9 dígitos");
        }
    }
}
