package br.com.uniamerica.sgd.controller;

import br.com.uniamerica.sgd.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "sgd.jwt.secret=segredo-de-teste-com-pelo-menos-32-bytes")
class EmpresaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ALUNO")
    void deveListarEmpresasDaMigrationParaAluno() throws Exception {
        // a lista vem da V2__insert_empresas.sql: se não estiver vazia, a migration rodou
        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].nome").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveListarEmpresasParaAdmin() throws Exception {
        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isOk());
    }

    @Test
    void deveResponder401NoFormatoErrorResponseSemToken() throws Exception {
        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/empresas"));
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void naoDeveTrazerDemandasDaEmpresaNaResposta() throws Exception {
        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].demandas").doesNotExist());
    }
}
