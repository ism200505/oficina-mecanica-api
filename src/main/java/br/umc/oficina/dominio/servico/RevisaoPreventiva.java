package br.umc.oficina.dominio.servico;

import br.umc.oficina.dominio.comum.Dinheiro;

/** DECORATOR - papel "ConcreteComponent". */
public class RevisaoPreventiva implements Servico {

    @Override
    public String descricao() {
        return "Revisão preventiva";
    }

    @Override
    public Dinheiro preco() {
        return Dinheiro.de("250.00");
    }

    @Override
    public int prazoEmHoras() {
        return 3;
    }
}
