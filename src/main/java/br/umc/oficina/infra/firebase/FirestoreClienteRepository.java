package br.umc.oficina.infra.firebase;

import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.cliente.Cliente;
import br.umc.oficina.dominio.cliente.Endereco;
import br.umc.oficina.dominio.comum.Cpf;
import br.umc.oficina.dominio.comum.Email;
import br.umc.oficina.dominio.comum.Telefone;
import br.umc.oficina.dominio.repositorio.ClienteRepository;
import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Coleção "clientes". O endereço (1:1) fica embutido no próprio documento do cliente,
 * que é a forma recomendada de modelar composição em bancos de documentos.
 */
@Repository
@Profile("!memoria")
public class FirestoreClienteRepository extends RepositorioFirestore implements ClienteRepository {

    public FirestoreClienteRepository(Firestore firestore) {
        super(firestore, "clientes");
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        gravar(cliente.id(), paraDocumento(cliente));
        return cliente;
    }

    @Override
    public Optional<Cliente> buscarPorId(String id) {
        return ler(id).map(this::paraCliente);
    }

    @Override
    public Optional<Cliente> buscarPorCpf(Cpf cpf) {
        return lerOnde("cpf", cpf.numero()).stream().findFirst().map(this::paraCliente);
    }

    @Override
    public List<Cliente> listarTodos() {
        return lerTodos().stream().map(this::paraCliente).toList();
    }

    @Override
    public void remover(String id) {
        apagar(id);
    }

    private Documento paraDocumento(Cliente c) {
        Endereco e = c.endereco();
        Documento endereco = Documento.novo()
                .com("logradouro", e.logradouro()).com("numero", e.numero())
                .com("complemento", e.complemento()).com("bairro", e.bairro())
                .com("cidade", e.cidade()).com("uf", e.uf()).com("cep", e.cep());
        return Documento.novo()
                .com("id", c.id()).com("nome", c.nome()).com("cpf", c.cpf().numero())
                .com("email", c.email().endereco()).com("telefone", c.telefone().numero())
                .com("dataNascimento", c.dataNascimento()).com("endereco", endereco)
                .com("canalNotificacao", c.canalNotificacao()).com("dataCadastro", c.dataCadastro())
                .com("ativo", c.ativo()).com("observacoes", c.observacoes());
    }

    private Cliente paraCliente(Documento d) {
        Documento e = d.subdocumento("endereco");
        Endereco endereco = new Endereco(e.texto("logradouro"), e.texto("numero"), e.texto("complemento"),
                e.texto("bairro"), e.texto("cidade"), e.texto("uf"), e.texto("cep"));
        return new Cliente(d.texto("id"), d.texto("nome"), new Cpf(d.texto("cpf")), new Email(d.texto("email")),
                new Telefone(d.texto("telefone")), d.data("dataNascimento"), endereco,
                d.enumeracao("canalNotificacao", CanalNotificacao.class), d.data("dataCadastro"),
                d.logico("ativo"), d.texto("observacoes"));
    }
}
