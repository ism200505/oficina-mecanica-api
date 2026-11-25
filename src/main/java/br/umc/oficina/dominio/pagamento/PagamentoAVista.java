package br.umc.oficina.dominio.pagamento;

import br.umc.oficina.dominio.comum.Dinheiro;

/**
 * À vista (dinheiro/débito): desconto proporcional ao valor.
 * Acima de R$ 500,00 = 15% | acima de R$ 300,00 = 10% | demais = sem desconto.
 */
public class PagamentoAVista implements FormaPagamento {

    private static final Dinheiro FAIXA_15 = Dinheiro.de("500.00");
    private static final Dinheiro FAIXA_10 = Dinheiro.de("300.00");

    @Override
    public Dinheiro calcularValorFinal(Dinheiro valor) {
        if (valor.maiorQue(FAIXA_15)) {
            return valor.descontadoDe(15);
        }
        if (valor.maiorQue(FAIXA_10)) {
            return valor.descontadoDe(10);
        }
        return valor;
    }

    @Override
    public String descricao() {
        return "À vista (10% acima de R$ 300 / 15% acima de R$ 500)";
    }
}
