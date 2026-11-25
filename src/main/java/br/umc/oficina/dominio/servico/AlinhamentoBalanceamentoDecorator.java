package br.umc.oficina.dominio.servico;

import br.umc.oficina.dominio.comum.Dinheiro;

/** DECORATOR - papel "ConcreteDecorator": adiciona alinhamento e balanceamento. */
public class AlinhamentoBalanceamentoDecorator extends ServicoDecorator {

    public AlinhamentoBalanceamentoDecorator(Servico servico) {
        super(servico);
    }

    @Override
    public String descricao() {
        return servico.descricao() + " + Alinhamento e balanceamento";
    }

    @Override
    public Dinheiro preco() {
        return servico.preco().mais(Dinheiro.de("120.00"));
    }

    @Override
    public int prazoEmHoras() {
        return servico.prazoEmHoras() + 1;
    }
}
