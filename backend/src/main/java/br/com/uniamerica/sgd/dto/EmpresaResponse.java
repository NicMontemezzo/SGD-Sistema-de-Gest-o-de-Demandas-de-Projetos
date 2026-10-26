package br.com.uniamerica.sgd.dto;

import br.com.uniamerica.sgd.entity.Empresa;

public record EmpresaResponse(
        Long id,
        String nome,
        String cnpj
) {
    public static EmpresaResponse from(Empresa e) {
        return new EmpresaResponse(e.getId(), e.getNome(), e.getCnpj());
    }
}
