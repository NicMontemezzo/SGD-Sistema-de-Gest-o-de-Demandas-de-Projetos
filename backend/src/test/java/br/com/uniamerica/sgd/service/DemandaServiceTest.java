package br.com.uniamerica.sgd.service;

import br.com.uniamerica.sgd.dto.DemandaRequest;
import br.com.uniamerica.sgd.dto.DemandaResponse;
import br.com.uniamerica.sgd.entity.Demanda;
import br.com.uniamerica.sgd.entity.Empresa;
import br.com.uniamerica.sgd.entity.SituacaoDemanda;
import br.com.uniamerica.sgd.exception.RecursoNaoEncontradoException;
import br.com.uniamerica.sgd.exception.RegraNegocioException;
import br.com.uniamerica.sgd.repository.DemandaRepository;
import br.com.uniamerica.sgd.repository.EmpresaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemandaServiceTest {

    @Mock
    private DemandaRepository demandaRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @InjectMocks
    private DemandaService demandaService;

    private Empresa empresa() {
        Empresa e = new Empresa();          // ajuste se a Empresa do Integrante 3 não tiver construtor/setters
        e.setId(1L);
        e.setNome("Acme");
        return e;
    }

    private DemandaRequest request(Long empresaId, Long versao) {
        return new DemandaRequest("Sistema de estoque", "Descrição", "Java, Angular",
                empresaId, null, versao);
    }

    private Demanda demandaExistente(SituacaoDemanda situacao) {
        Demanda d = new Demanda();
        d.setId(10L);
        d.setTitulo("Antigo título");
        d.setDescricao("Antiga");
        d.setEmpresa(empresa());
        d.setSituacao(situacao);
        d.setVersao(0L);
        return d;
    }

    @Test
    void deveCriarDemandaComDadosValidos() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa()));
        when(demandaRepository.existsByTituloAndEmpresaId("Sistema de estoque", 1L)).thenReturn(false);
        when(demandaRepository.save(any(Demanda.class))).thenAnswer(inv -> {
            Demanda d = inv.getArgument(0);
            d.setId(10L);
            return d;
        });

        DemandaResponse resposta = demandaService.criar(request(1L, null));

        assertEquals(10L, resposta.id());
        assertEquals("Sistema de estoque", resposta.titulo());
        assertEquals(SituacaoDemanda.DISPONIVEL, resposta.situacao());
        verify(demandaRepository).save(any(Demanda.class));
    }

    @Test
    void deveRespeitarSituacaoInformadaAoCriar() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa()));
        when(demandaRepository.existsByTituloAndEmpresaId(any(), any())).thenReturn(false);
        when(demandaRepository.save(any(Demanda.class))).thenAnswer(inv -> inv.getArgument(0));
        DemandaRequest req = new DemandaRequest("Sistema de estoque", "Desc", null, 1L,
                SituacaoDemanda.EM_ANDAMENTO, null);

        DemandaResponse resposta = demandaService.criar(req);

        assertEquals(SituacaoDemanda.EM_ANDAMENTO, resposta.situacao());
    }

    @Test
    void deveLancarExcecaoQuandoEmpresaNaoExiste() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> demandaService.criar(request(99L, null)));
        verify(demandaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoTituloRepetidoNaMesmaEmpresa() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa()));
        when(demandaRepository.existsByTituloAndEmpresaId("Sistema de estoque", 1L)).thenReturn(true);

        assertThrows(RegraNegocioException.class,
                () -> demandaService.criar(request(1L, null)));
        verify(demandaRepository, never()).save(any());
    }

    @Test
    void deveAtualizarDemandaComSucesso() {
        Demanda existente = demandaExistente(SituacaoDemanda.DISPONIVEL);
        when(demandaRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa()));
        when(demandaRepository.save(any(Demanda.class))).thenAnswer(inv -> inv.getArgument(0));
        DemandaRequest req = new DemandaRequest("Novo título", "Nova desc", "Spring", 1L,
                SituacaoDemanda.EM_ANDAMENTO, 0L);

        DemandaResponse resposta = demandaService.atualizar(10L, req);

        assertEquals("Novo título", resposta.titulo());
        assertEquals(SituacaoDemanda.EM_ANDAMENTO, resposta.situacao());
        verify(demandaRepository).save(existente);
    }

    @Test
    void deveManterSituacaoQuandoNaoInformadaNaAtualizacao() {
        Demanda existente = demandaExistente(SituacaoDemanda.EM_ANDAMENTO);
        when(demandaRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa()));
        when(demandaRepository.save(any(Demanda.class))).thenAnswer(inv -> inv.getArgument(0));

        DemandaResponse resposta = demandaService.atualizar(10L, request(1L, 0L));

        assertEquals(SituacaoDemanda.EM_ANDAMENTO, resposta.situacao());
    }

    @Test
    void deveLancarExcecaoQuandoAtualizarDemandaInexistente() {
        when(demandaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> demandaService.atualizar(10L, request(1L, 0L)));
        verify(demandaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoAtualizarDemandaEncerrada() {
        when(demandaRepository.findById(10L))
                .thenReturn(Optional.of(demandaExistente(SituacaoDemanda.ENCERRADA)));

        assertThrows(RegraNegocioException.class,
                () -> demandaService.atualizar(10L, request(1L, 0L)));
        verify(demandaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoVersaoDesatualizada() {
        when(demandaRepository.findById(10L))
                .thenReturn(Optional.of(demandaExistente(SituacaoDemanda.DISPONIVEL)));

        assertThrows(ObjectOptimisticLockingFailureException.class,
                () -> demandaService.atualizar(10L, request(1L, 5L)));
        verify(demandaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoAtualizarComEmpresaInexistente() {
        when(demandaRepository.findById(10L))
                .thenReturn(Optional.of(demandaExistente(SituacaoDemanda.DISPONIVEL)));
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> demandaService.atualizar(10L, request(99L, 0L)));
    }
}
