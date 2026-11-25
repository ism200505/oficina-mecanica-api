package br.umc.oficina.aplicacao;

import br.umc.oficina.api.dto.UsuarioRequest;
import br.umc.oficina.dominio.comum.ConflitoException;
import br.umc.oficina.dominio.repositorio.UsuarioRepository;
import br.umc.oficina.dominio.usuario.Papel;
import br.umc.oficina.dominio.usuario.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder codificador) {
        this.usuarios = usuarios;
        this.codificador = codificador;
    }

    public Usuario cadastrar(UsuarioRequest dados) {
        String login = dados.login().trim().toLowerCase(Locale.ROOT);
        if (usuarios.buscarPorLogin(login).isPresent()) {
            throw new ConflitoException("Login já está em uso: " + login);
        }
        return usuarios.salvar(novoUsuario(dados.nome(), login, dados.senha(), dados.papel()));
    }

    /** Cria o administrador inicial na primeira execução (se ainda não existir). */
    public boolean garantirAdministrador(String login, String senha) {
        if (usuarios.buscarPorLogin(login).isPresent()) {
            return false;
        }
        usuarios.salvar(novoUsuario("Administrador", login, senha, Papel.ADMIN));
        return true;
    }

    public List<Usuario> listar() {
        return usuarios.listarTodos();
    }

    private Usuario novoUsuario(String nome, String login, String senha, Papel papel) {
        return new Usuario(UUID.randomUUID().toString(), nome, login, codificador.encode(senha), papel, true);
    }
}
