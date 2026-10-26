package br.com.uniamerica.sgd.exception;

/**
 * Conflito com o estado atual dos dados (ex.: título repetido na mesma empresa). Responde 409.
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String message) {
        super(message);
    }

    public ConflitoException(String message, Throwable cause) {
        super(message, cause);
    }
}
