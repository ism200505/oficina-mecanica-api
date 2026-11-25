package br.umc.oficina.dominio.servico;

import java.util.function.UnaryOperator;

/**
 * Adicionais que o cliente pode escolher. Cada constante sabe qual
 * ConcreteDecorator deve "embrulhar" o serviço (fábrica de decoradores).
 */
public enum Adicional {

    TROCA_DE_OLEO("Troca de óleo e filtro", TrocaDeOleoDecorator::new),
    ALINHAMENTO_BALANCEAMENTO("Alinhamento e balanceamento", AlinhamentoBalanceamentoDecorator::new),
    TROCA_PASTILHAS_FREIO("Troca de pastilhas de freio", TrocaPastilhasFreioDecorator::new),
    HIGIENIZACAO_AR_CONDICIONADO("Higienização do ar-condicionado", HigienizacaoArCondicionadoDecorator::new),
    LAVAGEM_COMPLETA("Lavagem completa", LavagemCompletaDecorator::new),
    LEVA_E_TRAZ("Leva e traz", LevaETrazDecorator::new),
    GARANTIA_ESTENDIDA("Garantia estendida de 6 meses (+10%)", GarantiaEstendidaDecorator::new);

    private final String descricao;
    private final UnaryOperator<Servico> decorador;

    Adicional(String descricao, UnaryOperator<Servico> decorador) {
        this.descricao = descricao;
        this.decorador = decorador;
    }

    public Servico decorar(Servico servico) {
        return decorador.apply(servico);
    }

    public String descricao() {
        return descricao;
    }
}
