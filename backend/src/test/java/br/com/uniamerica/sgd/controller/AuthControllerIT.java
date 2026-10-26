package br.com.uniamerica.sgd.controller;

import br.com.uniamerica.sgd.TestcontainersConfiguration;
import br.com.uniamerica.sgd.client.FaculdadeGateway;
import br.com.uniamerica.sgd.client.UsuarioFaculdade;
import br.com.uniamerica.sgd.entity.Perfil;
import br.com.uniamerica.sgd.exception.CredenciaisInvalidasException;
import br.com.uniamerica.sgd.exception.ServicoExternoIndisponivelException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "sgd.jwt.secret=segredo-de-teste-com-pelo-menos-32-bytes")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FaculdadeGateway faculdadeGateway;

    @Test
    void deveResponder200ComTokenNoLoginValido() throws Exception {
        when(faculdadeGateway.autenticar("admin", "admin123"))
                .thenReturn(new UsuarioFaculdade("admin", "Administrador", Perfil.ADMIN));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin\",\"senha\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("Administrador"))
                .andExpect(jsonPath("$.perfil").value("ADMIN"));
    }

    @Test
    void deveResponder401QuandoCredenciaisInvalidas() throws Exception {
        when(faculdadeGateway.autenticar(anyString(), anyString()))
                .thenThrow(new CredenciaisInvalidasException("login ou senha invalidos"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void deveResponder400QuandoCamposVazios() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deveResponder503QuandoApiFaculdadeFora() throws Exception {
        when(faculdadeGateway.autenticar(anyString(), anyString()))
                .thenThrow(new ServicoExternoIndisponivelException("API da faculdade fora"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin\",\"senha\":\"admin123\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}
