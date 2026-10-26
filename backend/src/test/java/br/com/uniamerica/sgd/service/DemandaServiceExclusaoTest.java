package br.com.uniamerica.sgd.service;

import br.com.uniamerica.sgd.entity.Demanda;
import br.com.uniamerica.sgd.exception.RecursoNaoEncontradoException;
import br.com.uniamerica.sgd.repository.DemandaRepository;
import br.com.uniamerica.sgd.repository.EmpresaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemandaServiceExclusaoTest {

    @Mock
    private DemandaRepository demandaRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private DemandaService demandaService;

    //happy path

    @Test
    void excluir_existente_chamaDeleteUmaVez() {
        // dado
        Demanda demanda = new Demanda();
        demanda.setId(1L);
        when(demandaRepository.findById(1L)).thenReturn(Optional.of(demanda));

        //quando
        demandaService.excluir(1L);

        // então
        verify(demandaRepository, times(1)).delete(demanda);
    }

    @Test
    void excluir_inexistente_lancaExcecaoENaoApaga() {
        // dado
        when(demandaRepository.findById(99L)).thenReturn(Optional.empty());

        // quando e então
        assertThrows(RecursoNaoEncontradoException.class,
                () -> demandaService.excluir(99L));

        //nunca chamou delete
        verify(demandaRepository, never()).delete(any(Demanda.class));
    }
}
