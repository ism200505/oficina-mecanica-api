package br.umc.oficina.dominio.servico;

import br.umc.oficina.dominio.comum.Dinheiro;

/** DECORATOR - papel "ConcreteComponent". */
public class DiagnosticoEletronico implements Servico {

    @Override
    public String descricao() {
        return "Diagnóstico eletrônico (scanner)";
    }

    @Override
    public Dinheiro preco() {
        return Dinheiro.de("150.00");
    }

    @Override
    public int prazoEmHoras() {
        return 1;
    }
}
