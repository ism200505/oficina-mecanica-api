package br.umc.oficina.aplicacao.observador;

import br.umc.oficina.dominio.ordem.OrdemServico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OBSERVER - papel "Subject".
 * Recebe todos os ouvintes por injeção (lista de abstrações) e os avisa.
 * Falha em um ouvinte não impede os demais nem desfaz a operação principal.
 */
@Component
public class PublicadorDeStatus {

    private static final Logger LOG = LoggerFactory.getLogger(PublicadorDeStatus.class);

    private final List<OuvinteDeStatus> ouvintes;

    public PublicadorDeStatus(List<OuvinteDeStatus> ouvintes) {
        this.ouvintes = List.copyOf(ouvintes);
    }

    public void publicar(OrdemServico ordem) {
        ouvintes.forEach(ouvinte -> avisar(ouvinte, ordem));
    }

    private void avisar(OuvinteDeStatus ouvinte, OrdemServico ordem) {
        try {
            ouvinte.statusAlterado(ordem);
        } catch (RuntimeException erro) {
            LOG.error("Falha no ouvinte {} para a OS {}", ouvinte.getClass().getSimpleName(), ordem.numero(), erro);
        }
    }
}
