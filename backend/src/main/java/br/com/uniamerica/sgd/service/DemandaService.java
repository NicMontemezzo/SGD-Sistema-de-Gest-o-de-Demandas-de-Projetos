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
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemandaService {

    private final DemandaRepository demandaRepository;
    private final EmpresaRepository empresaRepository;

    @Transactional
    public DemandaResponse criar(DemandaRequest request) {
        log.info("Criando demanda '{}' para a empresa {}", request.titulo(), request.empresaId());

        Empresa empresa = buscarEmpresa(request.empresaId());

        if (demandaRepository.existsByTituloAndEmpresaId(request.titulo(), request.empresaId())) {
            throw new RegraNegocioException("Já existe uma demanda com este título para a empresa");
        }

        Demanda demanda = new Demanda();
        preencher(demanda, request, empresa);
        demanda.setSituacao(request.situacao() != null ? request.situacao() : SituacaoDemanda.DISPONIVEL);

        Demanda salva = demandaRepository.save(demanda);
        log.info("Demanda {} criada", salva.getId());
        return DemandaResponse.from(salva);
    }

    @Transactional
    public DemandaResponse atualizar(Long id, DemandaRequest request) {
        log.info("Atualizando demanda {}", id);

        Demanda demanda = demandaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Demanda não encontrada: " + id));

        if (demanda.getSituacao() == SituacaoDemanda.ENCERRADA) {
            throw new RegraNegocioException("Demanda encerrada não pode ser editada");
        }

        if (!Objects.equals(request.versao(), demanda.getVersao())) {
            throw new ObjectOptimisticLockingFailureException(Demanda.class, id);
        }

        Empresa empresa = buscarEmpresa(request.empresaId());
        preencher(demanda, request, empresa);
        if (request.situacao() != null) {
            demanda.setSituacao(request.situacao());
        }

        Demanda salva = demandaRepository.save(demanda);
        log.info("Demanda {} atualizada", salva.getId());
        return DemandaResponse.from(salva);
    }

    @Transactional(readOnly = true)
    public Page<DemandaResponse> listar(String titulo, SituacaoDemanda situacao, Long empresaId, Pageable pageable) {
        log.info("Listando demandas (título='{}', situação={}, empresa={})", titulo, situacao, empresaId);
        return demandaRepository.findAll(filtrar(titulo, situacao, empresaId), pageable)
                .map(DemandaResponse::from);
    }

    @Transactional(readOnly = true)
    public DemandaResponse buscarPorId(Long id) {
        log.info("Buscando demanda {}", id);
        Demanda demanda = demandaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Demanda não encontrada: " + id));
        return DemandaResponse.from(demanda);
    }

    @Transactional
    public void excluir(Long id) {
        log.info("Excluindo demanda {}", id);
        Demanda demanda = demandaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Demanda não encontrada: " + id));
        demandaRepository.delete(demanda);
        log.info("Demanda {} excluída", id);
    }

    // cada filtro só entra na consulta se foi informado
    private Specification<Demanda> filtrar(String titulo, SituacaoDemanda situacao, Long empresaId) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            if (titulo != null && !titulo.isBlank()) {
                predicados.add(cb.like(cb.lower(root.get("titulo")), "%" + titulo.trim().toLowerCase() + "%"));
            }
            if (situacao != null) {
                predicados.add(cb.equal(root.get("situacao"), situacao));
            }
            if (empresaId != null) {
                predicados.add(cb.equal(root.get("empresa").get("id"), empresaId));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    private Empresa buscarEmpresa(Long empresaId) {
        return empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa não encontrada: " + empresaId));
    }

    private void preencher(Demanda demanda, DemandaRequest request, Empresa empresa) {
        demanda.setTitulo(request.titulo());
        demanda.setDescricao(request.descricao());
        demanda.setTecnologias(request.tecnologias());
        demanda.setEmpresa(empresa);
    }
}
