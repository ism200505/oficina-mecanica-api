package br.umc.oficina.infra.memoria;

import br.umc.oficina.dominio.cliente.Cliente;
import br.umc.oficina.dominio.comum.Cpf;
import br.umc.oficina.dominio.repositorio.ClienteRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação alternativa em memória (perfil "memoria"), útil para demonstração sem internet.
 * Prova do DIP: os serviços funcionam igual com Firestore ou memória, sem nenhuma alteração.
 */
@Repository
@Profile("memoria")
public class MemoriaClienteRepository implements ClienteRepository {

    private final Map<String, Cliente> dados = new ConcurrentHashMap<>();

    @Override
    public Cliente salvar(Cliente cliente) {
        dados.put(cliente.id(), cliente);
        return cliente;
    }

    @Override
    public Optional<Cliente> buscarPorId(String id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public Optional<Cliente> buscarPorCpf(Cpf cpf) {
        return dados.values().stream().filter(c -> c.possuiCpf(cpf)).findFirst();
    }

    @Override
    public List<Cliente> listarTodos() {
        return List.copyOf(dados.values());
    }

    @Override
    public void remover(String id) {
        dados.remove(id);
    }
}
