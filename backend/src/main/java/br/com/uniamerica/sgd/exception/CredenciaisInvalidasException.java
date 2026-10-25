package br.com.uniamerica.sgd.exception;

/**
 * Login ou senha inválidos na API da faculdade. Responde 401.
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException(String message) {
        super(message);
    }

    public CredenciaisInvalidasException(String message, Throwable cause) {
        super(message, cause);
    }
}
