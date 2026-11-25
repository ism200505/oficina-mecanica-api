package br.umc.oficina.dominio.servico;

import br.umc.oficina.dominio.comum.Dinheiro;

/** DECORATOR - papel "ConcreteDecorator": adiciona lavagem completa. */
public class LavagemCompletaDecorator extends ServicoDecorator {

    public LavagemCompletaDecorator(Servico servico) {
        super(servico);
    }

    @Override
    public String descricao() {
        return servico.descricao() + " + Lavagem completa";
    }

    @Override
    public Dinheiro preco() {
        return servico.preco().mais(Dinheiro.de("70.00"));
    }

    @Override
    public int prazoEmHoras() {
        return servico.prazoEmHoras() + 1;
    }
}
