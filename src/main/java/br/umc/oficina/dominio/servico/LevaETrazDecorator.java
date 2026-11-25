package br.umc.oficina.dominio.servico;

import br.umc.oficina.dominio.comum.Dinheiro;

/** DECORATOR - papel "ConcreteDecorator": busca e entrega o veículo na casa do cliente. */
public class LevaETrazDecorator extends ServicoDecorator {

    public LevaETrazDecorator(Servico servico) {
        super(servico);
    }

    @Override
    public String descricao() {
        return servico.descricao() + " + Leva e traz";
    }

    @Override
    public Dinheiro preco() {
        return servico.preco().mais(Dinheiro.de("90.00"));
    }

    @Override
    public int prazoEmHoras() {
        return servico.prazoEmHoras() + 2;
    }
}
