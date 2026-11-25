package br.umc.oficina.dominio.servico;

import java.util.function.Supplier;

/**
 * Fábrica (Factory) dos serviços base (ConcreteComponents) disponíveis na oficina.
 * Para incluir um novo serviço basta adicionar uma constante (OCP) - sem if/else nem switch.
 */
public enum TipoServicoBase {

    REVISAO_PREVENTIVA(RevisaoPreventiva::new),
    DIAGNOSTICO_ELETRONICO(DiagnosticoEletronico::new),
    MANUTENCAO_CORRETIVA(ManutencaoCorretiva::new);

    private final Supplier<Servico> fabrica;

    TipoServicoBase(Supplier<Servico> fabrica) {
        this.fabrica = fabrica;
    }

    public Servico criar() {
        return fabrica.get();
    }
}
