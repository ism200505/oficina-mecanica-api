package br.umc.oficina.dominio.veiculo;

import br.umc.oficina.dominio.comum.Placa;
import br.umc.oficina.dominio.comum.Quilometragem;
import br.umc.oficina.dominio.comum.RegraDeNegocioException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidade Veículo (14 atributos).
 * Relacionamentos: N:1 com Cliente (proprietário) e 1:N com OrdemServico.
 * Sem "setters": o estado só muda por métodos de negócio (Calistenia de Objetos).
 */
public class Veiculo {

    public static final int INTERVALO_REVISAO_MESES = 6;
    public static final long INTERVALO_REVISAO_KM = 10_000;
    private static final int DIAS_ENTRE_LEMBRETES = 30;

    private final String id;
    private final String clienteId;
    private final Placa placa;
    private final String marca;
    private final String modelo;
    private final int anoFabricacao;
    private final int anoModelo;
    private final String cor;
    private final String chassi;
    private final TipoCombustivel combustivel;
    private Quilometragem quilometragemAtual;
    private LocalDate dataUltimaRevisao;
    private Quilometragem quilometragemUltimaRevisao;
    private LocalDate dataUltimoLembrete;

    public Veiculo(String id, String clienteId, Placa placa, String marca, String modelo,
                   int anoFabricacao, int anoModelo, String cor, String chassi,
                   TipoCombustivel combustivel, Quilometragem quilometragemAtual,
                   LocalDate dataUltimaRevisao, Quilometragem quilometragemUltimaRevisao,
                   LocalDate dataUltimoLembrete) {
        this.id = Objects.requireNonNull(id, "id é obrigatório");
        this.clienteId = Objects.requireNonNull(clienteId, "O veículo precisa de um proprietário");
        this.placa = Objects.requireNonNull(placa, "Placa é obrigatória");
        this.marca = Objects.requireNonNull(marca, "Marca é obrigatória");
        this.modelo = Objects.requireNonNull(modelo, "Modelo é obrigatório");
        this.anoFabricacao = anoFabricacao;
        this.anoModelo = anoModelo;
        this.cor = cor;
        this.chassi = chassi;
        this.combustivel = Objects.requireNonNull(combustivel, "Combustível é obrigatório");
        this.quilometragemAtual = Objects.requireNonNull(quilometragemAtual, "Quilometragem é obrigatória");
        this.dataUltimaRevisao = dataUltimaRevisao;
        this.quilometragemUltimaRevisao = quilometragemUltimaRevisao == null ? Quilometragem.ZERO : quilometragemUltimaRevisao;
        this.dataUltimoLembrete = dataUltimoLembrete;
        validarAnos();
    }

    private void validarAnos() {
        if (anoModelo < anoFabricacao || anoModelo > anoFabricacao + 1) {
            throw new RegraDeNegocioException("Ano do modelo deve ser igual ou 1 ano após o ano de fabricação");
        }
    }

    /** O odômetro nunca anda para trás. */
    public void registrarQuilometragem(Quilometragem nova) {
        if (nova.menorQue(quilometragemAtual)) {
            throw new RegraDeNegocioException("Quilometragem informada é menor que a atual do veículo ("
                    + quilometragemAtual.km() + " km)");
        }
        this.quilometragemAtual = nova;
    }

    public void registrarRevisao(LocalDate data, Quilometragem quilometragem) {
        registrarQuilometragem(quilometragem);
        this.dataUltimaRevisao = data;
        this.quilometragemUltimaRevisao = quilometragem;
        this.dataUltimoLembrete = null;
    }

    public void registrarLembreteEnviado(LocalDate data) {
        this.dataUltimoLembrete = data;
    }

    /** Revisão vence a cada 6 meses ou a cada 10.000 km, o que ocorrer primeiro. */
    public boolean precisaDeRevisao(LocalDate hoje) {
        return venceuPorTempo(hoje) || venceuPorQuilometragem();
    }

    /** Evita enviar o mesmo lembrete todos os dias: no máximo um a cada 30 dias. */
    public boolean deveReceberLembrete(LocalDate hoje) {
        return precisaDeRevisao(hoje) && lembreteLiberado(hoje);
    }

    private boolean venceuPorTempo(LocalDate hoje) {
        return dataUltimaRevisao != null
                && !dataUltimaRevisao.plusMonths(INTERVALO_REVISAO_MESES).isAfter(hoje);
    }

    private boolean venceuPorQuilometragem() {
        return quilometragemAtual.percorridoDesde(quilometragemUltimaRevisao) >= INTERVALO_REVISAO_KM;
    }

    private boolean lembreteLiberado(LocalDate hoje) {
        return dataUltimoLembrete == null
                || !dataUltimoLembrete.plusDays(DIAS_ENTRE_LEMBRETES).isAfter(hoje);
    }

    public boolean pertenceAo(String idCliente) {
        return clienteId.equals(idCliente);
    }

    public String descricaoResumida() {
        return marca + " " + modelo + " (" + placa.valor() + ")";
    }

    public String id() { return id; }
    public String clienteId() { return clienteId; }
    public Placa placa() { return placa; }
    public String marca() { return marca; }
    public String modelo() { return modelo; }
    public int anoFabricacao() { return anoFabricacao; }
    public int anoModelo() { return anoModelo; }
    public String cor() { return cor; }
    public String chassi() { return chassi; }
    public TipoCombustivel combustivel() { return combustivel; }
    public Quilometragem quilometragemAtual() { return quilometragemAtual; }
    public LocalDate dataUltimaRevisao() { return dataUltimaRevisao; }
    public Quilometragem quilometragemUltimaRevisao() { return quilometragemUltimaRevisao; }
    public LocalDate dataUltimoLembrete() { return dataUltimoLembrete; }
}
