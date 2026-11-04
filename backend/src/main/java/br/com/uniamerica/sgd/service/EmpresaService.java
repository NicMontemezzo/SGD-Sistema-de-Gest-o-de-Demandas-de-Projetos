package br.com.uniamerica.sgd.service;

import br.com.uniamerica.sgd.dto.EmpresaResponse;
import br.com.uniamerica.sgd.entity.Empresa;
import br.com.uniamerica.sgd.exception.RecursoNaoEncontradoException;
import br.com.uniamerica.sgd.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listar() {
        List<EmpresaResponse> empresas = empresaRepository.findAllByOrderByNomeAsc().stream()
                .map(EmpresaResponse::from)
                .toList();
        log.info("{} empresas listadas", empresas.size());
        return empresas;
    }

    @Transactional(readOnly = true)
    public Empresa buscarPorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa " + id + " não encontrada"));
    }
}
