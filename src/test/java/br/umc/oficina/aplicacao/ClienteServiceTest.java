package br.umc.oficina.aplicacao;

import br.umc.oficina.Fixtures;
import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.cliente.Cliente;
import br.umc.oficina.dominio.comum.ConflitoException;
import br.umc.oficina.dominio.comum.Cpf;
import br.umc.oficina.dominio.comum.RecursoNaoEncontradoException;
import br.umc.oficina.dominio.repositorio.ClienteRepository;
import br.umc.oficina.dominio.repositorio.VeiculoRepository;
import br.umc.oficina.dominio.veiculo.Veiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static br.umc.oficina.Fixtures.CPF_VALIDO;
import static br.umc.oficina.Fixtures.HOJE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Repositórios simulados com Mockito (opção 3 da aula: sem banco real). */
class ClienteServiceTest {

    private ClienteRepository clientes;
    private VeiculoRepository veiculos;
    private ClienteService servico;

    @BeforeEach
    void preparar() {
        clientes = mock(ClienteRepository.class);
        veiculos = mock(VeiculoRepository.class);
        servico = new ClienteService(clientes, veiculos, Fixtures.RELOGIO);
        when(clientes.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @Test
    @DisplayName("Cadastrar: cria cliente ativo com data de cadastro de hoje")
    void cadastrarComSucesso() {
        // Arrange
        when(clientes.buscarPorCpf(new Cpf(CPF_VALIDO))).thenReturn(Optional.empty());

        // Act
        Cliente criado = servico.cadastrar(Fixtures.clienteRequest(CPF_VALIDO));

        // Assert
        assertThat(criado.id()).isNotNull();
        assertThat(criado.ativo()).isTrue();
        assertThat(criado.dataCadastro()).isEqualTo(HOJE);
        assertThat(criado.endereco().cidade()).isEqualTo("Jacareí");
        verify(clientes).salvar(criado);
    }

    @Test
    @DisplayName("Cadastrar: CPF já cadastrado gera conflito e nada é salvo")
    void cadastrarCpfDuplicado() {
        // Arrange
        when(clientes.buscarPorCpf(new Cpf(CPF_VALIDO)))
                .thenReturn(Optional.of(Fixtures.cliente("outro", CanalNotificacao.EMAIL)));

        // Act + Assert
        assertThatThrownBy(() -> servico.cadastrar(Fixtures.clienteRequest(CPF_VALIDO)))
                .isInstanceOf(ConflitoException.class);
        verify(clientes, never()).salvar(any());
    }

    @Test
    @DisplayName("Buscar: retorna o cliente existente")
    void buscarExistente() {
        // Arrange
        Cliente cliente = Fixtures.cliente("c1", CanalNotificacao.EMAIL);
        when(clientes.buscarPorId("c1")).thenReturn(Optional.of(cliente));

        // Act + Assert
        assertThat(servico.buscarPorId("c1")).isEqualTo(cliente);
    }

    @Test
    @DisplayName("Buscar: id inexistente gera 404")
    void buscarInexistente() {
        // Arrange
        when(clientes.buscarPorId("x")).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> servico.buscarPorId("x")).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Atualizar: mantém id e data de cadastro originais")
    void atualizarComSucesso() {
        // Arrange
        Cliente atual = Fixtures.cliente("c1", CanalNotificacao.EMAIL);
        when(clientes.buscarPorId("c1")).thenReturn(Optional.of(atual));
        when(clientes.buscarPorCpf(new Cpf(CPF_VALIDO))).thenReturn(Optional.of(atual));

        // Act
        Cliente atualizado = servico.atualizar("c1", Fixtures.clienteRequest(CPF_VALIDO));

        // Assert
        assertThat(atualizado.id()).isEqualTo("c1");
        assertThat(atualizado.dataCadastro()).isEqualTo(atual.dataCadastro());
        assertThat(atualizado.canalNotificacao()).isEqualTo(CanalNotificacao.WHATSAPP);
    }

    @Test
    @DisplayName("Atualizar: CPF pertencente a outro cliente gera conflito")
    void atualizarCpfDeOutroCliente() {
        // Arrange
        when(clientes.buscarPorId("c1")).thenReturn(Optional.of(Fixtures.cliente("c1", CanalNotificacao.EMAIL)));
        when(clientes.buscarPorCpf(new Cpf(CPF_VALIDO)))
                .thenReturn(Optional.of(Fixtures.cliente("c2", CanalNotificacao.EMAIL)));

        // Act + Assert
        assertThatThrownBy(() -> servico.atualizar("c1", Fixtures.clienteRequest(CPF_VALIDO)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    @DisplayName("Remover: cliente sem veículos é removido")
    void removerComSucesso() {
        // Arrange
        when(clientes.buscarPorId("c1")).thenReturn(Optional.of(Fixtures.cliente("c1", CanalNotificacao.EMAIL)));
        when(veiculos.listarPorCliente("c1")).thenReturn(List.of());

        // Act
        servico.remover("c1");

        // Assert
        verify(clientes).remover("c1");
    }

    @Test
    @DisplayName("Remover: cliente com veículos (1:N) não pode ser removido")
    void removerComVeiculos() {
        // Arrange
        when(clientes.buscarPorId("c1")).thenReturn(Optional.of(Fixtures.cliente("c1", CanalNotificacao.EMAIL)));
        when(veiculos.listarPorCliente("c1"))
                .thenReturn(List.of(Fixtures.veiculo("v1", "c1", 1000, null, 0)));

        // Act + Assert
        assertThatThrownBy(() -> servico.remover("c1")).isInstanceOf(ConflitoException.class);
        verify(clientes, never()).remover("c1");
    }

    @Test
    @DisplayName("Veículos do cliente: lista o lado N do relacionamento")
    void veiculosDoCliente() {
        // Arrange
        Veiculo veiculo = Fixtures.veiculo("v1", "c1", 1000, null, 0);
        when(clientes.buscarPorId("c1")).thenReturn(Optional.of(Fixtures.cliente("c1", CanalNotificacao.EMAIL)));
        when(veiculos.listarPorCliente("c1")).thenReturn(List.of(veiculo));

        // Act
        List<Veiculo> resultado = servico.veiculosDoCliente("c1");

        // Assert
        assertThat(resultado).containsExactly(veiculo);
    }

    @Test
    @DisplayName("Veículos do cliente: cliente inexistente gera 404")
    void veiculosDeClienteInexistente() {
        // Arrange
        when(clientes.buscarPorId("x")).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> servico.veiculosDoCliente("x")).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
