package br.umc.oficina.api;

import br.umc.oficina.aplicacao.OrdemServicoService;
import br.umc.oficina.api.dto.AlterarStatusRequest;
import br.umc.oficina.api.dto.OrdemServicoRequest;
import br.umc.oficina.api.dto.OrdemServicoResponse;
import br.umc.oficina.dominio.ordem.StatusOrdemServico;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ordens-servico")
public class OrdemServicoController {

    private final OrdemServicoService ordens;

    public OrdemServicoController(OrdemServicoService ordens) {
        this.ordens = ordens;
    }

    @PostMapping
    public ResponseEntity<OrdemServicoResponse> abrir(@RequestBody @Valid OrdemServicoRequest dados) {
        OrdemServicoResponse criada = OrdemServicoResponse.de(ordens.abrir(dados));
        return ResponseEntity.created(Localizacao.doRecurso(criada.id())).body(criada);
    }

    /** GET /api/v1/ordens-servico?status=EM_EXECUCAO (filtro opcional). */
    @GetMapping
    public List<OrdemServicoResponse> listar(@RequestParam(required = false) StatusOrdemServico status) {
        return ordens.listar(status).stream().map(OrdemServicoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public OrdemServicoResponse buscar(@PathVariable String id) {
        return OrdemServicoResponse.de(ordens.buscarPorId(id));
    }

    /** PATCH = alteração parcial: só o status muda (e o cliente é notificado automaticamente). */
    @PatchMapping("/{id}/status")
    public OrdemServicoResponse alterarStatus(@PathVariable String id, @RequestBody @Valid AlterarStatusRequest dados) {
        return OrdemServicoResponse.de(ordens.alterarStatus(id, dados.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable String id) {
        ordens.remover(id);
        return ResponseEntity.noContent().build();
    }
}
