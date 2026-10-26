package br.com.uniamerica.sgd.repository;

import br.com.uniamerica.sgd.entity.Demanda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandaRepository extends JpaRepository<Demanda, Long> {

    boolean existsByTituloAndEmpresaId(String titulo, Long empresaId);
}
