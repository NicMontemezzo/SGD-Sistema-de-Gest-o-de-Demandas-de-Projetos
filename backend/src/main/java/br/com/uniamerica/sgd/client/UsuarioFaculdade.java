package br.com.uniamerica.sgd.client;

import br.com.uniamerica.sgd.entity.Perfil;

/**
 * Usuário autenticado pela API da faculdade. É o que o AuthService recebe para gerar o JWT.
 */
public record UsuarioFaculdade(String login, String nome, Perfil perfil) {
}
