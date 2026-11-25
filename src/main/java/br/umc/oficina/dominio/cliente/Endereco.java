package br.umc.oficina.dominio.cliente;

import br.umc.oficina.dominio.comum.RegraDeNegocioException;

/**
 * Endereço do cliente. Relacionamento 1:1 (composição) com {@link Cliente}:
 * cada cliente possui exatamente um endereço, que não existe sem o cliente.
 */
public record Endereco(String logradouro, String numero, String complemento,
                       String bairro, String cidade, String uf, String cep) {

    public Endereco {
        exigir(logradouro, "logradouro");
        exigir(numero, "número");
        exigir(bairro, "bairro");
        exigir(cidade, "cidade");
        exigir(uf, "UF");
        exigir(cep, "CEP");
        uf = uf.trim().toUpperCase();
        cep = cep.replaceAll("\\D", "");
        complemento = complemento == null ? "" : complemento.trim();
        validarFormato(uf.length() == 2, "UF deve ter 2 letras");
        validarFormato(cep.length() == 8, "CEP deve ter 8 dígitos");
    }

    private static void exigir(String campo, String nome) {
        validarFormato(campo != null && !campo.isBlank(), "Endereço: " + nome + " é obrigatório");
    }

    private static void validarFormato(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RegraDeNegocioException(mensagem);
        }
    }
}
