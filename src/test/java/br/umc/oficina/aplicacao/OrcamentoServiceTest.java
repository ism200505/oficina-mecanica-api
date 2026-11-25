package br.umc.oficina.aplicacao;

import br.umc.oficina.dominio.comum.Dinheiro;
import br.umc.oficina.dominio.ordem.Orcamento;
import br.umc.oficina.dominio.pagamento.TipoPagamento;
import br.umc.oficina.dominio.servico.Adicionais;
import br.umc.oficina.dominio.servico.Adicional;
import br.umc.oficina.dominio.servico.TipoServicoBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrcamentoServiceTest {

    private final OrcamentoService servico = new OrcamentoService();

    @Test
    @DisplayName("Orçamento completo: corretiva + freio + leva e traz à vista (acima de R$ 500 = 15%)")
    void orcamentoComAdicionaisAVista() {
        // Arrange
        Adicionais adicionais = Adicionais.de(List.of(Adicional.TROCA_PASTILHAS_FREIO, Adicional.LEVA_E_TRAZ));

        // Act
        Orcamento orcamento = servico.calcular(TipoServicoBase.MANUTENCAO_CORRETIVA, adicionais, TipoPagamento.A_VISTA);

        // Assert: 300 + 220 + 90 = 610 ; 610 - 15% = 518,50 ; prazo 4 + 2 + 2
        assertThat(orcamento.valorServicos()).isEqualTo(Dinheiro.de("610.00"));
        assertThat(orcamento.valorFinal()).isEqualTo(Dinheiro.de("518.50"));
        assertThat(orcamento.prazoEmHoras()).isEqualTo(8);
        assertThat(orcamento.descricao()).contains("Troca de pastilhas de freio", "Leva e traz");
    }

    @Test
    @DisplayName("Orçamento sem adicionais a prazo (+8%)")
    void orcamentoSemAdicionaisAPrazo() {
        // Act
        Orcamento orcamento = servico.calcular(TipoServicoBase.DIAGNOSTICO_ELETRONICO, Adicionais.de(null),
                TipoPagamento.A_PRAZO);

        // Assert: 150 + 8%
        assertThat(orcamento.valorFinal()).isEqualTo(Dinheiro.de("162.00"));
        assertThat(orcamento.formaPagamento()).isEqualTo(TipoPagamento.A_PRAZO);
    }
}
