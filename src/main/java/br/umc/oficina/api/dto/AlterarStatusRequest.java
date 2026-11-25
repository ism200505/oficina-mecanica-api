package br.umc.oficina.api.dto;

import br.umc.oficina.dominio.ordem.StatusOrdemServico;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(@NotNull StatusOrdemServico status) {
}
