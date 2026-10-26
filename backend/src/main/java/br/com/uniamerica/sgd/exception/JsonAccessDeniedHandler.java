package br.com.uniamerica.sgd.exception;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 403 no formato {@link ErrorResponse} quando um usuário autenticado sem perfil ADMIN
 * acessa uma rota restrita na configuração do Spring Security (RN-01).
 * Registrar no SecurityConfig: {@code .exceptionHandling(e -> e.accessDeniedHandler(handler))}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        log.warn("{} {} -> 403: {}", request.getMethod(), request.getRequestURI(),
                accessDeniedException.getMessage());
        errorResponseWriter.escrever(request, response, HttpStatus.FORBIDDEN,
                GlobalExceptionHandler.MENSAGEM_ACESSO_NEGADO);
    }
}
