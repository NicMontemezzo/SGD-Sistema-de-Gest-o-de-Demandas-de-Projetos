package br.com.uniamerica.sgd.controller;

import br.com.uniamerica.sgd.TestcontainersConfiguration;
import br.com.uniamerica.sgd.entity.Demanda;
import br.com.uniamerica.sgd.entity.Empresa;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "sgd.jwt.secret=segredo-de-teste-com-pelo-menos-32-bytes")
class DemandaExclusaoIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private DemandaRepository demandaRepository;
    @Autowired private EmpresaRepository empresaRepository;

    private Demanda demanda;

    @BeforeEach
    void preparar() {
        demandaRepository.deleteAll();
        empresaRepository.deleteAll();

        Empresa empresa = new Empresa();
        empresa.setNome("Acme");
        empresa.setCnpj("12345678000195");
        empresa = empresaRepository.save(empresa);

        demanda = new Demanda();
        demanda.setTitulo("Sistema de Estoque");
        demanda.setDescricao("Descrição");
        demanda.setEmpresa(empresa);
        demanda = demandaRepository.save(demanda);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void excluir_comAdmin_retorna204EApagaDoBanco() throws Exception {
        mockMvc.perform(delete("/api/demandas/" + demanda.getId()))
                .andExpect(status().isNoContent());

        assertFalse(demandaRepository.existsById(demanda.getId()));
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void excluir_comAluno_retorna403ENaoApaga() throws Exception {
        mockMvc.perform(delete("/api/demandas/" + demanda.getId()))
                .andExpect(status().isForbidden());

        assertTrue(demandaRepository.existsById(demanda.getId()));
    }

    @Test
    void excluir_semToken_retorna401() throws Exception {
        mockMvc.perform(delete("/api/demandas/" + demanda.getId()))
                .andExpect(status().isUnauthorized());

        assertTrue(demandaRepository.existsById(demanda.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void excluir_inexistente_retorna404() throws Exception {
        mockMvc.perform(delete("/api/demandas/999999"))
                .andExpect(status().isNotFound());
    }
}
