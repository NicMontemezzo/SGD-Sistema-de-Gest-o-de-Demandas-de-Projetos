package br.com.uniamerica.sgd.service;

import br.com.uniamerica.sgd.dto.DemandaResponse;
import br.com.uniamerica.sgd.entity.Demanda;
import br.com.uniamerica.sgd.entity.Empresa;
import br.com.uniamerica.sgd.entity.SituacaoDemanda;
import br.com.uniamerica.sgd.exception.RecursoNaoEncontradoException;
import br.com.uniamerica.sgd.repository.DemandaRepository;
import br.com.uniamerica.sgd.repository.EmpresaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Pra atuivar o Mokito
@ExtendWith(MockitoExtension.class)
class DemandaServiceListagemTest {

    // demandarepository falsa
    @Mock
    private DemandaRepository demandaRepository;

    // Empresarepository falsa
    @Mock
    private EmpresaRepository empresarepository;

    @InjectMocks
    private DemandaService demandaService;

    //montagem das demandas

    private Demanda criarDemanda(Long id, String titulo) {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNome("Empresa de teste");

        Demanda demanda = new Demanda();
        demanda.setId(id);
        demanda.setTitulo(titulo);
        demanda.setDescricao("descrição");
        demanda.setEmpresa(empresa);
        return demanda;
    }

    // Se buscarPorId existente

    @Test
    void buscarPorId_existente_retornaResponse() {
        //dado

        Demanda demanda = criarDemanda(1L, "API de pagamentos");
        when(demandaRepository.findById(1L)).thenReturn(Optional.of(demanda));

        //Quando
        DemandaResponse resposta = demandaService.buscarPorId(1L);

        //então
        assertEquals(1L,resposta.id());
        assertEquals("API de pagamentos", resposta.titulo());
        verify(demandaRepository).findById(1L);
    }

    // Se buscaPorId inexistente

    @Test
    void buscarPorId_inexistente_lancaExcecao() {
        when(demandaRepository.findById(99L)).thenReturn(Optional.empty());

        // assertThrows passa se o código dentro da lambda lançar essa exceção.
        assertThrows(RecursoNaoEncontradoException.class,
                () -> demandaService.buscarPorId(99L));
    }

    // Listar sem os filtro

    @Test
    void listar_semFiltros_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Demanda> pagina = new PageImpl<>(List.of(
                criarDemanda(1L, "Demanda um"), criarDemanda(2L, "Demanda dois")));
        when(demandaRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(pagina);

        Page<DemandaResponse> resultado = demandaService.listar(null, null, null, pageable);

        assertEquals(2, resultado.getTotalElements());
        assertEquals("Demanda um", resultado.getContent().get(0).titulo());
    }

    // Listar com os filtros

    @Test
    void listar_comFiltros_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Demanda> pagina = new PageImpl<>(List.of(
                criarDemanda(1L, "Demanda um"), criarDemanda(2L, "Demanda dois")));
        when(demandaRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(pagina);

        Page<DemandaResponse> resultado = demandaService.listar("api", SituacaoDemanda.DISPONIVEL, 1L, pageable);

        assertEquals(2, resultado.getTotalElements());
        assertEquals("Demanda um", resultado.getContent().get(0).titulo());
        verify(demandaRepository).findAll(any(Specification.class), eq(pageable));
    }
}
