package br.umc.oficina.aplicacao.observador;

import br.umc.oficina.dominio.ordem.OrdemServico;

/**
 * OBSERVER - papel "Observer".
 * Quem implementa é avisado sempre que uma ordem de serviço é aberta ou muda de status.
 * Novas automações = novas classes que implementam esta interface (OCP).
 */
public interface OuvinteDeStatus {

    void statusAlterado(OrdemServico ordem);
}
