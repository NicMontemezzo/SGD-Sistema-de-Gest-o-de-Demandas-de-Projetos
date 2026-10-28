package br.com.uniamerica.sgd.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorResponseTest {

    @Test
    void of_preencheStatusNomeDoStatusMensagemECaminho() {
        ErrorResponse erro = ErrorResponse.of(HttpStatus.NOT_FOUND, "Demanda não encontrada", "/api/demandas/9");

        assertNotNull(erro.timestamp());
        assertEquals(404, erro.status());
        assertEquals("Not Found", erro.error());
        assertEquals("Demanda não encontrada", erro.message());
        assertEquals("/api/demandas/9", erro.path());
        assertTrue(erro.details().isEmpty());
    }

    @Test
    void of_comDetailsNulo_devolveListaVazia() {
        ErrorResponse erro = ErrorResponse.of(HttpStatus.BAD_REQUEST, "Dados inválidos", "/api/demandas", null);

        assertNotNull(erro.details());
        assertTrue(erro.details().isEmpty());
    }

    @Test
    void of_copiaDetailsParaListaImutavel() {
        List<String> details = new ArrayList<>(List.of("titulo: O título é obrigatório"));

        ErrorResponse erro = ErrorResponse.of(HttpStatus.BAD_REQUEST, "Dados inválidos", "/api/demandas", details);
        details.add("alterado depois");

        assertEquals(List.of("titulo: O título é obrigatório"), erro.details());
        assertThrows(UnsupportedOperationException.class, () -> erro.details().add("outro"));
    }
}
