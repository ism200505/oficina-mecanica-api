package br.umc.oficina.infra.memoria;

import br.umc.oficina.dominio.comum.Placa;
import br.umc.oficina.dominio.repositorio.VeiculoRepository;
import br.umc.oficina.dominio.veiculo.Veiculo;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("memoria")
public class MemoriaVeiculoRepository implements VeiculoRepository {

    private final Map<String, Veiculo> dados = new ConcurrentHashMap<>();

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        dados.put(veiculo.id(), veiculo);
        return veiculo;
    }

    @Override
    public Optional<Veiculo> buscarPorId(String id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public Optional<Veiculo> buscarPorPlaca(Placa placa) {
        return dados.values().stream().filter(v -> v.placa().equals(placa)).findFirst();
    }

    @Override
    public List<Veiculo> listarPorCliente(String clienteId) {
        return dados.values().stream().filter(v -> v.pertenceAo(clienteId)).toList();
    }

    @Override
    public List<Veiculo> listarTodos() {
        return List.copyOf(dados.values());
    }

    @Override
    public void remover(String id) {
        dados.remove(id);
    }
}
