package br.umc.oficina.api.dto;

import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.cliente.Cliente;
import br.umc.oficina.dominio.comum.Cpf;
import br.umc.oficina.dominio.comum.Email;
import br.umc.oficina.dominio.comum.Telefone;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ClienteRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank String cpf,
        @NotBlank @jakarta.validation.constraints.Email String email,
        @NotBlank String telefone,
        @NotNull @Past LocalDate dataNascimento,
        @NotNull @Valid EnderecoDto endereco,
        @NotNull CanalNotificacao canalNotificacao,
        @Size(max = 500) String observacoes) {

    public Cpf cpfValidado() {
        return new Cpf(cpf);
    }

    public Cliente paraCliente(String id, LocalDate dataCadastro, boolean ativo) {
        return new Cliente(id, nome, cpfValidado(), new Email(email), new Telefone(telefone),
                dataNascimento, endereco.paraDominio(), canalNotificacao, dataCadastro, ativo, observacoes);
    }
}
