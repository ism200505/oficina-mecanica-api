package br.umc.oficina.dominio.repositorio;

import br.umc.oficina.dominio.usuario.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorLogin(String login);

    List<Usuario> listarTodos();
}
