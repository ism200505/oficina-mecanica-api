package br.umc.oficina.aplicacao;

import br.umc.oficina.dominio.ordem.Orcamento;
import br.umc.oficina.dominio.pagamento.FormaPagamento;
import br.umc.oficina.dominio.pagamento.TipoPagamento;
import br.umc.oficina.dominio.servico.Adicionais;
import br.umc.oficina.dominio.servico.Servico;
import br.umc.oficina.dominio.servico.TipoServicoBase;
import org.springframework.stereotype.Service;

/**
 * AUTOMAÇÃO: monta o orçamento automaticamente.
 * 1) cria o serviço base (ConcreteComponent);
 * 2) "embrulha" com os adicionais escolhidos (Decorators);
 * 3) aplica a forma de pagamento (Strategy) sobre o total.
 */
@Service
public class OrcamentoService {

    public Orcamento calcular(TipoServicoBase servicoBase, Adicionais adicionais, TipoPagamento tipoPagamento) {
        Servico servico = adicionais.decorar(servicoBase.criar());
        FormaPagamento formaPagamento = tipoPagamento.forma();
        return new Orcamento(servico.descricao(), servico.preco(),
                formaPagamento.calcularValorFinal(servico.preco()), servico.prazoEmHoras(), tipoPagamento);
    }
}
