package br.umc.oficina.api.dto;

import br.umc.oficina.dominio.pagamento.TipoPagamento;
import br.umc.oficina.dominio.servico.Adicional;
import br.umc.oficina.dominio.servico.Servico;
import br.umc.oficina.dominio.servico.TipoServicoBase;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/** Catálogo de serviços base, adicionais (decoradores) e formas de pagamento. */
public record CatalogoResponse(List<ItemServicoBase> servicosBase, List<ItemSimples> adicionais,
                               List<ItemSimples> formasPagamento) {

    public record ItemServicoBase(String codigo, String descricao, BigDecimal preco, int prazoEmHoras) {
    }

    public record ItemSimples(String codigo, String descricao) {
    }

    public static CatalogoResponse completo() {
        List<ItemServicoBase> bases = Arrays.stream(TipoServicoBase.values())
                .map(CatalogoResponse::itemBase)
                .toList();
        List<ItemSimples> adicionais = Arrays.stream(Adicional.values())
                .map(a -> new ItemSimples(a.name(), a.descricao()))
                .toList();
        List<ItemSimples> pagamentos = Arrays.stream(TipoPagamento.values())
                .map(p -> new ItemSimples(p.name(), p.forma().descricao()))
                .toList();
        return new CatalogoResponse(bases, adicionais, pagamentos);
    }

    private static ItemServicoBase itemBase(TipoServicoBase tipo) {
        Servico servico = tipo.criar();
        return new ItemServicoBase(tipo.name(), servico.descricao(), servico.preco().valor(), servico.prazoEmHoras());
    }
}
