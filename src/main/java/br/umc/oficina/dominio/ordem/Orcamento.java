package br.umc.oficina.dominio.ordem;

import br.umc.oficina.dominio.comum.Dinheiro;
import br.umc.oficina.dominio.pagamento.TipoPagamento;

import java.util.Objects;

/**
 * Orçamento da ordem de serviço. Relacionamento 1:1 (composição) com {@link OrdemServico}.
 * É um retrato do valor no momento da abertura: se a tabela de preços mudar, a OS não muda.
 */
public record Orcamento(String descricao, Dinheiro valorServicos, Dinheiro valorFinal,
                        int prazoEmHoras, TipoPagamento formaPagamento) {

    public Orcamento {
        Objects.requireNonNull(descricao, "descrição é obrigatória");
        Objects.requireNonNull(valorServicos, "valor dos serviços é obrigatório");
        Objects.requireNonNull(valorFinal, "valor final é obrigatório");
        Objects.requireNonNull(formaPagamento, "forma de pagamento é obrigatória");
    }
}
