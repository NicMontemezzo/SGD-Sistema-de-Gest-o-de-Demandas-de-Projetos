package br.com.uniamerica.sgd.dto;

import br.com.uniamerica.sgd.entity.Perfil;

public record LoginResponse(String token, String nome, Perfil perfil) {
}
