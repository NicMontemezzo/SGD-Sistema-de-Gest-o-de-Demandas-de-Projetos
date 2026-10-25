package br.com.uniamerica.sgd.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Formato único de erro da API. Nunca carrega stack trace.
 *
 * @param details campos inválidos nos erros 400 de validação; lista vazia nos demais
 */
public record ErrorResponse(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details) {

    public static ErrorResponse of(HttpStatus status, String message, String path, List<String> details) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                details == null ? List.of() : List.copyOf(details));
    }

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return of(status, message, path, List.of());
    }
}
