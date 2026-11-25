package br.umc.oficina.dominio.repositorio;

import br.umc.oficina.dominio.cliente.Cliente;
import br.umc.oficina.dominio.comum.Cpf;

import java.util.List;
import java.util.Optional;

/** Abstração de persistência (DIP): os serviços não conhecem o Firebase. */
public interface ClienteRepository {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(String id);

    Optional<Cliente> buscarPorCpf(Cpf cpf);

    List<Cliente> listarTodos();

    void remover(String id);
}
