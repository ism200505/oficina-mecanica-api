package br.umc.oficina.api.dto;

import br.umc.oficina.dominio.ordem.OrdemServico;
import br.umc.oficina.dominio.ordem.StatusOrdemServico;
import br.umc.oficina.dominio.servico.TipoServicoBase;

import java.time.LocalDateTime;
import java.util.List;

public record OrdemServicoResponse(String id, String numero, String clienteId, String veiculoId,
                                   String descricaoProblema, String mecanicoResponsavel,
                                   long quilometragemEntrada, TipoServicoBase servicoBase,
                                   List<String> adicionais, OrcamentoResponse orcamento,
                                   StatusOrdemServico status, LocalDateTime dataAbertura,
                                   LocalDateTime previsaoEntrega, LocalDateTime dataConclusao) {

    public static OrdemServicoResponse de(OrdemServico o) {
        return new OrdemServicoResponse(o.id(), o.numero(), o.clienteId(), o.veiculoId(),
                o.descricaoProblema(), o.mecanicoResponsavel(), o.quilometragemEntrada().km(),
                o.servicoBase(), o.adicionais().nomes(), OrcamentoResponse.de(o.orcamento()),
                o.status(), o.dataAbertura(), o.previsaoEntrega(), o.dataConclusao());
    }
}
