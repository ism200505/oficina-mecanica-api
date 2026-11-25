package br.umc.oficina.infra.firebase;

import br.umc.oficina.dominio.comum.Placa;
import br.umc.oficina.dominio.comum.Quilometragem;
import br.umc.oficina.dominio.repositorio.VeiculoRepository;
import br.umc.oficina.dominio.veiculo.TipoCombustivel;
import br.umc.oficina.dominio.veiculo.Veiculo;
import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Coleção "veiculos". O relacionamento N:1 com o cliente é guardado no campo "clienteId". */
@Repository
@Profile("!memoria")
public class FirestoreVeiculoRepository extends RepositorioFirestore implements VeiculoRepository {

    public FirestoreVeiculoRepository(Firestore firestore) {
        super(firestore, "veiculos");
    }

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        gravar(veiculo.id(), paraDocumento(veiculo));
        return veiculo;
    }

    @Override
    public Optional<Veiculo> buscarPorId(String id) {
        return ler(id).map(this::paraVeiculo);
    }

    @Override
    public Optional<Veiculo> buscarPorPlaca(Placa placa) {
        return lerOnde("placa", placa.valor()).stream().findFirst().map(this::paraVeiculo);
    }

    @Override
    public List<Veiculo> listarPorCliente(String clienteId) {
        return lerOnde("clienteId", clienteId).stream().map(this::paraVeiculo).toList();
    }

    @Override
    public List<Veiculo> listarTodos() {
        return lerTodos().stream().map(this::paraVeiculo).toList();
    }

    @Override
    public void remover(String id) {
        apagar(id);
    }

    private Documento paraDocumento(Veiculo v) {
        return Documento.novo()
                .com("id", v.id()).com("clienteId", v.clienteId()).com("placa", v.placa().valor())
                .com("marca", v.marca()).com("modelo", v.modelo())
                .com("anoFabricacao", (long) v.anoFabricacao()).com("anoModelo", (long) v.anoModelo())
                .com("cor", v.cor()).com("chassi", v.chassi()).com("combustivel", v.combustivel())
                .com("quilometragemAtual", v.quilometragemAtual().km())
                .com("dataUltimaRevisao", v.dataUltimaRevisao())
                .com("quilometragemUltimaRevisao", v.quilometragemUltimaRevisao().km())
                .com("dataUltimoLembrete", v.dataUltimoLembrete());
    }

    private Veiculo paraVeiculo(Documento d) {
        return new Veiculo(d.texto("id"), d.texto("clienteId"), new Placa(d.texto("placa")),
                d.texto("marca"), d.texto("modelo"), (int) d.numero("anoFabricacao"), (int) d.numero("anoModelo"),
                d.texto("cor"), d.texto("chassi"), d.enumeracao("combustivel", TipoCombustivel.class),
                new Quilometragem(d.numero("quilometragemAtual")), d.data("dataUltimaRevisao"),
                new Quilometragem(d.numero("quilometragemUltimaRevisao")), d.data("dataUltimoLembrete"));
    }
}
