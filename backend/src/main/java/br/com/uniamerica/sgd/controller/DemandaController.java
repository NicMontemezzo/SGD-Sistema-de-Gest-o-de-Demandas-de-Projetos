package br.com.uniamerica.sgd.controller;

import br.com.uniamerica.sgd.dto.DemandaRequest;
import br.com.uniamerica.sgd.dto.DemandaResponse;
import br.com.uniamerica.sgd.service.DemandaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/demandas")
@RequiredArgsConstructor
public class DemandaController {

    private final DemandaService demandaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DemandaResponse> criar(@Valid @RequestBody DemandaRequest request) {
        DemandaResponse criada = demandaService.criar(request);
        return ResponseEntity.created(URI.create("/api/demandas/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DemandaResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody DemandaRequest request) {
        return ResponseEntity.ok(demandaService.atualizar(id, request));
    }
}
