package br.umc.oficina.aplicacao;

import br.umc.oficina.Fixtures;
import br.umc.oficina.api.dto.LoginRequest;
import br.umc.oficina.api.dto.TokenResponse;
import br.umc.oficina.api.dto.UsuarioRequest;
import br.umc.oficina.dominio.comum.ConflitoException;
import br.umc.oficina.dominio.repositorio.UsuarioRepository;
import br.umc.oficina.dominio.usuario.Papel;
import br.umc.oficina.dominio.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutenticacaoEUsuarioServiceTest {

    private UsuarioRepository usuarios;
    private PasswordEncoder codificador;
    private GeradorDeToken geradorDeToken;
    private AutenticacaoService autenticacao;
    private UsuarioService usuarioService;

    private final Usuario admin = new Usuario("u1", "Administrador", "admin", "HASH", Papel.ADMIN, true);

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        codificador = mock(PasswordEncoder.class);
        geradorDeToken = mock(GeradorDeToken.class);
        autenticacao = new AutenticacaoService(usuarios, codificador, geradorDeToken,
                new LimitadorDeTentativas(Fixtures.RELOGIO));
        usuarioService = new UsuarioService(usuarios, codificador);
        when(usuarios.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
        when(codificador.encode("senhaForte123")).thenReturn("HASH-BCRYPT");
    }

    @Test
    @DisplayName("Login: credenciais corretas geram token Bearer")
    void loginComSucesso() {
        // Arrange
        when(usuarios.buscarPorLogin("admin")).thenReturn(Optional.of(admin));
        when(codificador.matches("admin123", "HASH")).thenReturn(true);
        when(geradorDeToken.gerar(admin)).thenReturn("jwt.token.assinado");
        when(geradorDeToken.validadeEmMinutos()).thenReturn(120L);

        // Act
        TokenResponse resposta = autenticacao.autenticar(new LoginRequest(" ADMIN ", "admin123"));

        // Assert
        assertThat(resposta.token()).isEqualTo("jwt.token.assinado");
        assertThat(resposta.tipo()).isEqualTo("Bearer");
        assertThat(resposta.papel()).isEqualTo(Papel.ADMIN);
    }

    @Test
    @DisplayName("Login: senha errada gera 401 e nenhum token")
    void loginSenhaErrada() {
        // Arrange
        when(usuarios.buscarPorLogin("admin")).thenReturn(Optional.of(admin));
        when(codificador.matches("errada", "HASH")).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> autenticacao.autenticar(new LoginRequest("admin", "errada")))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("Login ou senha inválidos");
        verify(geradorDeToken, never()).gerar(any());
    }

    @Test
    @DisplayName("Login: usuário inativo não entra, mesmo com a senha certa")
    void loginUsuarioInativo() {
        // Arrange
        Usuario inativo = new Usuario("u2", "Ex-funcionário", "ex", "HASH", Papel.MECANICO, false);
        when(usuarios.buscarPorLogin("ex")).thenReturn(Optional.of(inativo));
        when(codificador.matches("senha", "HASH")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> autenticacao.autenticar(new LoginRequest("ex", "senha")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    @DisplayName("Login: após 5 falhas seguidas o login é bloqueado (força bruta)")
    void bloqueioPorForcaBruta() {
        // Arrange
        when(usuarios.buscarPorLogin("admin")).thenReturn(Optional.of(admin));
        when(codificador.matches("errada", "HASH")).thenReturn(false);
        when(codificador.matches("admin123", "HASH")).thenReturn(true);
        for (int tentativa = 0; tentativa < 5; tentativa++) {
            assertThatThrownBy(() -> autenticacao.autenticar(new LoginRequest("admin", "errada")))
                    .isInstanceOf(CredenciaisInvalidasException.class);
        }

        // Act + Assert: nem a senha certa entra durante o bloqueio
        assertThatThrownBy(() -> autenticacao.autenticar(new LoginRequest("admin", "admin123")))
                .isInstanceOf(LoginBloqueadoException.class);
    }

    @Test
    @DisplayName("Cadastro de usuário: senha é gravada somente como hash")
    void cadastrarUsuario() {
        // Arrange
        when(usuarios.buscarPorLogin("joao")).thenReturn(Optional.empty());

        // Act
        Usuario criado = usuarioService.cadastrar(new UsuarioRequest("João", "Joao", "senhaForte123", Papel.MECANICO));

        // Assert
        assertThat(criado.login()).isEqualTo("joao");
        assertThat(criado.senhaHash()).isEqualTo("HASH-BCRYPT");
        assertThat(criado.ativo()).isTrue();
    }

    @Test
    @DisplayName("Cadastro de usuário: login repetido gera conflito")
    void cadastrarLoginRepetido() {
        // Arrange
        when(usuarios.buscarPorLogin("admin")).thenReturn(Optional.of(admin));

        // Act + Assert
        assertThatThrownBy(() -> usuarioService.cadastrar(new UsuarioRequest("Outro", "admin", "senhaForte123", Papel.ADMIN)))
                .isInstanceOf(ConflitoException.class);
        verify(usuarios, never()).salvar(any());
    }

    @Test
    @DisplayName("Administrador inicial: criado só quando ainda não existe")
    void garantirAdministrador() {
        // Arrange
        when(usuarios.buscarPorLogin("admin")).thenReturn(Optional.empty());
        when(usuarios.buscarPorLogin("root")).thenReturn(Optional.of(admin));

        // Act + Assert
        assertThat(usuarioService.garantirAdministrador("admin", "senhaForte123")).isTrue();
        assertThat(usuarioService.garantirAdministrador("root", "senhaForte123")).isFalse();
    }
}
