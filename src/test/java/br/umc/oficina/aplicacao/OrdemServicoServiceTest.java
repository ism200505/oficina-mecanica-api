package br.umc.oficina.aplicacao;

import br.umc.oficina.Fixtures;
import br.umc.oficina.aplicacao.observador.OuvinteDeStatus;
import br.umc.oficina.aplicacao.observador.PublicadorDeStatus;
import br.umc.oficina.dominio.comum.ConflitoException;
import br.umc.oficina.dominio.comum.Dinheiro;
import br.umc.oficina.dominio.comum.RecursoNaoEncontradoException;
import br.umc.oficina.dominio.comum.RegraDeNegocioException;
import br.umc.oficina.dominio.ordem.OrdemServico;
import br.umc.oficina.dominio.ordem.StatusOrdemServico;
import br.umc.oficina.dominio.repositorio.OrdemServicoRepository;
import br.umc.oficina.dominio.repositorio.VeiculoRepository;
import br.umc.oficina.dominio.servico.TipoServicoBase;
import br.umc.oficina.dominio.veiculo.Veiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static br.umc.oficina.Fixtures.AGORA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrdemServicoServiceTest {

    private OrdemServicoRepository ordens;
    private VeiculoRepository veiculos;
    private OuvinteDeStatus ouvinte;
    private OrdemServicoService servico;

    @BeforeEach
    void preparar() {
        ordens = mock(OrdemServicoRepository.class);
        veiculos = mock(VeiculoRepository.class);
        ouvinte = mock(OuvinteDeStatus.class);
        servico = new OrdemServicoService(ordens, veiculos, new OrcamentoService(),
                new PublicadorDeStatus(List.of(ouvinte)), Fixtures.RELOGIO);
        when(ordens.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @Test
    @DisplayName("Abrir: calcula orçamento (Decorator + PIX), previsão e avisa os ouvintes")
    void abrirComSucesso() {
        // Arrange
        Veiculo veiculo = Fixtures.veiculo("v1", "c1", 59_000, null, 0);
        when(veiculos.buscarPorId("v1")).thenReturn(Optional.of(veiculo));

        // Act
        OrdemServico ordem = servico.abrir(Fixtures.ordemRequest("v1", 60_000));

        // Assert: (250 revisão + 180 óleo) - 5% PIX = 408,50 ; prazo 3h + 1h
        assertThat(ordem.status()).isEqualTo(StatusOrdemServico.ABERTA);
        assertThat(ordem.clienteId()).isEqualTo("c1");
        assertThat(ordem.orcamento().valorServicos()).isEqualTo(Dinheiro.de("430.00"));
        assertThat(ordem.orcamento().valorFinal()).isEqualTo(Dinheiro.de("408.50"));
        assertThat(ordem.previsaoEntrega()).isEqualTo(AGORA.plusHours(4));
        assertThat(ordem.numero()).startsWith("OS-20261124-");
        assertThat(veiculo.quilometragemAtual().km()).isEqualTo(60_000L);
        verify(veiculos).salvar(veiculo);
        verify(ouvinte).statusAlterado(ordem);
    }

    @Test
    @DisplayName("Abrir: veículo inexistente gera 404 e ninguém é notificado")
    void abrirVeiculoInexistente() {
        // Arrange
        when(veiculos.buscarPorId("x")).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> servico.abrir(Fixtures.ordemRequest("x", 1000)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verifyNoInteractions(ouvinte);
    }

    @Test
    @DisplayName("Abrir: quilometragem de entrada menor que a do veículo é rejeitada")
    void abrirComQuilometragemMenor() {
        // Arrange
        when(veiculos.buscarPorId("v1")).thenReturn(Optional.of(Fixtures.veiculo("v1", "c1", 59_000, null, 0)));

        // Act + Assert
        assertThatThrownBy(() -> servico.abrir(Fixtures.ordemRequest("v1", 10_000)))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(ordens, never()).salvar(any());
    }

    @Test
    @DisplayName("Alterar status: transição válida é salva e os ouvintes são avisados")
    void alterarStatusComSucesso() {
        // Arrange
        OrdemServico ordem = Fixtures.ordem("os1", StatusOrdemServico.ABERTA, TipoServicoBase.REVISAO_PREVENTIVA);
        when(ordens.buscarPorId("os1")).thenReturn(Optional.of(ordem));

        // Act
        OrdemServico alterada = servico.alterarStatus("os1", StatusOrdemServico.EM_EXECUCAO);

        // Assert
        assertThat(alterada.status()).isEqualTo(StatusOrdemServico.EM_EXECUCAO);
        verify(ordens).salvar(ordem);
        verify(ouvinte).statusAlterado(ordem);
    }

    @Test
    @DisplayName("Alterar status: transição inválida não salva nem notifica")
    void alterarStatusInvalido() {
        // Arrange
        OrdemServico ordem = Fixtures.ordem("os1", StatusOrdemServico.ENTREGUE, TipoServicoBase.REVISAO_PREVENTIVA);
        when(ordens.buscarPorId("os1")).thenReturn(Optional.of(ordem));

        // Act + Assert
        assertThatThrownBy(() -> servico.alterarStatus("os1", StatusOrdemServico.EM_EXECUCAO))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(ordens, never()).salvar(any());
        verifyNoInteractions(ouvinte);
    }

    @Test
    @DisplayName("Listar: com filtro usa a busca por status; sem filtro lista todas")
    void listar() {
        // Arrange
        List<OrdemServico> abertas = List.of(Fixtures.ordem("os1", StatusOrdemServico.ABERTA, TipoServicoBase.REVISAO_PREVENTIVA));
        when(ordens.listarPorStatus(StatusOrdemServico.ABERTA)).thenReturn(abertas);
        when(ordens.listarTodas()).thenReturn(List.of());

        // Act + Assert
        assertThat(servico.listar(StatusOrdemServico.ABERTA)).hasSize(1);
        assertThat(servico.listar(null)).isEmpty();
    }

    @Test
    @DisplayName("Buscar: id inexistente gera 404")
    void buscarInexistente() {
        when(ordens.buscarPorId("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servico.buscarPorId("x")).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Remover: OS aberta pode ser excluída")
    void removerComSucesso() {
        // Arrange
        when(ordens.buscarPorId("os1")).thenReturn(
                Optional.of(Fixtures.ordem("os1", StatusOrdemServico.ABERTA, TipoServicoBase.REVISAO_PREVENTIVA)));

        // Act
        servico.remover("os1");

        // Assert
        verify(ordens).remover("os1");
    }

    @Test
    @DisplayName("Remover: OS em execução não pode ser excluída")
    void removerEmExecucao() {
        // Arrange
        when(ordens.buscarPorId("os1")).thenReturn(
                Optional.of(Fixtures.ordem("os1", StatusOrdemServico.EM_EXECUCAO, TipoServicoBase.REVISAO_PREVENTIVA)));

        // Act + Assert
        assertThatThrownBy(() -> servico.remover("os1")).isInstanceOf(ConflitoException.class);
        verify(ordens, never()).remover("os1");
    }
}
