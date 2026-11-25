package br.umc.oficina.aplicacao;

import br.umc.oficina.Fixtures;
import br.umc.oficina.aplicacao.observador.NotificarClienteOuvinte;
import br.umc.oficina.aplicacao.observador.OuvinteDeStatus;
import br.umc.oficina.aplicacao.observador.PublicadorDeStatus;
import br.umc.oficina.aplicacao.observador.RegistrarRevisaoOuvinte;
import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.cliente.Cliente;
import br.umc.oficina.dominio.comum.RegraDeNegocioException;
import br.umc.oficina.dominio.notificacao.Notificacao;
import br.umc.oficina.dominio.ordem.OrdemServico;
import br.umc.oficina.dominio.ordem.StatusOrdemServico;
import br.umc.oficina.dominio.repositorio.ClienteRepository;
import br.umc.oficina.dominio.repositorio.NotificacaoRepository;
import br.umc.oficina.dominio.repositorio.VeiculoRepository;
import br.umc.oficina.dominio.servico.TipoServicoBase;
import br.umc.oficina.dominio.veiculo.Veiculo;
import br.umc.oficina.infra.notificacao.EmailNotificador;
import br.umc.oficina.infra.notificacao.SmsNotificador;
import br.umc.oficina.infra.notificacao.WhatsAppNotificador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static br.umc.oficina.Fixtures.AGORA;
import static br.umc.oficina.Fixtures.RELOGIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Testa a Central de Notificações (DIP/LSP) e os ouvintes do padrão Observer. */
class NotificacoesEObservadoresTest {

    private NotificacaoRepository historico;
    private CentralDeNotificacoes central;

    @BeforeEach
    void preparar() {
        historico = mock(NotificacaoRepository.class);
        when(historico.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
        central = new CentralDeNotificacoes(
                List.of(new EmailNotificador(RELOGIO), new WhatsAppNotificador(RELOGIO), new SmsNotificador(RELOGIO)),
                historico);
    }

    @Test
    @DisplayName("Central: usa o canal preferido do cliente e grava no histórico")
    void notificarPeloCanalPreferido() {
        // Arrange
        Cliente cliente = Fixtures.cliente("c1", CanalNotificacao.WHATSAPP);

        // Act
        Notificacao enviada = central.notificar(cliente, "Seu carro está pronto");

        // Assert
        assertThat(enviada.canal()).isEqualTo(CanalNotificacao.WHATSAPP);
        assertThat(enviada.destino()).isEqualTo("+5512998765432");
        assertThat(enviada.dataHora()).isEqualTo(AGORA);
        verify(historico).salvar(enviada);
    }

    @Test
    @DisplayName("Central: canal sem notificador configurado gera erro de regra de negócio")
    void canalSemNotificador() {
        // Arrange
        CentralDeNotificacoes somenteEmail = new CentralDeNotificacoes(List.of(new EmailNotificador(RELOGIO)), historico);

        // Act + Assert
        assertThatThrownBy(() -> somenteEmail.notificar(Fixtures.cliente("c1", CanalNotificacao.SMS), "oi"))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(historico, never()).salvar(any());
    }

    @Test
    @DisplayName("Central: histórico filtra por cliente quando informado")
    void historicoPorCliente() {
        // Arrange
        when(historico.listarPorCliente("c1")).thenReturn(List.of());
        when(historico.listarTodas()).thenReturn(List.of());

        // Act
        central.historico("c1");
        central.historico(null);

        // Assert
        verify(historico).listarPorCliente("c1");
        verify(historico).listarTodas();
    }

    @Test
    @DisplayName("Observer: cliente é notificado automaticamente quando a OS muda de status")
    void ouvinteNotificaCliente() {
        // Arrange
        ClienteRepository clientes = mock(ClienteRepository.class);
        when(clientes.buscarPorId("cli-1")).thenReturn(Optional.of(Fixtures.cliente("cli-1", CanalNotificacao.EMAIL)));
        OrdemServico ordem = Fixtures.ordem("os1", StatusOrdemServico.CONCLUIDA, TipoServicoBase.REVISAO_PREVENTIVA);
        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);

        // Act
        new NotificarClienteOuvinte(clientes, central).statusAlterado(ordem);

        // Assert
        verify(historico).salvar(captor.capture());
        assertThat(captor.getValue().destino()).isEqualTo("maria@email.com");
        assertThat(captor.getValue().mensagem()).contains("concluído");
    }

    @Test
    @DisplayName("Observer: sem cliente encontrado, nada é enviado")
    void ouvinteSemCliente() {
        // Arrange
        ClienteRepository clientes = mock(ClienteRepository.class);
        when(clientes.buscarPorId("cli-1")).thenReturn(Optional.empty());

        // Act
        new NotificarClienteOuvinte(clientes, central)
                .statusAlterado(Fixtures.ordem("os1", StatusOrdemServico.ABERTA, TipoServicoBase.REVISAO_PREVENTIVA));

        // Assert
        verify(historico, never()).salvar(any());
    }

    @Test
    @DisplayName("Observer: revisão preventiva concluída atualiza a última revisão do veículo")
    void registrarRevisaoConcluida() {
        // Arrange
        VeiculoRepository veiculos = mock(VeiculoRepository.class);
        Veiculo veiculo = Fixtures.veiculo("vei-1", "cli-1", 59_000, null, 0);
        when(veiculos.buscarPorId("vei-1")).thenReturn(Optional.of(veiculo));
        OrdemServico ordem = Fixtures.ordem("os1", StatusOrdemServico.EM_EXECUCAO, TipoServicoBase.REVISAO_PREVENTIVA);
        ordem.alterarStatus(StatusOrdemServico.CONCLUIDA, AGORA);

        // Act
        new RegistrarRevisaoOuvinte(veiculos).statusAlterado(ordem);

        // Assert
        assertThat(veiculo.dataUltimaRevisao()).isEqualTo(AGORA.toLocalDate());
        assertThat(veiculo.quilometragemUltimaRevisao().km()).isEqualTo(60_000L);
        verify(veiculos).salvar(veiculo);
    }

    @Test
    @DisplayName("Observer: outros serviços concluídos não alteram a revisão")
    void ignorarOutrosServicos() {
        // Arrange
        VeiculoRepository veiculos = mock(VeiculoRepository.class);
        OrdemServico ordem = Fixtures.ordem("os1", StatusOrdemServico.EM_EXECUCAO, TipoServicoBase.DIAGNOSTICO_ELETRONICO);
        ordem.alterarStatus(StatusOrdemServico.CONCLUIDA, AGORA);

        // Act
        new RegistrarRevisaoOuvinte(veiculos).statusAlterado(ordem);

        // Assert
        verify(veiculos, never()).salvar(any());
    }

    @Test
    @DisplayName("Subject: falha em um ouvinte não impede que os demais sejam avisados")
    void falhaIsoladaNoPublicador() {
        // Arrange
        List<String> avisados = new ArrayList<>();
        OuvinteDeStatus quebrado = ordem -> {
            throw new IllegalStateException("serviço externo fora do ar");
        };
        OuvinteDeStatus saudavel = ordem -> avisados.add(ordem.numero());
        PublicadorDeStatus publicador = new PublicadorDeStatus(List.of(quebrado, saudavel));

        // Act
        publicador.publicar(Fixtures.ordem("os1", StatusOrdemServico.ABERTA, TipoServicoBase.REVISAO_PREVENTIVA));

        // Assert
        assertThat(avisados).containsExactly("OS-20261124-ABC123");
    }
}
