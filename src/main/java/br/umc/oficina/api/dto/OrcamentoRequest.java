package br.umc.oficina.api.dto;

import br.umc.oficina.dominio.pagamento.TipoPagamento;
import br.umc.oficina.dominio.servico.Adicional;
import br.umc.oficina.dominio.servico.TipoServicoBase;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrcamentoRequest(
        @NotNull TipoServicoBase servicoBase,
        List<Adicional> adicionais,
        @NotNull TipoPagamento formaPagamento) {
}
