package br.umc.oficina.api.dto;

import br.umc.oficina.dominio.pagamento.TipoPagamento;
import br.umc.oficina.dominio.servico.Adicional;
import br.umc.oficina.dominio.servico.TipoServicoBase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrdemServicoRequest(
        @NotBlank String veiculoId,
        @NotBlank @Size(max = 1000) String descricaoProblema,
        @NotBlank String mecanicoResponsavel,
        @NotNull @PositiveOrZero Long quilometragemEntrada,
        @NotNull TipoServicoBase servicoBase,
        List<Adicional> adicionais,
        @NotNull TipoPagamento formaPagamento) {
}
