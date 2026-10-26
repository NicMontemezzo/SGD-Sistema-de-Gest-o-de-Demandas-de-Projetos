package br.com.uniamerica.sgd.dto;

import br.com.uniamerica.sgd.entity.SituacaoDemanda;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DemandaRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(min = 5, max = 120, message = "O título deve ter entre 5 e 120 caracteres")
        String titulo,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String descricao,

        @Size(max = 500, message = "As tecnologias devem ter no máximo 500 caracteres")
        String tecnologias,

        @NotNull(message = "A empresa é obrigatória")
        Long empresaId,

        SituacaoDemanda situacao,

        // obrigatório só na edição (controle de versão / RN-04)
        Long versao
) {
}
