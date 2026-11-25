package br.umc.oficina.aplicacao;

import br.umc.oficina.Fixtures;
import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.comum.ConflitoException;
import br.umc.oficina.dominio.comum.Placa;
import br.umc.oficina.dominio.comum.RecursoNaoEncontradoException;
import br.umc.oficina.dominio.comum.RegraDeNegocioException;
import br.umc.oficina.dominio.ordem.StatusOrdemServico;
import br.umc.oficina.dominio.repositorio.ClienteRepository;
import br.umc.oficina.dominio.repositorio.OrdemServicoRepository;
import br.umc.oficina.dominio.repositorio.VeiculoRepository;
import br.umc.oficina.dominio.servico.TipoServicoBase;
import br.umc.oficina.dominio.veiculo.Veiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VeiculoServiceTest {

    private VeiculoRepository veiculos;
    private ClienteRepository clientes;
    private OrdemServicoRepository ordens;
    private VeiculoService servico;

    @BeforeEach
    void preparar() {
        veiculos = mock(VeiculoRepository.class);
        clientes = mock(ClienteRepository.class);
        ordens = mock(OrdemServicoRepository.class);
        servico = new VeiculoService(veiculos, clientes, ordens);
        when(veiculos.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @Test
    @DisplayName("Cadastrar: veículo vinculado a um cliente existente")
    void cadastrarComSucesso() {
        // Arrange
        when(clientes.buscarPorId("c1")).thenReturn(Optional.of(Fixtures.cliente("c1", CanalNotificacao.SMS)));
        when(veiculos.buscarPorPlaca(new Placa("BRA2E19"))).thenReturn(Optional.empty());

        // Act
        Veiculo criado = servico.cadastrar(Fixtures.veiculoRequest("c1", "bra-2e19", 30_000));

        // Assert
        assertThat(criado.clienteId()).isEqualTo("c1");
        assertThat(criado.placa().valor()).isEqualTo("BRA2E19");
        verify(veiculos).salvar(criado);
    }

    @Test
    @DisplayName("Cadastrar: cliente (proprietário) inexistente gera 404")
    void cadastrarSemCliente() {
        // Arrange
        when(clientes.buscarPorId("x")).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> servico.cadastrar(Fixtures.veiculoRequest("x", "BRA2E19", 30_000)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(veiculos, never()).salvar(any());
    }

    @Test
    @DisplayName("Cadastrar: placa já cadastrada gera conflito")
    void cadastrarPlacaDuplicada() {
        // Arrange
        when(clientes.buscarPorId("c1")).thenReturn(Optional.of(Fixtures.cliente("c1", CanalNotificacao.SMS)));
        when(veiculos.buscarPorPlaca(new Placa("BRA2E19")))
                .thenReturn(Optional.of(Fixtures.veiculo("outro", "c9", 1000, null, 0)));

        // Act + Assert
        assertThatThrownBy(() -> servico.cadastrar(Fixtures.veiculoRequest("c1", "BRA2E19", 30_000)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    @DisplayName("Buscar: id inexistente gera 404")
    void buscarInexistente() {
        when(veiculos.buscarPorId("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servico.buscarPorId("x")).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Atualizar: aceita quilometragem maior que a atual")
    void atualizarComSucesso() {
        // Arrange
        when(veiculos.buscarPorId("v1")).thenReturn(Optional.of(Fixtures.veiculo("v1", "c1", 30_000, null, 0)));
        when(clientes.buscarPorId("c1")).thenReturn(Optional.of(Fixtures.cliente("c1", CanalNotificacao.SMS)));
        when(veiculos.buscarPorPlaca(new Placa("BRA2E19"))).thenReturn(Optional.empty());

        // Act
        Veiculo atualizado = servico.atualizar("v1", Fixtures.veiculoRequest("c1", "BRA2E19", 31_000));

        // Assert
        assertThat(atualizado.quilometragemAtual().km()).isEqualTo(31_000L);
    }

    @Test
    @DisplayName("Atualizar: quilometragem menor que a atual é rejeitada")
    void atualizarQuilometragemMenor() {
        // Arrange
        when(veiculos.buscarPorId("v1")).thenReturn(Optional.of(Fixtures.veiculo("v1", "c1", 30_000, null, 0)));
        when(clientes.buscarPorId("c1")).thenReturn(Optional.of(Fixtures.cliente("c1", CanalNotificacao.SMS)));
        when(veiculos.buscarPorPlaca(new Placa("BRA2E19"))).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> servico.atualizar("v1", Fixtures.veiculoRequest("c1", "BRA2E19", 29_000)))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(veiculos, never()).salvar(any());
    }

    @Test
    @DisplayName("Remover: veículo sem ordens de serviço é removido")
    void removerComSucesso() {
        // Arrange
        when(veiculos.buscarPorId("v1")).thenReturn(Optional.of(Fixtures.veiculo("v1", "c1", 30_000, null, 0)));
        when(ordens.listarPorVeiculo("v1")).thenReturn(List.of());

        // Act
        servico.remover("v1");

        // Assert
        verify(veiculos).remover("v1");
    }

    @Test
    @DisplayName("Remover: veículo com histórico de OS não pode ser removido")
    void removerComOrdens() {
        // Arrange
        when(veiculos.buscarPorId("v1")).thenReturn(Optional.of(Fixtures.veiculo("v1", "c1", 30_000, null, 0)));
        when(ordens.listarPorVeiculo("v1")).thenReturn(
                List.of(Fixtures.ordem("os1", StatusOrdemServico.ENTREGUE, TipoServicoBase.REVISAO_PREVENTIVA)));

        // Act + Assert
        assertThatThrownBy(() -> servico.remover("v1")).isInstanceOf(ConflitoException.class);
        verify(veiculos, never()).remover("v1");
    }

    @Test
    @DisplayName("Ordens do veículo: lado N do relacionamento Veículo 1:N OS")
    void ordensDoVeiculo() {
        // Arrange
        when(veiculos.buscarPorId("v1")).thenReturn(Optional.of(Fixtures.veiculo("v1", "c1", 30_000, null, 0)));
        when(ordens.listarPorVeiculo("v1")).thenReturn(
                List.of(Fixtures.ordem("os1", StatusOrdemServico.ENTREGUE, TipoServicoBase.REVISAO_PREVENTIVA)));

        // Act + Assert
        assertThat(servico.ordensDoVeiculo("v1")).hasSize(1);
    }
}
