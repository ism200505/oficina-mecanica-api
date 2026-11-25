package br.umc.oficina.aplicacao;

import br.umc.oficina.Fixtures;
import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.repositorio.ClienteRepository;
import br.umc.oficina.dominio.repositorio.NotificacaoRepository;
import br.umc.oficina.dominio.repositorio.VeiculoRepository;
import br.umc.oficina.dominio.veiculo.Veiculo;
import br.umc.oficina.infra.notificacao.WhatsAppNotificador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static br.umc.oficina.Fixtures.HOJE;
import static br.umc.oficina.Fixtures.RELOGIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Automação: rotina agendada de lembretes de revisão. */
class LembreteRevisaoServiceTest {

    private VeiculoRepository veiculos;
    private ClienteRepository clientes;
    private NotificacaoRepository historico;
    private LembreteRevisaoService servico;

    @BeforeEach
    void preparar() {
        veiculos = mock(VeiculoRepository.class);
        clientes = mock(ClienteRepository.class);
        historico = mock(NotificacaoRepository.class);
        when(historico.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
        CentralDeNotificacoes central = new CentralDeNotificacoes(List.of(new WhatsAppNotificador(RELOGIO)), historico);
        servico = new LembreteRevisaoService(veiculos, clientes, central, RELOGIO);
        when(clientes.buscarPorId("c1")).thenReturn(Optional.of(Fixtures.cliente("c1", CanalNotificacao.WHATSAPP)));
    }

    @Test
    @DisplayName("Envia lembrete apenas para veículos com revisão vencida e registra o envio")
    void enviaParaVencidos() {
        // Arrange
        Veiculo vencido = Fixtures.veiculo("v1", "c1", 70_000, HOJE.minusMonths(8), 50_000);
        Veiculo emDia = Fixtures.veiculo("v2", "c1", 51_000, HOJE.minusMonths(1), 50_000);
        when(veiculos.listarTodos()).thenReturn(List.of(vencido, emDia));

        // Act
        int enviados = servico.executar();

        // Assert
        assertThat(enviados).isEqualTo(1);
        assertThat(vencido.dataUltimoLembrete()).isEqualTo(HOJE);
        assertThat(emDia.dataUltimoLembrete()).isNull();
        verify(historico, times(1)).salvar(any());
        verify(veiculos).salvar(vencido);
    }

    @Test
    @DisplayName("Não repete o lembrete para quem já foi avisado nos últimos 30 dias")
    void naoRepeteLembrete() {
        // Arrange
        Veiculo jaAvisado = Fixtures.veiculo("v1", "c1", 70_000, HOJE.minusMonths(8), 50_000);
        jaAvisado.registrarLembreteEnviado(HOJE.minusDays(5));
        when(veiculos.listarTodos()).thenReturn(List.of(jaAvisado));

        // Act
        int enviados = servico.executar();

        // Assert
        assertThat(enviados).isEqualTo(0);
        verify(historico, never()).salvar(any());
    }

    @Test
    @DisplayName("Veículo sem proprietário encontrado não conta como notificado")
    void semProprietario() {
        // Arrange
        Veiculo orfao = Fixtures.veiculo("v1", "c-inexistente", 70_000, HOJE.minusMonths(8), 50_000);
        when(veiculos.listarTodos()).thenReturn(List.of(orfao));
        when(clientes.buscarPorId("c-inexistente")).thenReturn(Optional.empty());

        // Act + Assert
        assertThat(servico.executar()).isEqualTo(0);
    }
}
