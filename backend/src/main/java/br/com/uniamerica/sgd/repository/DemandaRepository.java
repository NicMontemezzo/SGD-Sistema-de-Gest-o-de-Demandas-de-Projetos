package br.com.uniamerica.sgd.repository;

import br.com.uniamerica.sgd.entity.Demanda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DemandaRepository extends JpaRepository<Demanda, Long>, JpaSpecificationExecutor<Demanda> {

    boolean existsByTituloAndEmpresaId(String titulo, Long empresaId);

    // traz a empresa junto na mesma consulta, evitando uma consulta extra por demanda da página
    @Override
    @EntityGraph(attributePaths = "empresa")
    Page<Demanda> findAll(Specification<Demanda> spec, Pageable pageable);
}
