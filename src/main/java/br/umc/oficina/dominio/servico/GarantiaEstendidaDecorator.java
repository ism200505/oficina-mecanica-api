package br.umc.oficina.dominio.servico;

import br.umc.oficina.dominio.comum.Dinheiro;

/**
 * DECORATOR - papel "ConcreteDecorator": garantia estendida de 6 meses,
 * custa 10% sobre tudo o que já foi acumulado na cadeia de decoradores.
 */
public class GarantiaEstendidaDecorator extends ServicoDecorator {

    private static final int PERCENTUAL = 10;

    public GarantiaEstendidaDecorator(Servico servico) {
        super(servico);
    }

    @Override
    public String descricao() {
        return servico.descricao() + " + Garantia estendida (6 meses)";
    }

    @Override
    public Dinheiro preco() {
        return servico.preco().acrescidoDe(PERCENTUAL);
    }

    @Override
    public int prazoEmHoras() {
        return servico.prazoEmHoras();
    }
}
