package br.umc.oficina;

import br.umc.oficina.api.dto.ClienteRequest;
import br.umc.oficina.api.dto.EnderecoDto;
import br.umc.oficina.api.dto.OrdemServicoRequest;
import br.umc.oficina.api.dto.VeiculoRequest;
import br.umc.oficina.dominio.cliente.CanalNotificacao;
import br.umc.oficina.dominio.cliente.Cliente;
import br.umc.oficina.dominio.comum.Cpf;
import br.umc.oficina.dominio.comum.Dinheiro;
import br.umc.oficina.dominio.comum.Email;
import br.umc.oficina.dominio.comum.Placa;
import br.umc.oficina.dominio.comum.Quilometragem;
import br.umc.oficina.dominio.comum.Telefone;
import br.umc.oficina.dominio.ordem.Orcamento;
import br.umc.oficina.dominio.ordem.OrdemServico;
import br.umc.oficina.dominio.ordem.StatusOrdemServico;
import br.umc.oficina.dominio.pagamento.TipoPagamento;
import br.umc.oficina.dominio.servico.Adicionais;
import br.umc.oficina.dominio.servico.Adicional;
import br.umc.oficina.dominio.servico.TipoServicoBase;
import br.umc.oficina.dominio.veiculo.TipoCombustivel;
import br.umc.oficina.dominio.veiculo.Veiculo;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/** Dados fictícios reutilizados pelos testes (etapa "Arrange"). */
public final class Fixtures {

    /** Relógio fixo: 24/11/2026 09:00 em São Paulo - testes não dependem da data real. */
    public static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-24T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    public static final LocalDate HOJE = LocalDate.now(RELOGIO);
    public static final LocalDateTime AGORA = LocalDateTime.now(RELOGIO);

    public static final String CPF_VALIDO = "529.982.247-25";
    public static final String OUTRO_CPF_VALIDO = "111.444.777-35";

    private Fixtures() {
    }

    public static EnderecoDto endereco() {
        return new EnderecoDto("Rua das Flores", "123", "", "Centro", "Jacareí", "SP", "12300-000");
    }

    public static ClienteRequest clienteRequest(String cpf) {
        return new ClienteRequest("Maria da Silva", cpf, "maria@email.com", "(12) 99876-5432",
                LocalDate.of(1990, 5, 20), endereco(), CanalNotificacao.WHATSAPP, null);
    }

    public static Cliente cliente(String id, CanalNotificacao canal) {
        return new Cliente(id, "Maria da Silva", new Cpf(CPF_VALIDO), new Email("maria@email.com"),
                new Telefone("12998765432"), LocalDate.of(1990, 5, 20), endereco().paraDominio(), canal,
                LocalDate.of(2026, 1, 10), true, "");
    }

    public static Veiculo veiculo(String id, String clienteId, long km, LocalDate dataUltimaRevisao, long kmUltimaRevisao) {
        return new Veiculo(id, clienteId, new Placa("BRA2E19"), "Volkswagen", "Gol", 2019, 2020, "Prata",
                "9BWZZZ377VT004251", TipoCombustivel.FLEX, new Quilometragem(km), dataUltimaRevisao,
                new Quilometragem(kmUltimaRevisao), null);
    }

    public static VeiculoRequest veiculoRequest(String clienteId, String placa, long km) {
        return new VeiculoRequest(clienteId, placa, "Volkswagen", "Gol", 2019, 2020, "Prata",
                "9BWZZZ377VT004251", TipoCombustivel.FLEX, km, null, null);
    }

    public static OrdemServicoRequest ordemRequest(String veiculoId, long km) {
        return new OrdemServicoRequest(veiculoId, "Barulho ao frear", "João Mecânico", km,
                TipoServicoBase.REVISAO_PREVENTIVA, List.of(Adicional.TROCA_DE_OLEO), TipoPagamento.PIX);
    }

    public static OrdemServico ordem(String id, StatusOrdemServico status, TipoServicoBase base) {
        Orcamento orcamento = new Orcamento("Revisão preventiva", Dinheiro.de("250.00"), Dinheiro.de("237.50"),
                3, TipoPagamento.PIX);
        return new OrdemServico(id, "OS-20261124-ABC123", "cli-1", "vei-1", "Revisão dos 60 mil",
                "João Mecânico", new Quilometragem(60_000), base, Adicionais.nenhum(), orcamento, status,
                AGORA, AGORA.plusHours(3), null);
    }
}
