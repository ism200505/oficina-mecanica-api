package br.umc.oficina.dominio.pagamento;

import br.umc.oficina.dominio.comum.Dinheiro;

/** PIX: 5% de desconto em qualquer valor. */
public class PagamentoPix implements FormaPagamento {

    private static final int DESCONTO = 5;

    @Override
    public Dinheiro calcularValorFinal(Dinheiro valor) {
        return valor.descontadoDe(DESCONTO);
    }

    @Override
    public String descricao() {
        return "PIX (5% de desconto)";
    }
}
