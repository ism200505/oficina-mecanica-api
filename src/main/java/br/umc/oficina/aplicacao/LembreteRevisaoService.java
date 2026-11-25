package br.umc.oficina.aplicacao;

import br.umc.oficina.dominio.cliente.Cliente;
import br.umc.oficina.dominio.repositorio.ClienteRepository;
import br.umc.oficina.dominio.repositorio.VeiculoRepository;
import br.umc.oficina.dominio.veiculo.Veiculo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

/**
 * AUTOMAÇÃO: todos os dias (agendamento configurável) verifica quais veículos estão com a
 * revisão vencida (6 meses ou 10.000 km) e envia um lembrete ao proprietário.
 */
@Service
public class LembreteRevisaoService {

    private static final Logger LOG = LoggerFactory.getLogger(LembreteRevisaoService.class);

    private final VeiculoRepository veiculos;
    private final ClienteRepository clientes;
    private final CentralDeNotificacoes central;
    private final Clock relogio;

    public LembreteRevisaoService(VeiculoRepository veiculos, ClienteRepository clientes,
                                  CentralDeNotificacoes central, Clock relogio) {
        this.veiculos = veiculos;
        this.clientes = clientes;
        this.central = central;
        this.relogio = relogio;
    }

    @Scheduled(cron = "${oficina.lembrete-revisao.cron}", zone = "America/Sao_Paulo")
    public int executar() {
        LocalDate hoje = LocalDate.now(relogio);
        int enviados = (int) veiculos.listarTodos().stream()
                .filter(veiculo -> veiculo.deveReceberLembrete(hoje))
                .filter(veiculo -> lembrar(veiculo, hoje))
                .count();
        LOG.info("Lembretes de revisão enviados: {}", enviados);
        return enviados;
    }

    private boolean lembrar(Veiculo veiculo, LocalDate hoje) {
        Optional<Cliente> dono = clientes.buscarPorId(veiculo.clienteId());
        dono.ifPresent(cliente -> {
            central.notificar(cliente, mensagem(cliente, veiculo));
            veiculo.registrarLembreteEnviado(hoje);
            veiculos.salvar(veiculo);
        });
        return dono.isPresent();
    }

    private String mensagem(Cliente cliente, Veiculo veiculo) {
        return "Olá, " + cliente.nome() + "! A revisão do seu " + veiculo.descricaoResumida()
                + " está vencida (a cada " + Veiculo.INTERVALO_REVISAO_MESES + " meses ou "
                + Veiculo.INTERVALO_REVISAO_KM + " km). Agende com a nossa oficina!";
    }
}
