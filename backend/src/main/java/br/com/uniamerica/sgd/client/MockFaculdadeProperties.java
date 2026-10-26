package br.com.uniamerica.sgd.client;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import br.com.uniamerica.sgd.entity.Perfil;

/**
 * Usuários do mock da API da faculdade, definidos em application-mock.yml.
 * As senhas vêm de variáveis de ambiente, nunca do código.
 */
@ConfigurationProperties(prefix = "sgd.mock")
public record MockFaculdadeProperties(List<Usuario> usuarios) {

    public MockFaculdadeProperties {
        usuarios = usuarios == null ? List.of() : List.copyOf(usuarios);
    }

    public record Usuario(String login, String nome, Perfil perfil, String senha) {
    }
}
