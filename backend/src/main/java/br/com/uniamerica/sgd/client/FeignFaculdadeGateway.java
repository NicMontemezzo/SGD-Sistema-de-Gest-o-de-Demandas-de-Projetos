package br.com.uniamerica.sgd.client;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import br.com.uniamerica.sgd.client.FaculdadeFeignClient.FaculdadeLoginRequest;
import br.com.uniamerica.sgd.client.FaculdadeFeignClient.FaculdadeLoginResponse;
import br.com.uniamerica.sgd.entity.Perfil;
import br.com.uniamerica.sgd.exception.CredenciaisInvalidasException;
import br.com.uniamerica.sgd.exception.ServicoExternoIndisponivelException;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementação real do {@link FaculdadeGateway}, ativa fora do perfil {@code mock}.
 * Traduz os erros HTTP da API da faculdade para as exceções do domínio.
 */
@Slf4j
@Component
@Profile("!mock")
@RequiredArgsConstructor
public class FeignFaculdadeGateway implements FaculdadeGateway {

    static final String MENSAGEM_CREDENCIAIS_INVALIDAS = "Login ou senha inválidos";
    static final String MENSAGEM_INDISPONIVEL = "API da faculdade indisponível. Tente novamente mais tarde.";

    private final FaculdadeFeignClient faculdadeFeignClient;

    @Override
    public UsuarioFaculdade autenticar(String login, String senha) {
        FaculdadeLoginResponse resposta;
        try {
            resposta = faculdadeFeignClient.autenticar(new FaculdadeLoginRequest(login, senha));
        } catch (FeignException.Unauthorized | FeignException.Forbidden | FeignException.BadRequest ex) {
            log.warn("API da faculdade recusou o login '{}' (status {})", login, ex.status());
            throw new CredenciaisInvalidasException(MENSAGEM_CREDENCIAIS_INVALIDAS);
        } catch (RetryableException ex) {
            log.error("Sem conexão com a API da faculdade: {}", ex.getMessage());
            throw new ServicoExternoIndisponivelException(MENSAGEM_INDISPONIVEL, ex);
        } catch (FeignException ex) {
            log.error("API da faculdade respondeu com status {} ao autenticar '{}'", ex.status(), login);
            throw new ServicoExternoIndisponivelException(MENSAGEM_INDISPONIVEL, ex);
        }

        UsuarioFaculdade usuario = converter(resposta);
        log.info("Usuário '{}' autenticado pela API da faculdade com perfil {}", usuario.login(), usuario.perfil());
        return usuario;
    }

    private UsuarioFaculdade converter(FaculdadeLoginResponse resposta) {
        if (resposta == null || resposta.login() == null || resposta.perfil() == null) {
            log.error("API da faculdade devolveu resposta incompleta: {}", resposta);
            throw new ServicoExternoIndisponivelException(MENSAGEM_INDISPONIVEL);
        }
        try {
            Perfil perfil = Perfil.valueOf(resposta.perfil().trim().toUpperCase());
            return new UsuarioFaculdade(resposta.login(), resposta.nome(), perfil);
        } catch (IllegalArgumentException ex) {
            log.error("API da faculdade devolveu perfil desconhecido '{}' para '{}'", resposta.perfil(),
                    resposta.login());
            throw new ServicoExternoIndisponivelException(MENSAGEM_INDISPONIVEL, ex);
        }
    }
}
