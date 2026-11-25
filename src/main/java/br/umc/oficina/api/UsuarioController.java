package br.umc.oficina.api;

import br.umc.oficina.aplicacao.UsuarioService;
import br.umc.oficina.api.dto.UsuarioRequest;
import br.umc.oficina.api.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Somente ADMIN (ver SecurityConfig). */
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarios;

    public UsuarioController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@RequestBody @Valid UsuarioRequest dados) {
        UsuarioResponse criado = UsuarioResponse.de(usuarios.cadastrar(dados));
        return ResponseEntity.created(Localizacao.doRecurso(criado.id())).body(criado);
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarios.listar().stream().map(UsuarioResponse::de).toList();
    }
}
