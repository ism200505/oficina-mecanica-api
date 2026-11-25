package br.umc.oficina.api;

import br.umc.oficina.aplicacao.VeiculoService;
import br.umc.oficina.api.dto.OrdemServicoResponse;
import br.umc.oficina.api.dto.VeiculoRequest;
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
@RequestMapping("/api/v1/veiculos")
public class VeiculoController {

    private final VeiculoService veiculos;
    private final Clock relogio;

    public VeiculoController(VeiculoService veiculos, Clock relogio) {
        this.veiculos = veiculos;
        this.relogio = relogio;
    }

    @PostMapping
    public ResponseEntity<VeiculoResponse> cadastrar(@RequestBody @Valid VeiculoRequest dados) {
        VeiculoResponse criado = VeiculoResponse.de(veiculos.cadastrar(dados), hoje());
        return ResponseEntity.created(Localizacao.doRecurso(criado.id())).body(criado);
    }

    @GetMapping
    public List<VeiculoResponse> listar() {
        LocalDate hoje = hoje();
        return veiculos.listar().stream().map(v -> VeiculoResponse.de(v, hoje)).toList();
    }

    @GetMapping("/{id}")
    public VeiculoResponse buscar(@PathVariable String id) {
        return VeiculoResponse.de(veiculos.buscarPorId(id), hoje());
    }

    @PutMapping("/{id}")
    public VeiculoResponse atualizar(@PathVariable String id, @RequestBody @Valid VeiculoRequest dados) {
        return VeiculoResponse.de(veiculos.atualizar(id, dados), hoje());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable String id) {
        veiculos.remover(id);
        return ResponseEntity.noContent().build();
    }

    /** Sub-recurso do relacionamento 1:N: GET /api/v1/veiculos/{id}/ordens-servico */
    @GetMapping("/{id}/ordens-servico")
    public List<OrdemServicoResponse> ordensDeServico(@PathVariable String id) {
        return veiculos.ordensDoVeiculo(id).stream().map(OrdemServicoResponse::de).toList();
    }

    private LocalDate hoje() {
        return LocalDate.now(relogio);
    }
}
