package br.com.uniamerica.sgd.repository;

import br.com.uniamerica.sgd.entity.Demanda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DemandaRepository extends JpaRepository<Demanda, Long>, JpaSpecificationExecutor<Demanda> {

    // RN-02: "Sistema X" e "sistema x" contam como o mesmo título
    boolean existsByTituloIgnoreCaseAndEmpresaId(String titulo, Long empresaId);

    // mesma verificação na edição, ignorando a própria demanda
    boolean existsByTituloIgnoreCaseAndEmpresaIdAndIdNot(String titulo, Long empresaId, Long id);

    // traz a empresa junto na mesma consulta, evitando uma consulta extra por demanda da página
    @Override
    @EntityGraph(attributePaths = "empresa")
    Page<Demanda> findAll(Specification<Demanda> spec, Pageable pageable);
}
