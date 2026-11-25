package br.umc.oficina.dominio.servico;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Coleção de primeira classe (Calistenia de Objetos) para os adicionais escolhidos.
 * Garante que não haja repetição e preserva a ordem de escolha.
 * A garantia estendida é sempre aplicada por último, para incidir sobre o total.
 */
public final class Adicionais {

    private final List<Adicional> itens;

    private Adicionais(Collection<Adicional> itens) {
        LinkedHashSet<Adicional> semRepeticao = new LinkedHashSet<>(itens);
        boolean temGarantia = semRepeticao.remove(Adicional.GARANTIA_ESTENDIDA);
        List<Adicional> ordenados = new ArrayList<>(semRepeticao);
        adicionarGarantiaNoFinal(ordenados, temGarantia);
        this.itens = Collections.unmodifiableList(ordenados);
    }

    private static void adicionarGarantiaNoFinal(List<Adicional> lista, boolean temGarantia) {
        if (temGarantia) {
            lista.add(Adicional.GARANTIA_ESTENDIDA);
        }
    }

    public static Adicionais de(Collection<Adicional> itens) {
        return new Adicionais(itens == null ? List.of() : itens);
    }

    public static Adicionais nenhum() {
        return new Adicionais(List.of());
    }

    /** Aplica, em cadeia, cada decorador sobre o serviço base. */
    public Servico decorar(Servico base) {
        Servico resultado = base;
        for (Adicional adicional : itens) {
            resultado = adicional.decorar(resultado);
        }
        return resultado;
    }

    public List<Adicional> comoLista() {
        return itens;
    }

    public List<String> nomes() {
        return itens.stream().map(Enum::name).toList();
    }
}
