package br.umc.oficina.dominio.servico;

import br.umc.oficina.dominio.comum.Dinheiro;

/** DECORATOR - papel "ConcreteDecorator": adiciona higienização do ar-condicionado. */
public class HigienizacaoArCondicionadoDecorator extends ServicoDecorator {

    public HigienizacaoArCondicionadoDecorator(Servico servico) {
        super(servico);
    }

    @Override
    public String descricao() {
        return servico.descricao() + " + Higienização do ar-condicionado";
    }

    @Override
    public Dinheiro preco() {
        return servico.preco().mais(Dinheiro.de("140.00"));
    }

    @Override
    public int prazoEmHoras() {
        return servico.prazoEmHoras() + 1;
    }
}
