package br.umc.oficina.api;

import br.umc.oficina.aplicacao.ClienteService;
import br.umc.oficina.api.dto.ClienteRequest;
import br.umc.oficina.api.dto.ClienteResponse;
import br.umc.oficina.api.dto.VeiculoResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clientes;
    private final Clock relogio;

    public ClienteController(ClienteService clientes, Clock relogio) {
        this.clientes = clientes;
        this.relogio = relogio;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@RequestBody @Valid ClienteRequest dados) {
        ClienteResponse criado = ClienteResponse.de(clientes.cadastrar(dados));
        return ResponseEntity.created(Localizacao.doRecurso(criado.id())).body(criado);
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return clientes.listar().stream().map(ClienteResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable String id) {
        return ClienteResponse.de(clientes.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable String id, @RequestBody @Valid ClienteRequest dados) {
        return ClienteResponse.de(clientes.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable String id) {
        clientes.remover(id);
        return ResponseEntity.noContent().build();
    }

    /** Sub-recurso do relacionamento 1:N: GET /api/v1/clientes/{id}/veiculos */
    @GetMapping("/{id}/veiculos")
    public List<VeiculoResponse> veiculos(@PathVariable String id) {
        LocalDate hoje = LocalDate.now(relogio);
        return clientes.veiculosDoCliente(id).stream().map(v -> VeiculoResponse.de(v, hoje)).toList();
    }
}
