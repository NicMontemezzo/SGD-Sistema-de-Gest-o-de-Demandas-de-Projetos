package br.com.uniamerica.sgd.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import br.com.uniamerica.sgd.exception.CredenciaisInvalidasException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Simula a API da faculdade enquanto ela não chega. Ativo só no perfil {@code mock}.
 * Usuários configurados em application-mock.yml.
 */
@Slf4j
@Component
@Profile("mock")
@EnableConfigurationProperties(MockFaculdadeProperties.class)
@RequiredArgsConstructor
public class MockFaculdadeGateway implements FaculdadeGateway {

    static final String MENSAGEM_CREDENCIAIS_INVALIDAS = "Login ou senha inválidos";

    private final MockFaculdadeProperties properties;

    @Override
    public UsuarioFaculdade autenticar(String login, String senha) {
        if (login == null || senha == null) {
            throw new CredenciaisInvalidasException(MENSAGEM_CREDENCIAIS_INVALIDAS);
        }
        return properties.usuarios().stream()
                .filter(usuario -> login.equalsIgnoreCase(usuario.login()))
                .filter(usuario -> senhaConfere(usuario.senha(), senha))
                .findFirst()
                .map(usuario -> {
                    log.info("[mock] Usuário '{}' autenticado com perfil {}", usuario.login(), usuario.perfil());
                    return new UsuarioFaculdade(usuario.login(), usuario.nome(), usuario.perfil());
                })
                .orElseThrow(() -> {
                    log.warn("[mock] Falha de login para '{}'", login);
                    return new CredenciaisInvalidasException(MENSAGEM_CREDENCIAIS_INVALIDAS);
                });
    }

    /** Senha não configurada nunca autentica; comparação em tempo constante. */
    private static boolean senhaConfere(String senhaConfigurada, String senhaInformada) {
        if (senhaConfigurada == null || senhaConfigurada.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                senhaConfigurada.getBytes(StandardCharsets.UTF_8),
                senhaInformada.getBytes(StandardCharsets.UTF_8));
    }
}
