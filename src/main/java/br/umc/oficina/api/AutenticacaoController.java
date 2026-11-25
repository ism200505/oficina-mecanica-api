package br.umc.oficina.api;

import br.umc.oficina.aplicacao.AutenticacaoService;
import br.umc.oficina.api.dto.LoginRequest;
import br.umc.oficina.api.dto.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AutenticacaoController {

    private final AutenticacaoService autenticacao;

    public AutenticacaoController(AutenticacaoService autenticacao) {
        this.autenticacao = autenticacao;
    }

    /** POST /api/v1/auth/login -> 200 com o token | 401 credenciais inválidas | 429 bloqueado. */
    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest credenciais) {
        return autenticacao.autenticar(credenciais);
    }
}
