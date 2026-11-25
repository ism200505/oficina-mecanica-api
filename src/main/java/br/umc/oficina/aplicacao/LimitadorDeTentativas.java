package br.umc.oficina.aplicacao;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mitigação de ataque de força bruta no login: após 5 senhas erradas seguidas,
 * o login fica bloqueado por 5 minutos.
 */
@Component
public class LimitadorDeTentativas {

    static final int MAXIMO_DE_FALHAS = 5;
    static final Duration TEMPO_DE_BLOQUEIO = Duration.ofMinutes(5);

    private final Map<String, Tentativas> registros = new ConcurrentHashMap<>();
    private final Clock relogio;

    public LimitadorDeTentativas(Clock relogio) {
        this.relogio = relogio;
    }

    public void verificar(String login) {
        Tentativas tentativas = registros.getOrDefault(login, Tentativas.NENHUMA);
        if (tentativas.bloqueadoEm(relogio.instant())) {
            throw new LoginBloqueadoException();
        }
    }

    public void registrarFalha(String login) {
        registros.merge(login, Tentativas.NENHUMA.comMaisUmaFalha(relogio.instant()),
                (atual, nova) -> atual.comMaisUmaFalha(relogio.instant()));
    }

    public void registrarSucesso(String login) {
        registros.remove(login);
    }

    private record Tentativas(int falhas, Instant bloqueadoAte) {

        static final Tentativas NENHUMA = new Tentativas(0, Instant.MIN);

        Tentativas comMaisUmaFalha(Instant agora) {
            int total = falhas + 1;
            Instant bloqueio = total >= MAXIMO_DE_FALHAS ? agora.plus(TEMPO_DE_BLOQUEIO) : bloqueadoAte;
            return new Tentativas(total, bloqueio);
        }

        boolean bloqueadoEm(Instant agora) {
            return agora.isBefore(bloqueadoAte);
        }
    }
}
