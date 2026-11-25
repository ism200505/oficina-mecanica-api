package br.umc.oficina.aplicacao;

import br.umc.oficina.api.dto.LoginRequest;
import br.umc.oficina.api.dto.TokenResponse;
import br.umc.oficina.dominio.repositorio.UsuarioRepository;
import br.umc.oficina.dominio.usuario.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

/** Login de usuário: confere a senha (hash BCrypt), limita tentativas e emite um token JWT. */
@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final GeradorDeToken geradorDeToken;
    private final LimitadorDeTentativas limitador;

    public AutenticacaoService(UsuarioRepository usuarios, PasswordEncoder codificador,
                               GeradorDeToken geradorDeToken, LimitadorDeTentativas limitador) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.geradorDeToken = geradorDeToken;
        this.limitador = limitador;
    }

    public TokenResponse autenticar(LoginRequest credenciais) {
        String login = credenciais.login().trim().toLowerCase(Locale.ROOT);
        limitador.verificar(login);
        Usuario usuario = validarCredenciais(login, credenciais.senha())
                .orElseThrow(() -> falhaDeLogin(login));
        limitador.registrarSucesso(login);
        return new TokenResponse(geradorDeToken.gerar(usuario), "Bearer",
                geradorDeToken.validadeEmMinutos(), usuario.nome(), usuario.papel());
    }

    private Optional<Usuario> validarCredenciais(String login, String senha) {
        return usuarios.buscarPorLogin(login)
                .filter(Usuario::ativo)
                .filter(usuario -> codificador.matches(senha, usuario.senhaHash()));
    }

    private CredenciaisInvalidasException falhaDeLogin(String login) {
        limitador.registrarFalha(login);
        return new CredenciaisInvalidasException();
    }
}
