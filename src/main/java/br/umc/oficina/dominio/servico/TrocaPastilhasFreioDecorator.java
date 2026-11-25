package br.umc.oficina.dominio.servico;

import br.umc.oficina.dominio.comum.Dinheiro;

/** DECORATOR - papel "ConcreteDecorator": adiciona troca das pastilhas de freio. */
public class TrocaPastilhasFreioDecorator extends ServicoDecorator {

    public TrocaPastilhasFreioDecorator(Servico servico) {
        super(servico);
    }

    @Override
    public String descricao() {
        return servico.descricao() + " + Troca de pastilhas de freio";
    }

    @Override
    public Dinheiro preco() {
        return servico.preco().mais(Dinheiro.de("220.00"));
    }

    @Override
    public int prazoEmHoras() {
        return servico.prazoEmHoras() + 2;
    }
}
