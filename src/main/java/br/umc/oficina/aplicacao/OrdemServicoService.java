package br.umc.oficina.aplicacao;

import br.umc.oficina.aplicacao.observador.PublicadorDeStatus;
import br.umc.oficina.api.dto.OrdemServicoRequest;
import br.umc.oficina.dominio.comum.ConflitoException;
import br.umc.oficina.dominio.comum.Quilometragem;
import br.umc.oficina.dominio.comum.RecursoNaoEncontradoException;
import br.umc.oficina.dominio.ordem.Orcamento;
import br.umc.oficina.dominio.ordem.OrdemServico;
import br.umc.oficina.dominio.ordem.StatusOrdemServico;
import br.umc.oficina.dominio.repositorio.OrdemServicoRepository;
import br.umc.oficina.dominio.repositorio.VeiculoRepository;
import br.umc.oficina.dominio.servico.Adicionais;
import br.umc.oficina.dominio.veiculo.Veiculo;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class OrdemServicoService {

    private static final DateTimeFormatter FORMATO_NUMERO = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrdemServicoRepository ordens;
    private final VeiculoRepository veiculos;
    private final OrcamentoService orcamentos;
    private final PublicadorDeStatus publicador;
    private final Clock relogio;

    public OrdemServicoService(OrdemServicoRepository ordens, VeiculoRepository veiculos,
                               OrcamentoService orcamentos, PublicadorDeStatus publicador, Clock relogio) {
        this.ordens = ordens;
        this.veiculos = veiculos;
        this.orcamentos = orcamentos;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    /**
     * Abre a OS: valida o veículo, atualiza o odômetro, calcula o orçamento automaticamente
     * (Decorator + Strategy), calcula a previsão de entrega e avisa os ouvintes (Observer).
     */
    public OrdemServico abrir(OrdemServicoRequest dados) {
        Veiculo veiculo = buscarVeiculo(dados.veiculoId());
        Quilometragem entrada = new Quilometragem(dados.quilometragemEntrada());
        veiculo.registrarQuilometragem(entrada);

        Adicionais adicionais = Adicionais.de(dados.adicionais());
        Orcamento orcamento = orcamentos.calcular(dados.servicoBase(), adicionais, dados.formaPagamento());
        LocalDateTime agora = agora();

        OrdemServico ordem = new OrdemServico(UUID.randomUUID().toString(), gerarNumero(agora),
                veiculo.clienteId(), veiculo.id(), dados.descricaoProblema(), dados.mecanicoResponsavel(),
                entrada, dados.servicoBase(), adicionais, orcamento, StatusOrdemServico.ABERTA,
                agora, agora.plusHours(orcamento.prazoEmHoras()), null);

        veiculos.salvar(veiculo);
        OrdemServico salva = ordens.salvar(ordem);
        publicador.publicar(salva);
        return salva;
    }

    public OrdemServico buscarPorId(String id) {
        return ordens.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço", id));
    }

    public List<OrdemServico> listar(StatusOrdemServico status) {
        if (status == null) {
            return ordens.listarTodas();
        }
        return ordens.listarPorStatus(status);
    }

    public OrdemServico alterarStatus(String id, StatusOrdemServico novoStatus) {
        OrdemServico ordem = buscarPorId(id);
        ordem.alterarStatus(novoStatus, agora());
        OrdemServico salva = ordens.salvar(ordem);
        publicador.publicar(salva);
        return salva;
    }

    public void remover(String id) {
        OrdemServico ordem = buscarPorId(id);
        if (!ordem.podeSerExcluida()) {
            throw new ConflitoException("Somente ordens ABERTA ou CANCELADA podem ser excluídas");
        }
        ordens.remover(id);
    }

    private Veiculo buscarVeiculo(String veiculoId) {
        return veiculos.buscarPorId(veiculoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo", veiculoId));
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(relogio).truncatedTo(ChronoUnit.SECONDS);
    }

    private String gerarNumero(LocalDateTime agora) {
        String sufixo = UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        return "OS-" + agora.format(FORMATO_NUMERO) + "-" + sufixo;
    }
}
