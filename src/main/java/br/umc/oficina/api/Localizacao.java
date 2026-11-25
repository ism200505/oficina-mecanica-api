package br.umc.oficina.api;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/** Monta o cabeçalho Location das respostas 201 Created (padrão REST). */
final class Localizacao {

    private Localizacao() {
    }

    static URI doRecurso(String id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
