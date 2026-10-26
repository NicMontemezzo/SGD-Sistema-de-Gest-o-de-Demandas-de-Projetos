package br.com.uniamerica.sgd.exception;

/**
 * API da faculdade fora do ar ou respondendo com erro. Responde 503.
 */
public class ServicoExternoIndisponivelException extends RuntimeException {

    public ServicoExternoIndisponivelException(String message) {
        super(message);
    }

    public ServicoExternoIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}
