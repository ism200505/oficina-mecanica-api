package br.umc.oficina.dominio.pagamento;

import br.umc.oficina.dominio.comum.Dinheiro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FormaPagamentoTest {

    @Test
    @DisplayName("À vista: até R$ 300 sem desconto, acima 10%, acima de R$ 500 15%")
    void aVistaFaixas() {
        // Arrange
        FormaPagamento aVista = new PagamentoAVista();

        // Act + Assert
        assertThat(aVista.calcularValorFinal(Dinheiro.de("300.00"))).isEqualTo(Dinheiro.de("300.00"));
        assertThat(aVista.calcularValorFinal(Dinheiro.de("400.00"))).isEqualTo(Dinheiro.de("360.00"));
        assertThat(aVista.calcularValorFinal(Dinheiro.de("600.00"))).isEqualTo(Dinheiro.de("510.00"));
    }

    @Test
    @DisplayName("A prazo: acréscimo de 8%")
    void aPrazo() {
        assertThat(new PagamentoAPrazo().calcularValorFinal(Dinheiro.de("500.00"))).isEqualTo(Dinheiro.de("540.00"));
    }

    @Test
    @DisplayName("PIX: 5% de desconto")
    void pix() {
        assertThat(new PagamentoPix().calcularValorFinal(Dinheiro.de("200.00"))).isEqualTo(Dinheiro.de("190.00"));
    }

    static Stream<Arguments> todasAsFormas() {
        return Stream.of(TipoPagamento.values()).map(tipo -> Arguments.of(tipo.forma()));
    }

    /**
     * LSP: qualquer implementação pode substituir a abstração FormaPagamento.
     * Mesmo contrato para todas: aceita qualquer valor (inclusive zero) e devolve valor válido.
     */
    @ParameterizedTest
    @MethodSource("todasAsFormas")
    @DisplayName("LSP: todas as formas respeitam o mesmo contrato")
    void contratoDeSubstituicao(FormaPagamento forma) {
        // Act
        Dinheiro zero = forma.calcularValorFinal(Dinheiro.ZERO);
        Dinheiro alto = forma.calcularValorFinal(Dinheiro.de("10000.00"));

        // Assert
        assertThat(zero).isEqualTo(Dinheiro.ZERO);
        assertThat(alto.valor().compareTo(BigDecimal.ZERO) > 0).isTrue();
        assertThat(forma.descricao()).isNotNull();
    }
}
