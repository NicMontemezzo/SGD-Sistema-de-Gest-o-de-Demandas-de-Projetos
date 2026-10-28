package br.com.uniamerica.sgd.exception;

import br.com.uniamerica.sgd.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confere, de ponta a ponta, que todo erro da API sai no formato {@link ErrorResponse}
 * com o status certo e sem stack trace.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "sgd.jwt.secret=segredo-de-teste-com-pelo-menos-32-bytes")
class ErroRespostaIT {

    private static final String DATA_HORA = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void semToken_responde401NoFormatoPadrao() throws Exception {
        assertFormato(mockMvc.perform(get("/api/demandas")), 401, "Unauthorized",
                GlobalExceptionHandler.MENSAGEM_NAO_AUTENTICADO, "/api/demandas");
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void alunoExcluindo_responde403NoFormatoPadrao() throws Exception {
        assertFormato(mockMvc.perform(delete("/api/demandas/1")), 403, "Forbidden",
                GlobalExceptionHandler.MENSAGEM_ACESSO_NEGADO, "/api/demandas/1");
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void demandaInexistente_responde404() throws Exception {
        String path = "/api/demandas/" + Long.MAX_VALUE;

        mockMvc.perform(get(path))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void corpoInvalido_responde400ComDetalhePorCampo() throws Exception {
        assertFormato(mockMvc.perform(post("/api/demandas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"descricao\":\"Descrição\"}")),
                400, "Bad Request", GlobalExceptionHandler.MENSAGEM_VALIDACAO, "/api/demandas", false)
                .andExpect(jsonPath("$.details", containsInAnyOrder(
                        "titulo: O título é obrigatório",
                        "titulo: O título deve ter entre 5 e 120 caracteres",
                        "empresaId: A empresa é obrigatória")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void jsonMalformado_responde400() throws Exception {
        assertFormato(mockMvc.perform(post("/api/demandas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":")),
                400, "Bad Request", GlobalExceptionHandler.MENSAGEM_JSON_MALFORMADO, "/api/demandas");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void tipoDeMidiaNaoSuportado_responde415() throws Exception {
        mockMvc.perform(post("/api/demandas")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("texto"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.path").value("/api/demandas"));
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void idNaoNumerico_responde400() throws Exception {
        assertFormato(mockMvc.perform(get("/api/demandas/abc")), 400, "Bad Request",
                "Valor inválido para o parâmetro 'id'", "/api/demandas/abc");
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void situacaoInvalida_responde400() throws Exception {
        assertFormato(mockMvc.perform(get("/api/demandas").param("situacao", "INVALIDA")), 400, "Bad Request",
                "Valor inválido para o parâmetro 'situacao'", "/api/demandas");
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void ordenacaoPorCampoInexistente_responde400() throws Exception {
        assertFormato(mockMvc.perform(get("/api/demandas").param("sort", "campoQueNaoExiste")), 400,
                "Bad Request", "Campo de ordenação inválido: 'campoQueNaoExiste'", "/api/demandas");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void metodoNaoSuportado_responde405() throws Exception {
        assertFormato(mockMvc.perform(patch("/api/demandas/1")), 405, "Method Not Allowed",
                "Método PATCH não suportado nesta rota", "/api/demandas/1");
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void rotaInexistente_responde404() throws Exception {
        assertFormato(mockMvc.perform(get("/api/rota-que-nao-existe")), 404, "Not Found",
                "Rota não encontrada", "/api/rota-que-nao-existe");
    }

    private ResultActions assertFormato(ResultActions resultado, int status, String error, String mensagem,
            String path) throws Exception {
        return assertFormato(resultado, status, error, mensagem, path, true);
    }

    private ResultActions assertFormato(ResultActions resultado, int status, String error, String mensagem,
            String path, boolean semDetalhes) throws Exception {
        resultado
                .andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp", matchesPattern(DATA_HORA)))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.error").value(error))
                .andExpect(jsonPath("$.message").value(mensagem))
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
        if (semDetalhes) {
            resultado.andExpect(jsonPath("$.details").isEmpty());
        }
        return resultado;
    }
}
