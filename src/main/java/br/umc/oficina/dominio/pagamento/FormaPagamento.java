package br.umc.oficina.dominio.pagamento;

import br.umc.oficina.dominio.comum.Dinheiro;

/**
 * Abstração das formas de pagamento (DIP + Strategy).
 *
 * Contrato (LSP) que TODAS as implementações respeitam:
 * - Pré-condição: recebe qualquer {@link Dinheiro} não nulo (nenhuma implementação exige mais que isso).
 * - Pós-condição: devolve sempre um {@link Dinheiro} válido (não nulo e não negativo), nunca lança exceção.
 */
public interface FormaPagamento {

    Dinheiro calcularValorFinal(Dinheiro valorDosServicos);

    String descricao();
}
