package br.com.uniamerica.sgd.repository;

import br.com.uniamerica.sgd.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    List<Empresa> findAllByOrderByNomeAsc();
}
