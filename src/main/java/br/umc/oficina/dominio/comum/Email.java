package br.umc.oficina.dominio.comum;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Objeto de valor e-mail. */
public record Email(String endereco) {

    private static final Pattern FORMATO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    public Email {
        Objects.requireNonNull(endereco, "E-mail é obrigatório");
        endereco = endereco.trim().toLowerCase(Locale.ROOT);
        if (!FORMATO.matcher(endereco).matches()) {
            throw new RegraDeNegocioException("E-mail inválido");
        }
    }
}
