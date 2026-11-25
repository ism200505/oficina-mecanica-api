package br.umc.oficina.dominio.comum;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Objeto de valor placa (padrão antigo ABC1234 ou Mercosul ABC1D23). */
public record Placa(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$");

    public Placa {
        Objects.requireNonNull(valor, "Placa é obrigatória");
        valor = valor.replace("-", "").trim().toUpperCase(Locale.ROOT);
        if (!FORMATO.matcher(valor).matches()) {
            throw new RegraDeNegocioException("Placa inválida");
        }
    }
}
