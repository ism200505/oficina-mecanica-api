package br.umc.oficina.dominio.comum;

import java.util.Objects;

/** Objeto de valor CPF: guarda somente dígitos e valida os dígitos verificadores. */
public record Cpf(String numero) {

    private static final int TAMANHO = 11;

    public Cpf {
        Objects.requireNonNull(numero, "CPF é obrigatório");
        numero = numero.replaceAll("\\D", "");
        if (!valido(numero)) {
            throw new RegraDeNegocioException("CPF inválido");
        }
    }

    public String formatado() {
        return numero.substring(0, 3) + "." + numero.substring(3, 6) + "."
                + numero.substring(6, 9) + "-" + numero.substring(9);
    }

    private static boolean valido(String digitos) {
        if (digitos.length() != TAMANHO || digitos.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(digitos, 9) == valorDoDigito(digitos, 9)
                && digitoVerificador(digitos, 10) == valorDoDigito(digitos, 10);
    }

    private static int digitoVerificador(String digitos, int quantidade) {
        int soma = 0;
        for (int posicao = 0; posicao < quantidade; posicao++) {
            soma += valorDoDigito(digitos, posicao) * (quantidade + 1 - posicao);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private static int valorDoDigito(String digitos, int posicao) {
        return digitos.charAt(posicao) - '0';
    }
}
