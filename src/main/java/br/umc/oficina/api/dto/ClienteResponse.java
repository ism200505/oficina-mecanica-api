package br.umc.oficina.api.dto;

import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.cliente.Cliente;

import java.time.LocalDate;

public record ClienteResponse(String id, String nome, String cpf, String email, String telefone,
                              LocalDate dataNascimento, EnderecoDto endereco,
                              CanalNotificacao canalNotificacao, LocalDate dataCadastro,
                              boolean ativo, String observacoes) {

    public static ClienteResponse de(Cliente c) {
        return new ClienteResponse(c.id(), c.nome(), c.cpf().formatado(), c.email().endereco(),
                c.telefone().numero(), c.dataNascimento(), EnderecoDto.de(c.endereco()),
                c.canalNotificacao(), c.dataCadastro(), c.ativo(), c.observacoes());
    }
}
