package br.umc.oficina.dominio;

import br.umc.oficina.Fixtures;
import br.umc.oficina.dominio.comum.Quilometragem;
import br.umc.oficina.dominio.comum.RegraDeNegocioException;
import br.umc.oficina.dominio.ordem.OrdemServico;
import br.umc.oficina.dominio.ordem.StatusOrdemServico;
import br.umc.oficina.dominio.servico.TipoServicoBase;
import br.umc.oficina.dominio.veiculo.Veiculo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static br.umc.oficina.Fixtures.AGORA;
import static br.umc.oficina.Fixtures.HOJE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntidadesTest {

    @Test
    @DisplayName("OS segue o ciclo de vida e registra a data de conclusão")
    void ordemCicloDeVida() {
        // Arrange
        OrdemServico ordem = Fixtures.ordem("os-1", StatusOrdemServico.ABERTA, TipoServicoBase.REVISAO_PREVENTIVA);

        // Act
        ordem.alterarStatus(StatusOrdemServico.EM_EXECUCAO, AGORA);
        ordem.alterarStatus(StatusOrdemServico.CONCLUIDA, AGORA.plusHours(2));

        // Assert
        assertThat(ordem.status()).isEqualTo(StatusOrdemServico.CONCLUIDA);
        assertThat(ordem.dataConclusao()).isEqualTo(AGORA.plusHours(2));
        assertThat(ordem.concluiuRevisaoPreventiva()).isTrue();
    }

    @Test
    @DisplayName("OS não pode pular etapas (ABERTA -> ENTREGUE)")
    void ordemTransicaoInvalida() {
        // Arrange
        OrdemServico ordem = Fixtures.ordem("os-1", StatusOrdemServico.ABERTA, TipoServicoBase.DIAGNOSTICO_ELETRONICO);

        // Act + Assert
        assertThatThrownBy(() -> ordem.alterarStatus(StatusOrdemServico.ENTREGUE, AGORA))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Não é permitido");
        assertThat(ordem.status()).isEqualTo(StatusOrdemServico.ABERTA);
    }

    @Test
    @DisplayName("Mensagem automática contém número, status e valor da OS")
    void mensagemDeAcompanhamento() {
        // Arrange
        OrdemServico ordem = Fixtures.ordem("os-1", StatusOrdemServico.ABERTA, TipoServicoBase.REVISAO_PREVENTIVA);

        // Act
        String mensagem = ordem.mensagemDeAcompanhamento();

        // Assert
        assertThat(mensagem).contains("OS-20261124-ABC123", "ordem de serviço foi aberta", "R$ 237,50");
    }

    @Test
    @DisplayName("Veículo com revisão há 6 meses ou mais precisa de revisão")
    void revisaoVencidaPorTempo() {
        // Arrange
        Veiculo veiculo = Fixtures.veiculo("v1", "c1", 52_000, HOJE.minusMonths(6), 50_000);

        // Act + Assert
        assertThat(veiculo.precisaDeRevisao(HOJE)).isTrue();
    }

    @Test
    @DisplayName("Veículo que rodou 10.000 km desde a revisão precisa de revisão")
    void revisaoVencidaPorQuilometragem() {
        // Arrange
        Veiculo veiculo = Fixtures.veiculo("v1", "c1", 60_000, HOJE.minusMonths(1), 50_000);

        // Act + Assert
        assertThat(veiculo.precisaDeRevisao(HOJE)).isTrue();
    }

    @Test
    @DisplayName("Veículo com revisão recente e pouca rodagem está em dia")
    void revisaoEmDia() {
        // Arrange
        Veiculo veiculo = Fixtures.veiculo("v1", "c1", 55_000, HOJE.minusMonths(2), 50_000);

        // Act + Assert
        assertThat(veiculo.precisaDeRevisao(HOJE)).isFalse();
    }

    @Test
    @DisplayName("Lembrete não se repete antes de 30 dias")
    void lembreteLimitado() {
        // Arrange
        Veiculo veiculo = Fixtures.veiculo("v1", "c1", 70_000, HOJE.minusMonths(8), 50_000);

        // Act
        veiculo.registrarLembreteEnviado(HOJE.minusDays(10));

        // Assert
        assertThat(veiculo.deveReceberLembrete(HOJE)).isFalse();
        assertThat(veiculo.deveReceberLembrete(HOJE.plusDays(20))).isTrue();
    }

    @Test
    @DisplayName("Odômetro não pode voltar")
    void quilometragemNaoVolta() {
        // Arrange
        Veiculo veiculo = Fixtures.veiculo("v1", "c1", 60_000, null, 0);

        // Act + Assert
        assertThatThrownBy(() -> veiculo.registrarQuilometragem(new Quilometragem(59_000)))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("Ano do modelo incoerente com o de fabricação é rejeitado")
    void anoModeloInvalido() {
        assertThatThrownBy(() -> new Veiculo("v1", "c1", new br.umc.oficina.dominio.comum.Placa("ABC1234"),
                "Fiat", "Uno", 2020, 2018, "Branco", null,
                br.umc.oficina.dominio.veiculo.TipoCombustivel.FLEX, new Quilometragem(0), null, null, null))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}
