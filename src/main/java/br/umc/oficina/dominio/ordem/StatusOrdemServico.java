package br.umc.oficina.dominio.ordem;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ciclo de vida da ordem de serviço:
 * ABERTA -> EM_EXECUCAO -> CONCLUIDA -> ENTREGUE  (ABERTA/EM_EXECUCAO podem ser CANCELADA).
 */
public enum StatusOrdemServico {

    ABERTA("recebemos o seu veículo e a ordem de serviço foi aberta"),
    EM_EXECUCAO("o serviço no seu veículo foi iniciado"),
    CONCLUIDA("o serviço foi concluído e o veículo já pode ser retirado"),
    ENTREGUE("o veículo foi entregue. Obrigado pela preferência"),
    CANCELADA("a ordem de serviço foi cancelada");

    private final String mensagemAoCliente;

    StatusOrdemServico(String mensagemAoCliente) {
        this.mensagemAoCliente = mensagemAoCliente;
    }

    public Set<StatusOrdemServico> proximosPermitidos() {
        return switch (this) {
            case ABERTA -> EnumSet.of(EM_EXECUCAO, CANCELADA);
            case EM_EXECUCAO -> EnumSet.of(CONCLUIDA, CANCELADA);
            case CONCLUIDA -> EnumSet.of(ENTREGUE);
            case ENTREGUE, CANCELADA -> EnumSet.noneOf(StatusOrdemServico.class);
        };
    }

    public boolean podeMudarPara(StatusOrdemServico novo) {
        return proximosPermitidos().contains(novo);
    }

    public boolean permiteExclusao() {
        return this == ABERTA || this == CANCELADA;
    }

    public String mensagemAoCliente() {
        return mensagemAoCliente;
    }
}
