package br.umc.oficina.dominio.cliente;

import br.umc.oficina.dominio.comum.Cpf;
import br.umc.oficina.dominio.comum.Email;
import br.umc.oficina.dominio.comum.RegraDeNegocioException;
import br.umc.oficina.dominio.comum.Telefone;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidade Cliente (11 atributos).
 * Relacionamentos: 1:1 com {@link Endereco} e 1:N com Veiculo (um cliente possui vários veículos).
 */
public class Cliente {

    private final String id;
    private final String nome;
    private final Cpf cpf;
    private final Email email;
    private final Telefone telefone;
    private final LocalDate dataNascimento;
    private final Endereco endereco;
    private final CanalNotificacao canalNotificacao;
    private final LocalDate dataCadastro;
    private final boolean ativo;
    private final String observacoes;

    public Cliente(String id, String nome, Cpf cpf, Email email, Telefone telefone,
                   LocalDate dataNascimento, Endereco endereco, CanalNotificacao canalNotificacao,
                   LocalDate dataCadastro, boolean ativo, String observacoes) {
        this.id = Objects.requireNonNull(id, "id é obrigatório");
        this.nome = validarNome(nome);
        this.cpf = Objects.requireNonNull(cpf, "CPF é obrigatório");
        this.email = Objects.requireNonNull(email, "E-mail é obrigatório");
        this.telefone = Objects.requireNonNull(telefone, "Telefone é obrigatório");
        this.dataNascimento = Objects.requireNonNull(dataNascimento, "Data de nascimento é obrigatória");
        this.endereco = Objects.requireNonNull(endereco, "Endereço é obrigatório");
        this.canalNotificacao = Objects.requireNonNull(canalNotificacao, "Canal de notificação é obrigatório");
        this.dataCadastro = Objects.requireNonNull(dataCadastro, "Data de cadastro é obrigatória");
        this.ativo = ativo;
        this.observacoes = observacoes == null ? "" : observacoes;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome do cliente é obrigatório");
        }
        return nome.trim();
    }

    public boolean possuiCpf(Cpf outro) {
        return cpf.equals(outro);
    }

    public String id() { return id; }
    public String nome() { return nome; }
    public Cpf cpf() { return cpf; }
    public Email email() { return email; }
    public Telefone telefone() { return telefone; }
    public LocalDate dataNascimento() { return dataNascimento; }
    public Endereco endereco() { return endereco; }
    public CanalNotificacao canalNotificacao() { return canalNotificacao; }
    public LocalDate dataCadastro() { return dataCadastro; }
    public boolean ativo() { return ativo; }
    public String observacoes() { return observacoes; }
}
