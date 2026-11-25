package br.umc.oficina.dominio.ordem;

import br.umc.oficina.dominio.comum.Quilometragem;
import br.umc.oficina.dominio.comum.RegraDeNegocioException;
import br.umc.oficina.dominio.servico.Adicionais;
import br.umc.oficina.dominio.servico.TipoServicoBase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Entidade Ordem de Serviço (14 atributos).
 * Relacionamentos: N:1 com Veiculo, N:1 com Cliente e 1:1 com {@link Orcamento}.
 */
public class OrdemServico {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String id;
    private final String numero;
    private final String clienteId;
    private final String veiculoId;
    private final String descricaoProblema;
    private final String mecanicoResponsavel;
    private final Quilometragem quilometragemEntrada;
    private final TipoServicoBase servicoBase;
    private final Adicionais adicionais;
    private final Orcamento orcamento;
    private StatusOrdemServico status;
    private final LocalDateTime dataAbertura;
    private final LocalDateTime previsaoEntrega;
    private LocalDateTime dataConclusao;

    public OrdemServico(String id, String numero, String clienteId, String veiculoId,
                        String descricaoProblema, String mecanicoResponsavel,
                        Quilometragem quilometragemEntrada, TipoServicoBase servicoBase,
                        Adicionais adicionais, Orcamento orcamento, StatusOrdemServico status,
                        LocalDateTime dataAbertura, LocalDateTime previsaoEntrega,
                        LocalDateTime dataConclusao) {
        this.id = Objects.requireNonNull(id, "id é obrigatório");
        this.numero = Objects.requireNonNull(numero, "número é obrigatório");
        this.clienteId = Objects.requireNonNull(clienteId, "cliente é obrigatório");
        this.veiculoId = Objects.requireNonNull(veiculoId, "veículo é obrigatório");
        this.descricaoProblema = Objects.requireNonNull(descricaoProblema, "descrição do problema é obrigatória");
        this.mecanicoResponsavel = Objects.requireNonNull(mecanicoResponsavel, "mecânico responsável é obrigatório");
        this.quilometragemEntrada = Objects.requireNonNull(quilometragemEntrada, "quilometragem é obrigatória");
        this.servicoBase = Objects.requireNonNull(servicoBase, "serviço base é obrigatório");
        this.adicionais = Objects.requireNonNull(adicionais, "adicionais não podem ser nulos");
        this.orcamento = Objects.requireNonNull(orcamento, "orçamento é obrigatório");
        this.status = Objects.requireNonNull(status, "status é obrigatório");
        this.dataAbertura = Objects.requireNonNull(dataAbertura, "data de abertura é obrigatória");
        this.previsaoEntrega = Objects.requireNonNull(previsaoEntrega, "previsão de entrega é obrigatória");
        this.dataConclusao = dataConclusao;
    }

    /** Muda o status respeitando o ciclo de vida definido em {@link StatusOrdemServico}. */
    public void alterarStatus(StatusOrdemServico novo, LocalDateTime agora) {
        if (!status.podeMudarPara(novo)) {
            throw new RegraDeNegocioException("Não é permitido mudar a OS de " + status + " para " + novo
                    + ". Permitidos: " + status.proximosPermitidos());
        }
        this.status = novo;
        registrarConclusaoSeNecessario(agora);
    }

    private void registrarConclusaoSeNecessario(LocalDateTime agora) {
        if (status == StatusOrdemServico.CONCLUIDA) {
            this.dataConclusao = agora;
        }
    }

    public boolean concluiuRevisaoPreventiva() {
        return status == StatusOrdemServico.CONCLUIDA && servicoBase == TipoServicoBase.REVISAO_PREVENTIVA;
    }

    public boolean podeSerExcluida() {
        return status.permiteExclusao();
    }

    /** Texto enviado automaticamente ao cliente a cada mudança de status. */
    public String mensagemDeAcompanhamento() {
        return "OS " + numero + ": " + status.mensagemAoCliente() + ". Serviço: " + orcamento.descricao()
                + ". Valor: " + orcamento.valorFinal() + ". Previsão de entrega: "
                + previsaoEntrega.format(FORMATO_DATA) + ".";
    }

    public String id() { return id; }
    public String numero() { return numero; }
    public String clienteId() { return clienteId; }
    public String veiculoId() { return veiculoId; }
    public String descricaoProblema() { return descricaoProblema; }
    public String mecanicoResponsavel() { return mecanicoResponsavel; }
    public Quilometragem quilometragemEntrada() { return quilometragemEntrada; }
    public TipoServicoBase servicoBase() { return servicoBase; }
    public Adicionais adicionais() { return adicionais; }
    public Orcamento orcamento() { return orcamento; }
    public StatusOrdemServico status() { return status; }
    public LocalDateTime dataAbertura() { return dataAbertura; }
    public LocalDateTime previsaoEntrega() { return previsaoEntrega; }
    public LocalDateTime dataConclusao() { return dataConclusao; }
}
