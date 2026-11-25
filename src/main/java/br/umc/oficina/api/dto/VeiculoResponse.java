package br.umc.oficina.api.dto;

import br.umc.oficina.dominio.veiculo.TipoCombustivel;
import br.umc.oficina.dominio.veiculo.Veiculo;

import java.time.LocalDate;

public record VeiculoResponse(String id, String clienteId, String placa, String marca, String modelo,
                              int anoFabricacao, int anoModelo, String cor, String chassi,
                              TipoCombustivel combustivel, long quilometragemAtual,
                              LocalDate dataUltimaRevisao, long quilometragemUltimaRevisao,
                              LocalDate dataUltimoLembrete, boolean revisaoPendente) {

    public static VeiculoResponse de(Veiculo v, LocalDate hoje) {
        return new VeiculoResponse(v.id(), v.clienteId(), v.placa().valor(), v.marca(), v.modelo(),
                v.anoFabricacao(), v.anoModelo(), v.cor(), v.chassi(), v.combustivel(),
                v.quilometragemAtual().km(), v.dataUltimaRevisao(), v.quilometragemUltimaRevisao().km(),
                v.dataUltimoLembrete(), v.precisaDeRevisao(hoje));
    }
}
