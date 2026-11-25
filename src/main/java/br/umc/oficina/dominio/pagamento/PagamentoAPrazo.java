package br.umc.oficina.dominio.pagamento;

import br.umc.oficina.dominio.comum.Dinheiro;

/** A prazo (cartão parcelado/boleto): acréscimo de 8% de juros. */
public class PagamentoAPrazo implements FormaPagamento {

    private static final int JUROS = 8;

    @Override
    public Dinheiro calcularValorFinal(Dinheiro valor) {
        return valor.acrescidoDe(JUROS);
    }

    @Override
    public String descricao() {
        return "A prazo (+8% de juros)";
    }
}
