package br.umc.oficina.infra.config;

import br.umc.oficina.aplicacao.ClienteService;
import br.umc.oficina.aplicacao.UsuarioService;
import br.umc.oficina.aplicacao.VeiculoService;
import br.umc.oficina.api.dto.ClienteRequest;
import br.umc.oficina.api.dto.EnderecoDto;
import br.umc.oficina.api.dto.VeiculoRequest;
import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.cliente.Cliente;
import br.umc.oficina.dominio.veiculo.TipoCombustivel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Na inicialização: cria o administrador (se não existir) e, opcionalmente,
 * dados de demonstração (um cliente e um veículo com revisão vencida).
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DadosIniciais.class);

    private final UsuarioService usuarios;
    private final ClienteService clientes;
    private final VeiculoService veiculos;
    private final Clock relogio;
    private final String loginAdmin;
    private final String senhaAdmin;
    private final boolean popularDemonstracao;

    public DadosIniciais(UsuarioService usuarios, ClienteService clientes, VeiculoService veiculos, Clock relogio,
                         @Value("${oficina.admin.login}") String loginAdmin,
                         @Value("${oficina.admin.senha}") String senhaAdmin,
                         @Value("${oficina.demo.popular-dados:false}") boolean popularDemonstracao) {
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.veiculos = veiculos;
        this.relogio = relogio;
        this.loginAdmin = loginAdmin;
        this.senhaAdmin = senhaAdmin;
        this.popularDemonstracao = popularDemonstracao;
    }

    @Override
    public void run(String... args) {
        if (usuarios.garantirAdministrador(loginAdmin, senhaAdmin)) {
            LOG.warn("Usuário administrador '{}' criado. Troque a senha padrão em produção (ADMIN_SENHA).", loginAdmin);
        }
        if (popularDemonstracao && clientes.listar().isEmpty()) {
            popular();
        }
    }

    private void popular() {
        Cliente cliente = clientes.cadastrar(new ClienteRequest("Maria da Silva", "529.982.247-25",
                "maria.silva@email.com", "(12) 99876-5432", LocalDate.of(1990, 5, 20),
                new EnderecoDto("Rua das Flores", "123", "Casa 2", "Centro", "Jacareí", "SP", "12300-000"),
                CanalNotificacao.WHATSAPP, "Cliente de demonstração"));
        LocalDate revisaoAntiga = LocalDate.now(relogio).minusMonths(8);
        veiculos.cadastrar(new VeiculoRequest(cliente.id(), "BRA2E19", "Volkswagen", "Gol", 2019, 2020, "Prata",
                "9BWZZZ377VT004251", TipoCombustivel.FLEX, 58_000L, revisaoAntiga, 50_000L));
        LOG.info("Dados de demonstração criados (cliente Maria da Silva / veículo BRA2E19).");
    }
}
