package br.umc.oficina.seguranca;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/** Lê o cabeçalho "Authorization: Bearer &lt;token&gt;" e autentica a requisição. */
public class JwtFiltroAutenticacao extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;

    public JwtFiltroAutenticacao(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta,
                                    FilterChain cadeia) throws ServletException, IOException {
        extrairToken(requisicao)
                .flatMap(jwtService::validar)
                .ifPresent(this::autenticar);
        cadeia.doFilter(requisicao, resposta);
    }

    private Optional<String> extrairToken(HttpServletRequest requisicao) {
        return Optional.ofNullable(requisicao.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(cabecalho -> cabecalho.startsWith(PREFIXO))
                .map(cabecalho -> cabecalho.substring(PREFIXO.length()));
    }

    private void autenticar(Claims dados) {
        String papel = dados.get("papel", String.class);
        UsernamePasswordAuthenticationToken autenticacao = new UsernamePasswordAuthenticationToken(
                dados.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_" + papel)));
        SecurityContextHolder.getContext().setAuthentication(autenticacao);
    }
}
