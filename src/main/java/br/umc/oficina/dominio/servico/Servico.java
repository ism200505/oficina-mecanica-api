package br.umc.oficina.dominio.servico;

import br.umc.oficina.dominio.comum.Dinheiro;

/**
 * DECORATOR - papel "Component".
 * Interface comum para os serviços da oficina e para os adicionais que os decoram.
 */
public interface Servico {

    String descricao();

    Dinheiro preco();

    int prazoEmHoras();
}
