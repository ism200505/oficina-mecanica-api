package br.umc.oficina.aplicacao;

import br.umc.oficina.api.dto.ClienteRequest;
import br.umc.oficina.dominio.cliente.Cliente;
import br.umc.oficina.dominio.comum.ConflitoException;
import br.umc.oficina.dominio.comum.Cpf;
import br.umc.oficina.dominio.comum.RecursoNaoEncontradoException;
import br.umc.oficina.dominio.repositorio.ClienteRepository;
import br.umc.oficina.dominio.repositorio.VeiculoRepository;
import br.umc.oficina.dominio.veiculo.Veiculo;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Regras de negócio de clientes. Depende apenas de abstrações (DIP):
 * repositórios (interfaces) e Clock, todos injetados pelo construtor.
 */
@Service
public class ClienteService {

    private final ClienteRepository clientes;
    private final VeiculoRepository veiculos;
    private final Clock relogio;

    public ClienteService(ClienteRepository clientes, VeiculoRepository veiculos, Clock relogio) {
        this.clientes = clientes;
        this.veiculos = veiculos;
        this.relogio = relogio;
    }

    public Cliente cadastrar(ClienteRequest dados) {
        garantirCpfDisponivel(dados.cpfValidado(), null);
        Cliente novo = dados.paraCliente(UUID.randomUUID().toString(), LocalDate.now(relogio), true);
        return clientes.salvar(novo);
    }

    public Cliente buscarPorId(String id) {
        return clientes.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    public List<Cliente> listar() {
        return clientes.listarTodos();
    }

    public Cliente atualizar(String id, ClienteRequest dados) {
        Cliente atual = buscarPorId(id);
        garantirCpfDisponivel(dados.cpfValidado(), id);
        Cliente atualizado = dados.paraCliente(id, atual.dataCadastro(), atual.ativo());
        return clientes.salvar(atualizado);
    }

    public void remover(String id) {
        buscarPorId(id);
        if (!veiculos.listarPorCliente(id).isEmpty()) {
            throw new ConflitoException("Cliente possui veículos cadastrados; remova-os antes");
        }
        clientes.remover(id);
    }

    /** Relacionamento 1:N - veículos de um cliente. */
    public List<Veiculo> veiculosDoCliente(String id) {
        buscarPorId(id);
        return veiculos.listarPorCliente(id);
    }

    private void garantirCpfDisponivel(Cpf cpf, String idPermitido) {
        clientes.buscarPorCpf(cpf)
                .filter(existente -> !existente.id().equals(idPermitido))
                .ifPresent(existente -> {
                    throw new ConflitoException("Já existe cliente com o CPF " + cpf.formatado());
                });
    }
}
