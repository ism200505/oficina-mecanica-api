package br.umc.oficina.api.dto;

import br.umc.oficina.dominio.ordem.Orcamento;
import br.umc.oficina.dominio.pagamento.TipoPagamento;

import java.math.BigDecimal;

public record OrcamentoResponse(String descricao, BigDecimal valorServicos, BigDecimal valorFinal,
                                int prazoEmHoras, TipoPagamento formaPagamento) {

    public static OrcamentoResponse de(Orcamento o) {
        return new OrcamentoResponse(o.descricao(), o.valorServicos().valor(), o.valorFinal().valor(),
                o.prazoEmHoras(), o.formaPagamento());
    }
}
