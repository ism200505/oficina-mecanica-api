package br.umc.oficina.infra.firebase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Envelope de leitura/escrita de documentos do Firestore (Map&lt;String,Object&gt;).
 * Datas são gravadas em ISO-8601 e valores monetários como texto para não perder precisão.
 */
public final class Documento {

    private final Map<String, Object> dados;

    private Documento(Map<String, Object> dados) {
        this.dados = dados;
    }

    public static Documento novo() {
        return new Documento(new LinkedHashMap<>());
    }

    public static Documento de(Map<String, Object> dados) {
        return new Documento(dados == null ? new LinkedHashMap<>() : dados);
    }

    public Documento com(String campo, Object valor) {
        dados.put(campo, converter(valor));
        return this;
    }

    private static Object converter(Object valor) {
        if (valor instanceof LocalDate || valor instanceof LocalDateTime || valor instanceof Enum<?>) {
            return valor.toString();
        }
        if (valor instanceof Documento documento) {
            return documento.dados;
        }
        return valor;
    }

    public String texto(String campo) {
        Object valor = dados.get(campo);
        return valor == null ? null : valor.toString();
    }

    public long numero(String campo) {
        Object valor = dados.get(campo);
        return valor == null ? 0L : ((Number) valor).longValue();
    }

    public boolean logico(String campo) {
        return Boolean.TRUE.equals(dados.get(campo));
    }

    public LocalDate data(String campo) {
        String valor = texto(campo);
        return valor == null ? null : LocalDate.parse(valor);
    }

    public LocalDateTime dataHora(String campo) {
        String valor = texto(campo);
        return valor == null ? null : LocalDateTime.parse(valor);
    }

    public <E extends Enum<E>> E enumeracao(String campo, Class<E> tipo) {
        String valor = texto(campo);
        return valor == null ? null : Enum.valueOf(tipo, valor);
    }

    @SuppressWarnings("unchecked")
    public Documento subdocumento(String campo) {
        return Documento.de((Map<String, Object>) dados.get(campo));
    }

    @SuppressWarnings("unchecked")
    public List<String> listaDeTextos(String campo) {
        Object valor = dados.get(campo);
        return valor == null ? List.of() : (List<String>) valor;
    }

    public Map<String, Object> comoMapa() {
        return dados;
    }
}
