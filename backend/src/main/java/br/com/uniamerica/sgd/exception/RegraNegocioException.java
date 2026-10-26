package br.com.uniamerica.sgd.exception;

/**
 * Violação de regra de negócio (ex.: editar demanda ENCERRADA). Responde 400.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String message) {
        super(message);
    }

    public RegraNegocioException(String message, Throwable cause) {
        super(message, cause);
    }
}
