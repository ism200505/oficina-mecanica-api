package br.umc.oficina.aplicacao.observador;

import br.umc.oficina.dominio.ordem.OrdemServico;
import br.umc.oficina.dominio.repositorio.VeiculoRepository;
import org.springframework.stereotype.Component;

/**
 * AUTOMAÇÃO: quando uma revisão preventiva é concluída, atualiza sozinho a data e a
 * quilometragem da última revisão do veículo (base para os lembretes automáticos).
 */
@Component
public class RegistrarRevisaoOuvinte implements OuvinteDeStatus {

    private final VeiculoRepository veiculos;

    public RegistrarRevisaoOuvinte(VeiculoRepository veiculos) {
        this.veiculos = veiculos;
    }

    @Override
    public void statusAlterado(OrdemServico ordem) {
        if (!ordem.concluiuRevisaoPreventiva()) {
            return;
        }
        veiculos.buscarPorId(ordem.veiculoId()).ifPresent(veiculo -> {
            veiculo.registrarRevisao(ordem.dataConclusao().toLocalDate(), ordem.quilometragemEntrada());
            veiculos.salvar(veiculo);
        });
    }
}
