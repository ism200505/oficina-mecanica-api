package br.umc.oficina.dominio.pagamento;

/** Fábrica das estratégias de pagamento a partir do valor recebido na API. */
public enum TipoPagamento {

    A_VISTA(new PagamentoAVista()),
    A_PRAZO(new PagamentoAPrazo()),
    PIX(new PagamentoPix());

    private final FormaPagamento forma;

    TipoPagamento(FormaPagamento forma) {
        this.forma = forma;
    }

    public FormaPagamento forma() {
        return forma;
    }
}
