package br.com.uniamerica.sgd.exception;

/**
 * Recurso inexistente (ex.: demanda ou empresa com id que não existe). Responde 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String message) {
        super(message);
    }

    public RecursoNaoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }
}
