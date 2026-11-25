package br.umc.oficina.infra.memoria;

import br.umc.oficina.dominio.ordem.OrdemServico;
import br.umc.oficina.dominio.ordem.StatusOrdemServico;
import br.umc.oficina.dominio.repositorio.OrdemServicoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("memoria")
public class MemoriaOrdemServicoRepository implements OrdemServicoRepository {

    private final Map<String, OrdemServico> dados = new ConcurrentHashMap<>();

    @Override
    public OrdemServico salvar(OrdemServico ordem) {
        dados.put(ordem.id(), ordem);
        return ordem;
    }

    @Override
    public Optional<OrdemServico> buscarPorId(String id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public List<OrdemServico> listarPorVeiculo(String veiculoId) {
        return dados.values().stream().filter(o -> o.veiculoId().equals(veiculoId)).toList();
    }

    @Override
    public List<OrdemServico> listarPorStatus(StatusOrdemServico status) {
        return dados.values().stream().filter(o -> o.status() == status).toList();
    }

    @Override
    public List<OrdemServico> listarTodas() {
        return List.copyOf(dados.values());
    }

    @Override
    public void remover(String id) {
        dados.remove(id);
    }
}
