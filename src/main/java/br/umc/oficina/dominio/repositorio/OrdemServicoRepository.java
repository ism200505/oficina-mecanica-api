package br.umc.oficina.dominio.repositorio;

import br.umc.oficina.dominio.ordem.OrdemServico;
import br.umc.oficina.dominio.ordem.StatusOrdemServico;

import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository {

    OrdemServico salvar(OrdemServico ordem);

    Optional<OrdemServico> buscarPorId(String id);

    /** Lado "N" do relacionamento 1:N Veículo -> Ordens de Serviço. */
    List<OrdemServico> listarPorVeiculo(String veiculoId);

    List<OrdemServico> listarPorStatus(StatusOrdemServico status);

    List<OrdemServico> listarTodas();

    void remover(String id);
}
