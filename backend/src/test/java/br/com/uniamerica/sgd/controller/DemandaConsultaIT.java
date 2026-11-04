package br.com.uniamerica.sgd.controller;

import br.com.uniamerica.sgd.TestcontainersConfiguration;
import br.com.uniamerica.sgd.entity.Demanda;
import br.com.uniamerica.sgd.entity.Empresa;
import br.com.uniamerica.sgd.entity.SituacaoDemanda;
import br.com.uniamerica.sgd.repository.DemandaRepository;
import br.com.uniamerica.sgd.repository.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "sgd.jwt.secret=segredo-de-teste-com-pelo-menos-32-bytes")
class DemandaConsultaIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private DemandaRepository demandaRepository;
    @Autowired private EmpresaRepository empresaRepository;

    private Empresa acme;
    private Empresa globex;

    @BeforeEach
    void preparar() {
        demandaRepository.deleteAll();
        empresaRepository.deleteAll();
        acme = salvarEmpresa("Acme", "12345678000195");
        globex = salvarEmpresa("Globex", "98765432000110");
    }

    private Empresa salvarEmpresa(String nome, String cnpj) {
        Empresa e = new Empresa();
        e.setNome(nome);
        e.setCnpj(cnpj);
        return empresaRepository.save(e);
    }

    private Demanda salvarDemanda(String titulo, Empresa empresa, SituacaoDemanda situacao) {
        Demanda d = new Demanda();
        d.setTitulo(titulo);
        d.setDescricao("Descrição");
        d.setEmpresa(empresa);
        d.setSituacao(situacao);
        return demandaRepository.save(d);
    }

    // ---------- GET /api/demandas ----------

    @Test
    @WithMockUser(roles = "ALUNO")
    void listar_paginaOsResultados() throws Exception {
        for (int i = 1; i <= 5; i++) {
            salvarDemanda("Demanda " + i, acme, SituacaoDemanda.DISPONIVEL);
        }

        mockMvc.perform(get("/api/demandas").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));

        mockMvc.perform(get("/api/demandas").param("page", "2").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void listar_filtraPorTituloSemDiferenciarMaiusculas() throws Exception {
        salvarDemanda("Sistema de Estoque", acme, SituacaoDemanda.DISPONIVEL);
        salvarDemanda("Portal do Aluno", acme, SituacaoDemanda.DISPONIVEL);

        mockMvc.perform(get("/api/demandas").param("titulo", "estoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Sistema de Estoque"));
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void listar_filtraPorSituacao() throws Exception {
        salvarDemanda("Demanda aberta", acme, SituacaoDemanda.DISPONIVEL);
        salvarDemanda("Demanda encerrada", acme, SituacaoDemanda.ENCERRADA);

        mockMvc.perform(get("/api/demandas").param("situacao", "ENCERRADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Demanda encerrada"));
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void listar_filtraPorEmpresa() throws Exception {
        salvarDemanda("Demanda da Acme", acme, SituacaoDemanda.DISPONIVEL);
        salvarDemanda("Demanda da Globex", globex, SituacaoDemanda.DISPONIVEL);

        mockMvc.perform(get("/api/demandas").param("empresaId", globex.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].empresaNome").value("Globex"));
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void listar_semResultado_retornaPaginaVazia() throws Exception {
        salvarDemanda("Sistema de Estoque", acme, SituacaoDemanda.DISPONIVEL);

        mockMvc.perform(get("/api/demandas").param("titulo", "nao-existe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void listar_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/demandas"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- GET /api/demandas/{id} ----------

    @Test
    @WithMockUser(roles = "ALUNO")
    void buscarPorId_existente_retorna200() throws Exception {
        Demanda d = salvarDemanda("Sistema de Estoque", acme, SituacaoDemanda.DISPONIVEL);

        mockMvc.perform(get("/api/demandas/" + d.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(d.getId()))
                .andExpect(jsonPath("$.titulo").value("Sistema de Estoque"))
                .andExpect(jsonPath("$.empresaNome").value("Acme"));
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void buscarPorId_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/demandas/999999"))
                .andExpect(status().isNotFound());
    }
}
