package br.umc.oficina.infra.firebase;

import br.umc.oficina.dominio.repositorio.UsuarioRepository;
import br.umc.oficina.dominio.usuario.Papel;
import br.umc.oficina.dominio.usuario.Usuario;
import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Coleção "usuarios". Guarda apenas o hash BCrypt da senha. */
@Repository
@Profile("!memoria")
public class FirestoreUsuarioRepository extends RepositorioFirestore implements UsuarioRepository {

    public FirestoreUsuarioRepository(Firestore firestore) {
        super(firestore, "usuarios");
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        gravar(usuario.id(), Documento.novo()
                .com("id", usuario.id()).com("nome", usuario.nome()).com("login", usuario.login())
                .com("senhaHash", usuario.senhaHash()).com("papel", usuario.papel())
                .com("ativo", usuario.ativo()));
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        return lerOnde("login", login).stream().findFirst().map(this::paraUsuario);
    }

    @Override
    public List<Usuario> listarTodos() {
        return lerTodos().stream().map(this::paraUsuario).toList();
    }

    private Usuario paraUsuario(Documento d) {
        return new Usuario(d.texto("id"), d.texto("nome"), d.texto("login"), d.texto("senhaHash"),
                d.enumeracao("papel", Papel.class), d.logico("ativo"));
    }
}
