package br.umc.oficina.api.dto;

import br.umc.oficina.dominio.cliente.Endereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoDto(
        @NotBlank String logradouro,
        @NotBlank String numero,
        String complemento,
        @NotBlank String bairro,
        @NotBlank String cidade,
        @NotBlank @Size(min = 2, max = 2) String uf,
        @NotBlank @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP deve ter o formato 00000-000") String cep) {

    public Endereco paraDominio() {
        return new Endereco(logradouro, numero, complemento, bairro, cidade, uf, cep);
    }

    public static EnderecoDto de(Endereco e) {
        return new EnderecoDto(e.logradouro(), e.numero(), e.complemento(), e.bairro(), e.cidade(), e.uf(), e.cep());
    }
}
