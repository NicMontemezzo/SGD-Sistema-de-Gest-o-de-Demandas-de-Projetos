package br.com.uniamerica.sgd.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Cliente HTTP da API da faculdade.
 * A API real ainda não foi enviada: rota e corpos abaixo são provisórios e devem ser
 * ajustados quando a documentação chegar (só esta classe e os records Faculdade* mudam).
 */
@FeignClient(name = "faculdade", url = "${sgd.faculdade.url}", configuration = FaculdadeFeignConfig.class)
public interface FaculdadeFeignClient {

    @PostMapping("/auth/login")
    FaculdadeLoginResponse autenticar(@RequestBody FaculdadeLoginRequest request);

    record FaculdadeLoginRequest(String login, String senha) {
    }

    record FaculdadeLoginResponse(String login, String nome, String perfil) {
    }
}
