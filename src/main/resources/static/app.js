// Front-end simples (JavaScript puro) que consome a API REST com o token JWT.
// Todo texto vindo da API é inserido com textContent (evita XSS).
"use strict";

const API = "/api/v1";
const PROXIMOS_STATUS = {
    ABERTA: ["EM_EXECUCAO", "CANCELADA"],
    EM_EXECUCAO: ["CONCLUIDA", "CANCELADA"],
    CONCLUIDA: ["ENTREGUE"],
    ENTREGUE: [],
    CANCELADA: []
};
const ROTULO_STATUS = {
    EM_EXECUCAO: "Iniciar", CANCELADA: "Cancelar", CONCLUIDA: "Concluir", ENTREGUE: "Entregar"
};

let sessao = lerSessao();
let catalogo = null;
let veiculosPorId = {};

// ---------- utilitários ----------
function lerSessao() {
    try { return JSON.parse(sessionStorage.getItem("oficina-sessao")); } catch (e) { return null; }
}
function gravarSessao(valor) {
    sessao = valor;
    try {
        if (valor) { sessionStorage.setItem("oficina-sessao", JSON.stringify(valor)); }
        else { sessionStorage.removeItem("oficina-sessao"); }
    } catch (e) { /* armazenamento indisponível: segue só em memória */ }
}

async function api(caminho, opcoes = {}) {
    const resposta = await fetch(API + caminho, {
        ...opcoes,
        headers: {
            "Content-Type": "application/json",
            ...(sessao ? { Authorization: "Bearer " + sessao.token } : {})
        },
        body: opcoes.body ? JSON.stringify(opcoes.body) : undefined
    });
    if (resposta.status === 401 && caminho !== "/auth/login") { sair(); throw new Error("Sessão expirada. Entre novamente."); }
    if (resposta.status === 204) { return null; }
    const corpo = await resposta.json().catch(() => null);
    if (!resposta.ok) {
        const campos = corpo && corpo.campos ? " " + Object.entries(corpo.campos).map(([c, m]) => c + ": " + m).join("; ") : "";
        throw new Error((corpo && corpo.detail ? corpo.detail : "Erro " + resposta.status) + campos);
    }
    return corpo;
}

function el(tag, propriedades = {}, ...filhos) {
    const elemento = document.createElement(tag);
    Object.entries(propriedades).forEach(([chave, valor]) => {
        if (chave === "onclick") { elemento.addEventListener("click", valor); }
        else { elemento[chave] = valor; }
    });
    filhos.forEach(f => elemento.append(f instanceof Node ? f : document.createTextNode(f ?? "")));
    return elemento;
}

function avisar(texto, ehErro = false) {
    const caixa = document.getElementById("mensagem");
    caixa.textContent = texto;
    caixa.className = "mensagem" + (ehErro ? " erro" : "");
    clearTimeout(avisar.timer);
    avisar.timer = setTimeout(() => caixa.classList.add("oculto"), 6000);
}

const moeda = v => Number(v).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
const dataBr = v => v ? new Date(v.length === 10 ? v + "T00:00:00" : v.slice(0, 19)).toLocaleString("pt-BR", v.length === 10 ? { dateStyle: "short" } : { dateStyle: "short", timeStyle: "short" }) : "—";
const dadosDo = form => Object.fromEntries(new FormData(form).entries());
const vazioParaNulo = v => (v === "" || v === undefined ? null : v);

function preencherSelect(select, itens, valor, texto) {
    select.replaceChildren(...itens.map(i => el("option", { value: valor(i) }, texto(i))));
}

function preencherAdicionais(container) {
    container.replaceChildren(...catalogo.adicionais.map(a =>
        el("label", {}, el("input", { type: "checkbox", value: a.codigo }), a.descricao)));
}
const adicionaisMarcados = container => [...container.querySelectorAll("input:checked")].map(c => c.value);

async function executar(acao, sucesso) {
    try { await acao(); if (sucesso) { avisar(sucesso); } }
    catch (erro) { avisar(erro.message, true); }
}

// ---------- login ----------
document.getElementById("form-login").addEventListener("submit", async evento => {
    evento.preventDefault();
    const erro = document.getElementById("erro-login");
    erro.textContent = "";
    try {
        gravarSessao(await api("/auth/login", { method: "POST", body: dadosDo(evento.target) }));
        iniciar();
    } catch (e) { erro.textContent = e.message; }
});

function sair() {
    gravarSessao(null);
    document.getElementById("tela-app").classList.add("oculto");
    document.getElementById("tela-login").classList.remove("oculto");
}
document.getElementById("sair").addEventListener("click", sair);

// ---------- navegação ----------
document.querySelectorAll("nav button").forEach(botao => botao.addEventListener("click", () => {
    document.querySelectorAll("nav button").forEach(b => b.classList.toggle("ativa", b === botao));
    document.querySelectorAll(".aba").forEach(a => a.classList.toggle("oculto", a.id !== "aba-" + botao.dataset.aba));
    carregarAba(botao.dataset.aba);
}));

function carregarAba(aba) {
    const carregadores = { ordens: carregarOrdens, clientes: carregarClientes, veiculos: carregarVeiculos, notificacoes: carregarNotificacoes };
    if (carregadores[aba]) { executar(carregadores[aba]); }
}

async function iniciar() {
    document.getElementById("tela-login").classList.add("oculto");
    document.getElementById("tela-app").classList.remove("oculto");
    document.getElementById("usuario-logado").textContent = sessao.nome + " (" + sessao.papel + ")";
    await executar(async () => {
        catalogo = await api("/catalogo");
        document.querySelectorAll("select[name=servicoBase]").forEach(s =>
            preencherSelect(s, catalogo.servicosBase, i => i.codigo, i => i.descricao + " — " + moeda(i.preco)));
        document.querySelectorAll("select[name=formaPagamento]").forEach(s =>
            preencherSelect(s, catalogo.formasPagamento, i => i.codigo, i => i.descricao));
        preencherAdicionais(document.getElementById("adicionais-ordem"));
        preencherAdicionais(document.getElementById("adicionais-orcamento"));
        await carregarOrdens();
    });
}

// ---------- clientes ----------
async function carregarClientes() {
    const clientes = await api("/clientes");
    document.getElementById("tabela-clientes").replaceChildren(...clientes.map(c => el("tr", {},
        el("td", {}, c.nome), el("td", {}, c.cpf), el("td", {}, c.telefone),
        el("td", {}, c.endereco.cidade + "/" + c.endereco.uf), el("td", {}, c.canalNotificacao),
        el("td", {},
            el("button", { className: "secundario", onclick: () => executar(() => verVeiculosDoCliente(c)) }, "Veículos"),
            el("button", { className: "perigo", onclick: () => executar(async () => { await api("/clientes/" + c.id, { method: "DELETE" }); await carregarClientes(); }, "Cliente removido") }, "Excluir")))));
    return clientes;
}

async function verVeiculosDoCliente(cliente) {
    const veiculos = await api("/clientes/" + cliente.id + "/veiculos");
    avisar(cliente.nome + " possui " + veiculos.length + " veículo(s): " + (veiculos.map(v => v.placa).join(", ") || "nenhum"));
}

document.getElementById("form-cliente").addEventListener("submit", evento => {
    evento.preventDefault();
    const d = dadosDo(evento.target);
    const corpo = {
        nome: d.nome, cpf: d.cpf, email: d.email, telefone: d.telefone, dataNascimento: d.dataNascimento,
        canalNotificacao: d.canalNotificacao, observacoes: d.observacoes,
        endereco: { logradouro: d.logradouro, numero: d.numero, complemento: d.complemento, bairro: d.bairro, cidade: d.cidade, uf: d.uf, cep: d.cep }
    };
    executar(async () => { await api("/clientes", { method: "POST", body: corpo }); evento.target.reset(); await carregarClientes(); }, "Cliente cadastrado");
});

// ---------- veículos ----------
async function carregarVeiculos() {
    const [veiculos, clientes] = await Promise.all([api("/veiculos"), api("/clientes")]);
    preencherSelect(document.querySelector("#form-veiculo select[name=clienteId]"), clientes, c => c.id, c => c.nome + " — " + c.cpf);
    document.getElementById("tabela-veiculos").replaceChildren(...veiculos.map(v => el("tr", {},
        el("td", {}, v.placa), el("td", {}, v.marca + " " + v.modelo + " " + v.anoModelo),
        el("td", {}, v.quilometragemAtual.toLocaleString("pt-BR")), el("td", {}, dataBr(v.dataUltimaRevisao)),
        el("td", {}, el("span", { className: "selo " + (v.revisaoPendente ? "vencida" : "em-dia") }, v.revisaoPendente ? "Vencida" : "Em dia")),
        el("td", {},
            el("button", { className: "secundario", onclick: () => executar(() => verHistorico(v)) }, "Histórico"),
            el("button", { className: "perigo", onclick: () => executar(async () => { await api("/veiculos/" + v.id, { method: "DELETE" }); await carregarVeiculos(); }, "Veículo removido") }, "Excluir")))));
    return veiculos;
}

async function verHistorico(veiculo) {
    const ordens = await api("/veiculos/" + veiculo.id + "/ordens-servico");
    avisar(veiculo.placa + ": " + ordens.length + " ordem(ns) de serviço. " + ordens.map(o => o.numero + " (" + o.status + ")").join(", "));
}

document.getElementById("form-veiculo").addEventListener("submit", evento => {
    evento.preventDefault();
    const d = dadosDo(evento.target);
    const corpo = {
        ...d, anoFabricacao: Number(d.anoFabricacao), anoModelo: Number(d.anoModelo),
        quilometragemAtual: Number(d.quilometragemAtual), dataUltimaRevisao: vazioParaNulo(d.dataUltimaRevisao),
        quilometragemUltimaRevisao: d.quilometragemUltimaRevisao ? Number(d.quilometragemUltimaRevisao) : null
    };
    executar(async () => { await api("/veiculos", { method: "POST", body: corpo }); evento.target.reset(); await carregarVeiculos(); }, "Veículo cadastrado");
});

// ---------- ordens de serviço ----------
async function carregarOrdens() {
    const [ordens, veiculos] = await Promise.all([api("/ordens-servico"), api("/veiculos")]);
    veiculosPorId = Object.fromEntries(veiculos.map(v => [v.id, v]));
    preencherSelect(document.querySelector("#form-ordem select[name=veiculoId]"), veiculos, v => v.id, v => v.placa + " — " + v.marca + " " + v.modelo);
    ordens.sort((a, b) => b.dataAbertura.localeCompare(a.dataAbertura));
    document.getElementById("tabela-ordens").replaceChildren(...ordens.map(linhaDaOrdem));
}

function linhaDaOrdem(o) {
    const veiculo = veiculosPorId[o.veiculoId];
    const botoes = PROXIMOS_STATUS[o.status].map(s => el("button", {
        className: s === "CANCELADA" ? "perigo" : "",
        onclick: () => executar(async () => {
            await api("/ordens-servico/" + o.id + "/status", { method: "PATCH", body: { status: s } });
            await carregarOrdens();
        }, "Status alterado para " + s + " — cliente notificado automaticamente")
    }, ROTULO_STATUS[s]));
    return el("tr", {},
        el("td", {}, o.numero), el("td", {}, veiculo ? veiculo.placa : "—"),
        el("td", {}, o.orcamento.descricao), el("td", {}, moeda(o.orcamento.valorFinal)),
        el("td", {}, dataBr(o.previsaoEntrega)), el("td", {}, el("span", { className: "selo " + o.status }, o.status)),
        el("td", {}, ...botoes));
}

document.getElementById("form-ordem").addEventListener("submit", evento => {
    evento.preventDefault();
    const d = dadosDo(evento.target);
    const corpo = {
        veiculoId: d.veiculoId, descricaoProblema: d.descricaoProblema, mecanicoResponsavel: d.mecanicoResponsavel,
        quilometragemEntrada: Number(d.quilometragemEntrada), servicoBase: d.servicoBase, formaPagamento: d.formaPagamento,
        adicionais: adicionaisMarcados(document.getElementById("adicionais-ordem"))
    };
    executar(async () => {
        const ordem = await api("/ordens-servico", { method: "POST", body: corpo });
        evento.target.reset();
        await carregarOrdens();
        avisar("OS " + ordem.numero + " aberta. Total: " + moeda(ordem.orcamento.valorFinal) + ". Cliente notificado.");
    });
});

// ---------- orçamento ----------
document.getElementById("form-orcamento").addEventListener("submit", evento => {
    evento.preventDefault();
    const d = dadosDo(evento.target);
    executar(async () => {
        const o = await api("/orcamentos/simulacoes", { method: "POST", body: {
            servicoBase: d.servicoBase, formaPagamento: d.formaPagamento,
            adicionais: adicionaisMarcados(document.getElementById("adicionais-orcamento")) } });
        const caixa = document.getElementById("resultado-orcamento");
        caixa.replaceChildren(
            el("h2", {}, "Resultado"), el("p", {}, o.descricao),
            el("p", {}, "Valor dos serviços: " + moeda(o.valorServicos)),
            el("p", { className: "valor" }, moeda(o.valorFinal)),
            el("p", { className: "sutil" }, "Prazo estimado: " + o.prazoEmHoras + " h · Pagamento: " + o.formaPagamento));
        caixa.classList.remove("oculto");
    });
});

// ---------- notificações ----------
async function carregarNotificacoes() {
    const notificacoes = await api("/notificacoes");
    document.getElementById("tabela-notificacoes").replaceChildren(...notificacoes.map(n => el("tr", {},
        el("td", {}, dataBr(n.dataHora)), el("td", {}, n.canal), el("td", {}, n.destino), el("td", {}, n.mensagem))));
}

document.getElementById("executar-lembretes").addEventListener("click", () => executar(async () => {
    const r = await api("/lembretes-revisao/execucoes", { method: "POST" });
    await carregarNotificacoes();
    avisar("Rotina executada: " + r.veiculosNotificados + " lembrete(s) de revisão enviado(s).");
}));

if (sessao) { iniciar(); }
