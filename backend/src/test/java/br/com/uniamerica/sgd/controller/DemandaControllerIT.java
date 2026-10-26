package br.com.uniamerica.sgd.controller;

import br.com.uniamerica.sgd.TestcontainersConfiguration;
import br.com.uniamerica.sgd.entity.Demanda;
import br.com.uniamerica.sgd.entity.Empresa;
import br.com.uniamerica.sgd.entity.SituacaoDemanda;
import br.com.uniamerica.sgd.repository.DemandaRepository;
import br.com.uniamerica.sgd.repository.EmpresaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "sgd.jwt.secret=segredo-de-teste-com-pelo-menos-32-bytes")
class DemandaControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private DemandaRepository demandaRepository;
    @Autowired private EmpresaRepository empresaRepository;

    private Empresa empresa;

    @BeforeEach
    void preparar() {
        demandaRepository.deleteAll();
        empresa = new Empresa();
        empresa.setNome("Acme");
        empresa.setCnpj("12345678000195");
        empresa = empresaRepository.save(empresa);
    }

    @AfterEach
    void limpar() {
        demandaRepository.deleteAll();
        empresaRepository.deleteById(empresa.getId());
    }

    private String json(String titulo, String descricao, Long empresaId, Long versao) throws Exception {
        Map<String, Object> m = new HashMap<>();
        m.put("titulo", titulo);
        m.put("descricao", descricao);
        m.put("tecnologias", "Java");
        m.put("empresaId", empresaId);
        m.put("versao", versao);
        return objectMapper.writeValueAsString(m);
    }

    private ResultActions postar(String body) throws Exception {
        return mockMvc.perform(post("/api/demandas").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions atualizar(Long id, String body) throws Exception {
        return mockMvc.perform(put("/api/demandas/" + id).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private Demanda salvarDemanda(String titulo, SituacaoDemanda situacao) {
        Demanda d = new Demanda();
        d.setTitulo(titulo);
        d.setDescricao("Descrição");
        d.setEmpresa(empresa);
        d.setSituacao(situacao);
        return demandaRepository.save(d);
    }

    // ---------- POST ----------
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCriaDemanda201() throws Exception {
        postar(json("Sistema de estoque", "Desc", empresa.getId(), null))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Sistema de estoque"))
                .andExpect(jsonPath("$.situacao").value("DISPONIVEL"));

        assertEquals(1, demandaRepository.count());
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void alunoNaoCria403() throws Exception {
        postar(json("Sistema de estoque", "Desc", empresa.getId(), null))
                .andExpect(status().isForbidden());
    }

    @Test
    void semTokenNaoCria401() throws Exception {
        postar(json("Sistema de estoque", "Desc", empresa.getId(), null))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void tituloCurtoRetorna400ComDetails() throws Exception {
        postar(json("abc", "Desc", empresa.getId(), null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void camposVaziosRetorna400ComDetails() throws Exception {
        postar("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void empresaInexistenteRetorna404() throws Exception {
        postar(json("Sistema de estoque", "Desc", 999999L, null))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void tituloRepetidoNaEmpresaRetorna400() throws Exception {
        salvarDemanda("Sistema de estoque", SituacaoDemanda.DISPONIVEL);

        postar(json("Sistema de estoque", "Desc", empresa.getId(), null))
                .andExpect(status().isBadRequest());
    }

    // ---------- PUT ----------
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminAtualizaDemanda200() throws Exception {
        Demanda d = salvarDemanda("Título antigo", SituacaoDemanda.DISPONIVEL);

        atualizar(d.getId(), json("Título novo", "Nova desc", empresa.getId(), d.getVersao()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Título novo"));

        assertEquals("Título novo", demandaRepository.findById(d.getId()).orElseThrow().getTitulo());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void atualizarIdInexistenteRetorna404() throws Exception {
        atualizar(999999L, json("Título novo", "Desc", empresa.getId(), 0L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void atualizarDemandaEncerradaRetorna400() throws Exception {
        Demanda d = salvarDemanda("Título antigo", SituacaoDemanda.ENCERRADA);

        atualizar(d.getId(), json("Título novo", "Desc", empresa.getId(), d.getVersao()))
                .andExpect(status().isBadRequest());
    }
}