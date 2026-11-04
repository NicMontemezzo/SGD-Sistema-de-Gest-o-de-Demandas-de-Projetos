package br.com.uniamerica.sgd.security;

import br.com.uniamerica.sgd.TestcontainersConfiguration;
import br.com.uniamerica.sgd.client.UsuarioFaculdade;
import br.com.uniamerica.sgd.entity.Perfil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "sgd.jwt.secret=segredo-de-teste-com-pelo-menos-32-bytes")
class SegurancaIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void deveResponder401SemToken() throws Exception {
        mockMvc.perform(get("/api/demandas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void deveResponder401ComTokenAdulterado() throws Exception {
        mockMvc.perform(get("/api/demandas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-adulterado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void deveResponder403QuandoAlunoTentaCriarDemanda() throws Exception {
        String tokenAluno = jwtUtil.gerarToken(new UsuarioFaculdade("ra123", "Aluno", Perfil.ALUNO));

        mockMvc.perform(post("/api/demandas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
