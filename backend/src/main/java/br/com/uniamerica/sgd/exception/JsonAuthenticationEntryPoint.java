package br.com.uniamerica.sgd.exception;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 401 no formato {@link ErrorResponse} quando a requisição chega sem token ou com token inválido.
 * Registrar no SecurityConfig: {@code .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        log.warn("{} {} -> 401: {}", request.getMethod(), request.getRequestURI(), authException.getMessage());
        errorResponseWriter.escrever(request, response, HttpStatus.UNAUTHORIZED,
                GlobalExceptionHandler.MENSAGEM_NAO_AUTENTICADO);
    }
}
