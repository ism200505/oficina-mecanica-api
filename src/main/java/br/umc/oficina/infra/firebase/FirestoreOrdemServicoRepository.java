package br.umc.oficina.infra.firebase;

import br.umc.oficina.dominio.comum.Dinheiro;
import br.umc.oficina.dominio.comum.Quilometragem;
import br.umc.oficina.dominio.ordem.Orcamento;
import br.umc.oficina.dominio.ordem.OrdemServico;
import br.umc.oficina.dominio.ordem.StatusOrdemServico;
import br.umc.oficina.dominio.pagamento.TipoPagamento;
import br.umc.oficina.dominio.repositorio.OrdemServicoRepository;
import br.umc.oficina.dominio.servico.Adicionais;
import br.umc.oficina.dominio.servico.Adicional;
import br.umc.oficina.dominio.servico.TipoServicoBase;
import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Coleção "ordens_servico". O orçamento (1:1) fica embutido no documento da OS. */
@Repository
@Profile("!memoria")
public class FirestoreOrdemServicoRepository extends RepositorioFirestore implements OrdemServicoRepository {

    public FirestoreOrdemServicoRepository(Firestore firestore) {
        super(firestore, "ordens_servico");
    }

    @Override
    public OrdemServico salvar(OrdemServico ordem) {
        gravar(ordem.id(), paraDocumento(ordem));
        return ordem;
    }

    @Override
    public Optional<OrdemServico> buscarPorId(String id) {
        return ler(id).map(this::paraOrdem);
    }

    @Override
    public List<OrdemServico> listarPorVeiculo(String veiculoId) {
        return lerOnde("veiculoId", veiculoId).stream().map(this::paraOrdem).toList();
    }

    @Override
    public List<OrdemServico> listarPorStatus(StatusOrdemServico status) {
        return lerOnde("status", status.name()).stream().map(this::paraOrdem).toList();
    }

    @Override
    public List<OrdemServico> listarTodas() {
        return lerTodos().stream().map(this::paraOrdem).toList();
    }

    @Override
    public void remover(String id) {
        apagar(id);
    }

    private Documento paraDocumento(OrdemServico o) {
        Orcamento orc = o.orcamento();
        Documento orcamento = Documento.novo()
                .com("descricao", orc.descricao())
                .com("valorServicos", orc.valorServicos().valor().toPlainString())
                .com("valorFinal", orc.valorFinal().valor().toPlainString())
                .com("prazoEmHoras", (long) orc.prazoEmHoras())
                .com("formaPagamento", orc.formaPagamento());
        return Documento.novo()
                .com("id", o.id()).com("numero", o.numero()).com("clienteId", o.clienteId())
                .com("veiculoId", o.veiculoId()).com("descricaoProblema", o.descricaoProblema())
                .com("mecanicoResponsavel", o.mecanicoResponsavel())
                .com("quilometragemEntrada", o.quilometragemEntrada().km())
                .com("servicoBase", o.servicoBase()).com("adicionais", o.adicionais().nomes())
                .com("orcamento", orcamento).com("status", o.status())
                .com("dataAbertura", o.dataAbertura()).com("previsaoEntrega", o.previsaoEntrega())
                .com("dataConclusao", o.dataConclusao());
    }

    private OrdemServico paraOrdem(Documento d) {
        Documento o = d.subdocumento("orcamento");
        Orcamento orcamento = new Orcamento(o.texto("descricao"), Dinheiro.de(o.texto("valorServicos")),
                Dinheiro.de(o.texto("valorFinal")), (int) o.numero("prazoEmHoras"),
                o.enumeracao("formaPagamento", TipoPagamento.class));
        List<Adicional> adicionais = d.listaDeTextos("adicionais").stream().map(Adicional::valueOf).toList();
        return new OrdemServico(d.texto("id"), d.texto("numero"), d.texto("clienteId"), d.texto("veiculoId"),
                d.texto("descricaoProblema"), d.texto("mecanicoResponsavel"),
                new Quilometragem(d.numero("quilometragemEntrada")),
                d.enumeracao("servicoBase", TipoServicoBase.class), Adicionais.de(adicionais), orcamento,
                d.enumeracao("status", StatusOrdemServico.class), d.dataHora("dataAbertura"),
                d.dataHora("previsaoEntrega"), d.dataHora("dataConclusao"));
    }
}
