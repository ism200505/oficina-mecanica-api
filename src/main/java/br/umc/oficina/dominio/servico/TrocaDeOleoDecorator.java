package br.umc.oficina.dominio.servico;

import br.umc.oficina.dominio.comum.Dinheiro;

/** DECORATOR - papel "ConcreteDecorator": adiciona troca de óleo e filtro. */
public class TrocaDeOleoDecorator extends ServicoDecorator {

    public TrocaDeOleoDecorator(Servico servico) {
        super(servico);
    }

    @Override
    public String descricao() {
        return servico.descricao() + " + Troca de óleo e filtro";
    }

    @Override
    public Dinheiro preco() {
        return servico.preco().mais(Dinheiro.de("180.00"));
    }

    @Override
    public int prazoEmHoras() {
        return servico.prazoEmHoras() + 1;
    }
}
