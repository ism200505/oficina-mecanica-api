package br.umc.oficina.aplicacao;

import br.umc.oficina.api.dto.VeiculoRequest;
import br.umc.oficina.dominio.comum.ConflitoException;
import br.umc.oficina.dominio.comum.Placa;
import br.umc.oficina.dominio.comum.RecursoNaoEncontradoException;
import br.umc.oficina.dominio.comum.RegraDeNegocioException;
import br.umc.oficina.dominio.ordem.OrdemServico;
import br.umc.oficina.dominio.repositorio.ClienteRepository;
import br.umc.oficina.dominio.repositorio.OrdemServicoRepository;
import br.umc.oficina.dominio.repositorio.VeiculoRepository;
import br.umc.oficina.dominio.veiculo.Veiculo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculos;
    private final ClienteRepository clientes;
    private final OrdemServicoRepository ordens;

    public VeiculoService(VeiculoRepository veiculos, ClienteRepository clientes, OrdemServicoRepository ordens) {
        this.veiculos = veiculos;
        this.clientes = clientes;
        this.ordens = ordens;
    }

    public Veiculo cadastrar(VeiculoRequest dados) {
        garantirClienteExistente(dados.clienteId());
        garantirPlacaDisponivel(dados.placaValidada(), null);
        Veiculo novo = dados.paraVeiculo(UUID.randomUUID().toString(), null);
        return veiculos.salvar(novo);
    }

    public Veiculo buscarPorId(String id) {
        return veiculos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo", id));
    }

    public List<Veiculo> listar() {
        return veiculos.listarTodos();
    }

    public Veiculo atualizar(String id, VeiculoRequest dados) {
        Veiculo atual = buscarPorId(id);
        garantirClienteExistente(dados.clienteId());
        garantirPlacaDisponivel(dados.placaValidada(), id);
        Veiculo atualizado = dados.paraVeiculo(id, atual.dataUltimoLembrete());
        garantirQueOdometroNaoVoltou(atual, atualizado);
        return veiculos.salvar(atualizado);
    }

    public void remover(String id) {
        buscarPorId(id);
        if (!ordens.listarPorVeiculo(id).isEmpty()) {
            throw new ConflitoException("Veículo possui ordens de serviço e não pode ser removido");
        }
        veiculos.remover(id);
    }

    /** Relacionamento 1:N - histórico de ordens de serviço do veículo. */
    public List<OrdemServico> ordensDoVeiculo(String id) {
        buscarPorId(id);
        return ordens.listarPorVeiculo(id);
    }

    private void garantirClienteExistente(String clienteId) {
        clientes.buscarPorId(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", clienteId));
    }

    private void garantirPlacaDisponivel(Placa placa, String idPermitido) {
        veiculos.buscarPorPlaca(placa)
                .filter(existente -> !existente.id().equals(idPermitido))
                .ifPresent(existente -> {
                    throw new ConflitoException("Já existe veículo com a placa " + placa.valor());
                });
    }

    private void garantirQueOdometroNaoVoltou(Veiculo atual, Veiculo atualizado) {
        if (atualizado.quilometragemAtual().menorQue(atual.quilometragemAtual())) {
            throw new RegraDeNegocioException("A quilometragem não pode ser menor que a atual ("
                    + atual.quilometragemAtual().km() + " km)");
        }
    }
}
