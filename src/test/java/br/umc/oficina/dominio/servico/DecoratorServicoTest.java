package br.umc.oficina.dominio.servico;

import br.umc.oficina.dominio.comum.Dinheiro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DecoratorServicoTest {

    @Test
    @DisplayName("Serviço base sem decoradores mantém preço, descrição e prazo")
    void servicoBaseSemAdicionais() {
        // Arrange
        Servico revisao = new RevisaoPreventiva();

        // Act
        Servico resultado = Adicionais.nenhum().decorar(revisao);

        // Assert
        assertThat(resultado.preco()).isEqualTo(Dinheiro.de("250.00"));
        assertThat(resultado.descricao()).isEqualTo("Revisão preventiva");
        assertThat(resultado.prazoEmHoras()).isEqualTo(3);
    }

    @Test
    @DisplayName("Decoradores empilhados somam preço, descrição e prazo em tempo de execução")
    void decoradoresEmpilhados() {
        // Arrange
        Servico servico = new AlinhamentoBalanceamentoDecorator(new TrocaDeOleoDecorator(new RevisaoPreventiva()));

        // Act
        Dinheiro preco = servico.preco();

        // Assert: 250 + 180 + 120
        assertThat(preco).isEqualTo(Dinheiro.de("550.00"));
        assertThat(servico.descricao())
                .isEqualTo("Revisão preventiva + Troca de óleo e filtro + Alinhamento e balanceamento");
        assertThat(servico.prazoEmHoras()).isEqualTo(5);
    }

    @Test
    @DisplayName("Garantia estendida incide sobre o total, mesmo escolhida primeiro")
    void garantiaSempreNoFinal() {
        // Arrange
        Adicionais adicionais = Adicionais.de(List.of(Adicional.GARANTIA_ESTENDIDA, Adicional.TROCA_DE_OLEO));

        // Act
        Servico servico = adicionais.decorar(TipoServicoBase.REVISAO_PREVENTIVA.criar());

        // Assert: (250 + 180) * 1,10
        assertThat(servico.preco()).isEqualTo(Dinheiro.de("473.00"));
        assertThat(adicionais.nomes()).containsExactly("TROCA_DE_OLEO", "GARANTIA_ESTENDIDA");
    }

    @Test
    @DisplayName("Adicional repetido é aplicado uma única vez")
    void adicionalRepetido() {
        // Arrange
        Adicionais adicionais = Adicionais.de(List.of(Adicional.LAVAGEM_COMPLETA, Adicional.LAVAGEM_COMPLETA));

        // Act
        Servico servico = adicionais.decorar(new DiagnosticoEletronico());

        // Assert: 150 + 70
        assertThat(servico.preco()).isEqualTo(Dinheiro.de("220.00"));
        assertThat(adicionais.comoLista()).hasSize(1);
    }

    @Test
    @DisplayName("Decorator sem componente é inválido")
    void decoratorSemComponente() {
        assertThatThrownBy(() -> new LevaETrazDecorator(null)).isInstanceOf(NullPointerException.class);
    }
}
