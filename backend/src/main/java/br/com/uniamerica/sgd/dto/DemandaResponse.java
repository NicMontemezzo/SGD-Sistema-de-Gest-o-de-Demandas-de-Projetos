package br.com.uniamerica.sgd.dto;

import br.com.uniamerica.sgd.entity.Demanda;
import br.com.uniamerica.sgd.entity.SituacaoDemanda;

public record DemandaResponse(
        Long id,
        String titulo,
        String descricao,
        String tecnologias,
        Long empresaId,
        String empresaNome,
        SituacaoDemanda situacao,
        Long versao
) {
    public static DemandaResponse from(Demanda d) {
        return new DemandaResponse(
                d.getId(), d.getTitulo(), d.getDescricao(), d.getTecnologias(),
                d.getEmpresa().getId(), d.getEmpresa().getNome(),
                d.getSituacao(), d.getVersao());
    }
}
