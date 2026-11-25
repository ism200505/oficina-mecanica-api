package br.umc.oficina.dominio.servico;

import br.umc.oficina.dominio.comum.Dinheiro;

/** DECORATOR - papel "ConcreteComponent". */
public class ManutencaoCorretiva implements Servico {

    @Override
    public String descricao() {
        return "Manutenção corretiva (mão de obra)";
    }

    @Override
    public Dinheiro preco() {
        return Dinheiro.de("300.00");
    }

    @Override
    public int prazoEmHoras() {
        return 4;
    }
}
