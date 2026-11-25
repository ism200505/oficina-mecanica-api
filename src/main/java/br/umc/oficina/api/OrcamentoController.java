package br.umc.oficina.api;

import br.umc.oficina.aplicacao.OrcamentoService;
import br.umc.oficina.api.dto.CatalogoResponse;
import br.umc.oficina.api.dto.OrcamentoRequest;
import br.umc.oficina.api.dto.OrcamentoResponse;
import br.umc.oficina.dominio.servico.Adicionais;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrcamentoController {

    private final OrcamentoService orcamentos;

    public OrcamentoController(OrcamentoService orcamentos) {
        this.orcamentos = orcamentos;
    }

    /** Simula um orçamento sem gravar nada (demonstra o Decorator + Strategy). */
    @PostMapping("/api/v1/orcamentos/simulacoes")
    public OrcamentoResponse simular(@RequestBody @Valid OrcamentoRequest dados) {
        return OrcamentoResponse.de(orcamentos.calcular(dados.servicoBase(),
                Adicionais.de(dados.adicionais()), dados.formaPagamento()));
    }

    @GetMapping("/api/v1/catalogo")
    public CatalogoResponse catalogo() {
        return CatalogoResponse.completo();
    }
}
