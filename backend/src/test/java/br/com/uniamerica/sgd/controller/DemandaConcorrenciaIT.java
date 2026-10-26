package br.com.uniamerica.sgd.controller;

import br.com.uniamerica.sgd.TestcontainersConfiguration;
import br.com.uniamerica.sgd.entity.Demanda;
import br.com.uniamerica.sgd.entity.Empresa;
import br.com.uniamerica.sgd.repository.DemandaRepository;
import br.com.uniamerica.sgd.repository.EmpresaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * RN-04: duas edições da mesma demanda com a mesma versão. Só a primeira grava (200), a outra recebe 409.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "sgd.jwt.secret=segredo-de-teste-com-pelo-menos-32-bytes")
class DemandaConcorrenciaIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private DemandaRepository demandaRepository;
    @Autowired private EmpresaRepository empresaRepository;

    @Test
    void duasEdicoesComAMesmaVersao_umaRecebe200_eOutraRecebe409() throws Exception {
        demandaRepository.deleteAll();
        empresaRepository.deleteAll();

        Empresa empresa = new Empresa();
        empresa.setNome("Acme");
        empresa.setCnpj("12345678000195");
        empresa = empresaRepository.save(empresa);

        Demanda demanda = new Demanda();
        demanda.setTitulo("Sistema de Estoque");
        demanda.setDescricao("Descrição");
        demanda.setEmpresa(empresa);
        demanda = demandaRepository.save(demanda);

        Long id = demanda.getId();
        Long versao = demanda.getVersao();
        Long empresaId = empresa.getId();

        // as duas threads esperam o sinal e disparam juntas
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<Integer>> resultados = new ArrayList<>();

        for (String titulo : List.of("Título editado A", "Título editado B")) {
            Callable<Integer> edicao = () -> {
                largada.await();
                return mockMvc.perform(put("/api/demandas/" + id)
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(corpo(titulo, empresaId, versao)))
                        .andReturn().getResponse().getStatus();
            };
            resultados.add(executor.submit(edicao));
        }

        largada.countDown();

        int ok = 0;
        int conflito = 0;
        for (Future<Integer> resultado : resultados) {
            int status = resultado.get();
            if (status == 200) {
                ok++;
            } else if (status == 409) {
                conflito++;
            }
        }
        executor.shutdown();

        assertEquals(1, ok, "exatamente uma edição deve gravar");
        assertEquals(1, conflito, "a outra deve receber 409");
    }

    private String corpo(String titulo, Long empresaId, Long versao) throws Exception {
        Map<String, Object> m = new HashMap<>();
        m.put("titulo", titulo);
        m.put("descricao", "Descrição");
        m.put("empresaId", empresaId);
        m.put("versao", versao);
        return objectMapper.writeValueAsString(m);
    }
}
