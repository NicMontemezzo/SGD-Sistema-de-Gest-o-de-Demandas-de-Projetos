package br.com.uniamerica.sgd.client;

import br.com.uniamerica.sgd.client.FaculdadeFeignClient.FaculdadeLoginRequest;
import br.com.uniamerica.sgd.client.FaculdadeFeignClient.FaculdadeLoginResponse;
import br.com.uniamerica.sgd.entity.Perfil;
import br.com.uniamerica.sgd.exception.CredenciaisInvalidasException;
import br.com.uniamerica.sgd.exception.ServicoExternoIndisponivelException;
import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeignFaculdadeGatewayTest {

    private static final Request REQUEST = Request.create(Request.HttpMethod.POST,
            "http://localhost:9090/auth/login", Map.of(), null, StandardCharsets.UTF_8, null);

    @Mock
    private FaculdadeFeignClient faculdadeFeignClient;

    @InjectMocks
    private FeignFaculdadeGateway gateway;

    @Test
    void autenticar_enviaLoginESenhaEConverteResposta() {
        when(faculdadeFeignClient.autenticar(new FaculdadeLoginRequest("admin", "senha")))
                .thenReturn(new FaculdadeLoginResponse("admin", "Administrador", "ADMIN"));

        UsuarioFaculdade usuario = gateway.autenticar("admin", "senha");

        assertEquals(new UsuarioFaculdade("admin", "Administrador", Perfil.ADMIN), usuario);
        verify(faculdadeFeignClient).autenticar(new FaculdadeLoginRequest("admin", "senha"));
    }

    @Test
    void autenticar_aceitaPerfilMinusculoComEspacos() {
        when(faculdadeFeignClient.autenticar(any()))
                .thenReturn(new FaculdadeLoginResponse("ra123", "Aluno", " aluno "));

        assertEquals(Perfil.ALUNO, gateway.autenticar("ra123", "senha").perfil());
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 401, 403 })
    void autenticar_apiRecusaLogin_lancaCredenciaisInvalidas(int status) {
        when(faculdadeFeignClient.autenticar(any())).thenThrow(erroHttp(status));

        CredenciaisInvalidasException ex = assertThrows(CredenciaisInvalidasException.class,
                () -> gateway.autenticar("admin", "errada"));
        assertEquals(FeignFaculdadeGateway.MENSAGEM_CREDENCIAIS_INVALIDAS, ex.getMessage());
    }

    @Test
    void autenticar_semConexao_lancaServicoIndisponivelComCausa() {
        RetryableException semConexao = new RetryableException(-1, "Connection refused",
                Request.HttpMethod.POST, (Long) null, REQUEST);
        when(faculdadeFeignClient.autenticar(any())).thenThrow(semConexao);

        ServicoExternoIndisponivelException ex = assertIndisponivel();
        assertSame(semConexao, ex.getCause());
    }

    @ParameterizedTest
    @ValueSource(ints = { 404, 500, 503 })
    void autenticar_apiComErro_lancaServicoIndisponivelComCausa(int status) {
        FeignException erro = erroHttp(status);
        when(faculdadeFeignClient.autenticar(any())).thenThrow(erro);

        ServicoExternoIndisponivelException ex = assertIndisponivel();
        assertSame(erro, ex.getCause());
    }

    @Test
    void autenticar_respostaNula_lancaServicoIndisponivel() {
        when(faculdadeFeignClient.autenticar(any())).thenReturn(null);

        assertNull(assertIndisponivel().getCause());
    }

    @Test
    void autenticar_respostaSemLogin_lancaServicoIndisponivel() {
        when(faculdadeFeignClient.autenticar(any()))
                .thenReturn(new FaculdadeLoginResponse(null, "Administrador", "ADMIN"));

        assertIndisponivel();
    }

    @Test
    void autenticar_respostaSemPerfil_lancaServicoIndisponivel() {
        when(faculdadeFeignClient.autenticar(any()))
                .thenReturn(new FaculdadeLoginResponse("admin", "Administrador", null));

        assertIndisponivel();
    }

    @Test
    void autenticar_perfilDesconhecido_lancaServicoIndisponivel() {
        when(faculdadeFeignClient.autenticar(any()))
                .thenReturn(new FaculdadeLoginResponse("prof", "Professor", "PROFESSOR"));

        assertInstanceOf(IllegalArgumentException.class, assertIndisponivel().getCause());
    }

    private ServicoExternoIndisponivelException assertIndisponivel() {
        ServicoExternoIndisponivelException ex = assertThrows(ServicoExternoIndisponivelException.class,
                () -> gateway.autenticar("admin", "senha"));
        assertEquals(FeignFaculdadeGateway.MENSAGEM_INDISPONIVEL, ex.getMessage());
        return ex;
    }

    private static FeignException erroHttp(int status) {
        Response response = Response.builder()
                .status(status)
                .reason("erro " + status)
                .request(REQUEST)
                .headers(Map.of())
                .build();
        return FeignException.errorStatus("FaculdadeFeignClient#autenticar", response);
    }
}
