package br.umc.oficina.seguranca;

import br.umc.oficina.aplicacao.GeradorDeToken;
import br.umc.oficina.dominio.usuario.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/** Emissão e validação de tokens JWT assinados com HMAC-SHA256. */
@Service
public class JwtService implements GeradorDeToken {

    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    private final SecretKey chave;
    private final long validadeEmMinutos;
    private final Clock relogio;

    public JwtService(@Value("${seguranca.jwt.segredo}") String segredo,
                      @Value("${seguranca.jwt.expiracao-minutos}") long validadeEmMinutos,
                      Clock relogio) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException("O segredo JWT (JWT_SEGREDO) deve ter pelo menos 32 caracteres");
        }
        this.chave = Keys.hmacShaKeyFor(bytes);
        this.validadeEmMinutos = validadeEmMinutos;
        this.relogio = relogio;
    }

    @Override
    public String gerar(Usuario usuario) {
        Instant agora = relogio.instant();
        return Jwts.builder()
                .subject(usuario.login())
                .claim("papel", usuario.papel().name())
                .claim("nome", usuario.nome())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(validadeEmMinutos, ChronoUnit.MINUTES)))
                .signWith(chave)
                .compact();
    }

    @Override
    public long validadeEmMinutos() {
        return validadeEmMinutos;
    }

    /** Devolve as informações do token se a assinatura e a validade estiverem corretas. */
    public Optional<Claims> validar(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException tokenInvalido) {
            return Optional.empty();
        }
    }
}
