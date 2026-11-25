package br.umc.oficina.dominio.comum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjetosDeValorTest {

    @Test
    @DisplayName("Dinheiro: soma, acréscimo e desconto percentual com 2 casas")
    void dinheiroOperacoes() {
        // Arrange
        Dinheiro cem = Dinheiro.de("100.00");

        // Act
        Dinheiro soma = cem.mais(Dinheiro.de("50.50"));
        Dinheiro comJuros = cem.acrescidoDe(8);
        Dinheiro comDesconto = cem.descontadoDe(15);

        // Assert
        assertThat(soma).isEqualTo(Dinheiro.de("150.50"));
        assertThat(comJuros).isEqualTo(Dinheiro.de("108.00"));
        assertThat(comDesconto).isEqualTo(Dinheiro.de("85.00"));
    }

    @Test
    @DisplayName("Dinheiro: valor negativo viola a invariante")
    void dinheiroNegativo() {
        assertThatThrownBy(() -> Dinheiro.de("-1.00"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("negativo");
    }

    @Test
    @DisplayName("CPF válido é aceito e formatado")
    void cpfValido() {
        // Act
        Cpf cpf = new Cpf("52998224725");

        // Assert
        assertThat(cpf.formatado()).isEqualTo("529.982.247-25");
    }

    @Test
    @DisplayName("CPF com dígito verificador errado ou repetido é rejeitado")
    void cpfInvalido() {
        assertThatThrownBy(() -> new Cpf("529.982.247-24")).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> new Cpf("111.111.111-11")).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("Placa nos padrões antigo e Mercosul")
    void placaValida() {
        assertThat(new Placa("abc-1234").valor()).isEqualTo("ABC1234");
        assertThat(new Placa("BRA2E19").valor()).isEqualTo("BRA2E19");
    }

    @Test
    @DisplayName("Placa fora do padrão é rejeitada")
    void placaInvalida() {
        assertThatThrownBy(() -> new Placa("12ABC34")).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("E-mail e telefone inválidos são rejeitados")
    void emailETelefoneInvalidos() {
        assertThatThrownBy(() -> new Email("sem-arroba")).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> new Telefone("1234")).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("Quilometragem negativa é rejeitada")
    void quilometragemNegativa() {
        assertThatThrownBy(() -> new Quilometragem(-5)).isInstanceOf(RegraDeNegocioException.class);
    }
}
