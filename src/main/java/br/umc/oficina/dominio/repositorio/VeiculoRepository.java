package br.umc.oficina.dominio.repositorio;

import br.umc.oficina.dominio.comum.Placa;
import br.umc.oficina.dominio.veiculo.Veiculo;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository {

    Veiculo salvar(Veiculo veiculo);

    Optional<Veiculo> buscarPorId(String id);

    Optional<Veiculo> buscarPorPlaca(Placa placa);

    /** Lado "N" do relacionamento 1:N Cliente -> Veículos. */
    List<Veiculo> listarPorCliente(String clienteId);

    List<Veiculo> listarTodos();

    void remover(String id);
}
