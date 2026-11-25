package br.umc.oficina.dominio.comum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Objeto de valor que encapsula valores monetários (Calistenia: "encapsule tipos primitivos").
 * Invariante: o valor nunca é nulo nem negativo e sempre possui 2 casas decimais.
 */
public record Dinheiro(BigDecimal valor) implements Comparable<Dinheiro> {

    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    public Dinheiro {
        Objects.requireNonNull(valor, "O valor monetário é obrigatório");
        if (valor.signum() < 0) {
            throw new RegraDeNegocioException("Valor monetário não pode ser negativo");
        }
        valor = valor.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Dinheiro de(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public static Dinheiro de(double valor) {
        return new Dinheiro(BigDecimal.valueOf(valor));
    }

    public Dinheiro mais(Dinheiro outro) {
        return new Dinheiro(valor.add(outro.valor));
    }

    /** Acrescenta um percentual (ex.: 8 = +8%). */
    public Dinheiro acrescidoDe(int percentual) {
        return new Dinheiro(valor.add(fracao(percentual)));
    }

    /** Desconta um percentual (ex.: 10 = -10%). */
    public Dinheiro descontadoDe(int percentual) {
        return new Dinheiro(valor.subtract(fracao(percentual)));
    }

    public boolean maiorQue(Dinheiro outro) {
        return compareTo(outro) > 0;
    }

    private BigDecimal fracao(int percentual) {
        return valor.multiply(BigDecimal.valueOf(percentual)).divide(CEM, 2, RoundingMode.HALF_EVEN);
    }

    @Override
    public int compareTo(Dinheiro outro) {
        return valor.compareTo(outro.valor);
    }

    @Override
    public String toString() {
        return "R$ " + valor.toPlainString().replace('.', ',');
    }
}
