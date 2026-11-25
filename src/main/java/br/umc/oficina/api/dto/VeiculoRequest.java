package br.umc.oficina.api.dto;

import br.umc.oficina.dominio.comum.Placa;
import br.umc.oficina.dominio.comum.Quilometragem;
import br.umc.oficina.dominio.veiculo.TipoCombustivel;
import br.umc.oficina.dominio.veiculo.Veiculo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record VeiculoRequest(
        @NotBlank String clienteId,
        @NotBlank String placa,
        @NotBlank String marca,
        @NotBlank String modelo,
        @NotNull @Min(1950) @Max(2100) Integer anoFabricacao,
        @NotNull @Min(1950) @Max(2101) Integer anoModelo,
        @NotBlank String cor,
        @Size(max = 17) String chassi,
        @NotNull TipoCombustivel combustivel,
        @NotNull @PositiveOrZero Long quilometragemAtual,
        @PastOrPresent LocalDate dataUltimaRevisao,
        @PositiveOrZero Long quilometragemUltimaRevisao) {

    public Placa placaValidada() {
        return new Placa(placa);
    }

    public Veiculo paraVeiculo(String id, LocalDate dataUltimoLembrete) {
        Quilometragem kmRevisao = quilometragemUltimaRevisao == null ? null : new Quilometragem(quilometragemUltimaRevisao);
        return new Veiculo(id, clienteId, placaValidada(), marca, modelo, anoFabricacao, anoModelo, cor, chassi,
                combustivel, new Quilometragem(quilometragemAtual), dataUltimaRevisao, kmRevisao, dataUltimoLembrete);
    }
}
