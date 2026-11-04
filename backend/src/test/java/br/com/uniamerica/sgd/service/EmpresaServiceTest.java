package br.com.uniamerica.sgd.service;

import br.com.uniamerica.sgd.dto.EmpresaResponse;
import br.com.uniamerica.sgd.entity.Empresa;
import br.com.uniamerica.sgd.exception.RecursoNaoEncontradoException;
import br.com.uniamerica.sgd.repository.EmpresaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private EmpresaService empresaService;

    private Empresa empresa(Long id, String nome) {
        Empresa e = new Empresa();
        e.setId(id);
        e.setNome(nome);
        e.setCnpj("00.000.000/0001-0" + id);
        return e;
    }

    @Test
    void deveListarEmpresasEmOrdemAlfabetica() {
        // preparar
        when(empresaRepository.findAllByOrderByNomeAsc())
                .thenReturn(List.of(empresa(2L, "Acme"), empresa(1L, "Beta")));

        // agir
        List<EmpresaResponse> empresas = empresaService.listar();

        // conferir
        assertEquals(List.of("Acme", "Beta"), empresas.stream().map(EmpresaResponse::nome).toList());
        verify(empresaRepository).findAllByOrderByNomeAsc();
    }

    @Test
    void deveDevolverListaVaziaQuandoNaoHaEmpresas() {
        when(empresaRepository.findAllByOrderByNomeAsc()).thenReturn(List.of());

        List<EmpresaResponse> empresas = empresaService.listar();

        assertTrue(empresas.isEmpty());
    }

    @Test
    void deveBuscarEmpresaExistente() {
        Empresa acme = empresa(1L, "Acme");
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(acme));

        Empresa encontrada = empresaService.buscarPorId(1L);

        assertSame(acme, encontrada);
        verify(empresaRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoQuandoEmpresaNaoExiste() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException ex = assertThrows(RecursoNaoEncontradoException.class,
                () -> empresaService.buscarPorId(99L));

        assertEquals("Empresa 99 não encontrada", ex.getMessage());
    }
}
