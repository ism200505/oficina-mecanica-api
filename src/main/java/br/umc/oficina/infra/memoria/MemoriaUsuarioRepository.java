package br.umc.oficina.infra.memoria;

import br.umc.oficina.dominio.repositorio.UsuarioRepository;
import br.umc.oficina.dominio.usuario.Usuario;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("memoria")
public class MemoriaUsuarioRepository implements UsuarioRepository {

    private final Map<String, Usuario> dados = new ConcurrentHashMap<>();

    @Override
    public Usuario salvar(Usuario usuario) {
        dados.put(usuario.id(), usuario);
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        return dados.values().stream().filter(u -> u.login().equals(login)).findFirst();
    }

    @Override
    public List<Usuario> listarTodos() {
        return List.copyOf(dados.values());
    }
}
