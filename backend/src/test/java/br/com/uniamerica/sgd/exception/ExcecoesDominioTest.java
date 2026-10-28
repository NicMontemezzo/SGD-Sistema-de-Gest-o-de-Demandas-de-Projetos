package br.com.uniamerica.sgd.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * As exceções do domínio guardam a causa original, para o log mostrar de onde veio o erro.
 */
class ExcecoesDominioTest {

    private final Throwable causa = new IllegalStateException("causa original");

    @Test
    void conflito_guardaMensagemECausa() {
        ConflitoException ex = new ConflitoException("Título repetido", causa);

        assertEquals("Título repetido", ex.getMessage());
        assertSame(causa, ex.getCause());
    }

    @Test
    void credenciaisInvalidas_guardaMensagemECausa() {
        CredenciaisInvalidasException ex = new CredenciaisInvalidasException("Login ou senha inválidos", causa);

        assertEquals("Login ou senha inválidos", ex.getMessage());
        assertSame(causa, ex.getCause());
    }

    @Test
    void recursoNaoEncontrado_guardaMensagemECausa() {
        RecursoNaoEncontradoException ex = new RecursoNaoEncontradoException("Demanda não encontrada", causa);

        assertEquals("Demanda não encontrada", ex.getMessage());
        assertSame(causa, ex.getCause());
    }

    @Test
    void regraNegocio_guardaMensagemECausa() {
        RegraNegocioException ex = new RegraNegocioException("Demanda encerrada", causa);

        assertEquals("Demanda encerrada", ex.getMessage());
        assertSame(causa, ex.getCause());
    }

    @Test
    void servicoExternoIndisponivel_guardaMensagemECausa() {
        ServicoExternoIndisponivelException ex = new ServicoExternoIndisponivelException("API fora do ar", causa);

        assertEquals("API fora do ar", ex.getMessage());
        assertSame(causa, ex.getCause());
    }
}
