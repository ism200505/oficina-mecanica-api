package br.umc.oficina.dominio.servico;

import java.util.Objects;

/**
 * DECORATOR - papel "Decorator".
 * Mantém uma referência para um {@link Servico} (composição) e segue a mesma interface.
 * Implementado sem framework, conforme exigido na especificação.
 */
public abstract class ServicoDecorator implements Servico {

    protected final Servico servico;

    protected ServicoDecorator(Servico servico) {
        this.servico = Objects.requireNonNull(servico, "O serviço decorado é obrigatório");
    }

    @Override
    public abstract String descricao();
}
