package br.umc.oficina.api;

import br.umc.oficina.aplicacao.CentralDeNotificacoes;
import br.umc.oficina.aplicacao.LembreteRevisaoService;
import br.umc.oficina.api.dto.LembreteResponse;
import br.umc.oficina.api.dto.NotificacaoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NotificacaoController {

    private final CentralDeNotificacoes central;
    private final LembreteRevisaoService lembretes;

    public NotificacaoController(CentralDeNotificacoes central, LembreteRevisaoService lembretes) {
        this.central = central;
        this.lembretes = lembretes;
    }

    /** Histórico das notificações automáticas (opcionalmente de um cliente). */
    @GetMapping("/api/v1/notificacoes")
    public List<NotificacaoResponse> listar(@RequestParam(required = false) String clienteId) {
        return central.historico(clienteId).stream().map(NotificacaoResponse::de).toList();
    }

    /** Dispara manualmente a rotina automática de lembretes (que também roda sozinha todo dia). */
    @PostMapping("/api/v1/lembretes-revisao/execucoes")
    public LembreteResponse executarLembretes() {
        return new LembreteResponse(lembretes.executar());
    }
}
