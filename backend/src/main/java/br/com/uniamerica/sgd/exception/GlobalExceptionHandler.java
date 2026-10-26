package br.com.uniamerica.sgd.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Converte toda exceção lançada pelos controllers em {@link ErrorResponse}.
 * Erros de segurança que acontecem antes do controller (sem token, token inválido)
 * são tratados por {@link JsonAuthenticationEntryPoint} e {@link JsonAccessDeniedHandler}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    static final String MENSAGEM_ERRO_INTERNO = "Erro interno. Tente novamente mais tarde.";
    static final String MENSAGEM_VALIDACAO = "Dados inválidos";
    static final String MENSAGEM_JSON_MALFORMADO = "Corpo da requisição ausente ou malformado";
    static final String MENSAGEM_CONFLITO_VERSAO =
            "O registro foi alterado por outra pessoa. Recarregue e tente novamente.";
    static final String MENSAGEM_NAO_AUTENTICADO = "Autenticação necessária";
    static final String MENSAGEM_ACESSO_NEGADO = "Acesso negado";

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex,
            HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErrorResponse> tratarRegraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErrorResponse> tratarConflito(ConflitoException ex, HttpServletRequest request) {
        return montar(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    /** RN-04: duas edições simultâneas da mesma demanda; só a primeira grava. */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> tratarConflitoDeVersao(ObjectOptimisticLockingFailureException ex,
            HttpServletRequest request) {
        return montar(HttpStatus.CONFLICT, MENSAGEM_CONFLITO_VERSAO, request, List.of());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErrorResponse> tratarCredenciaisInvalidas(CredenciaisInvalidasException ex,
            HttpServletRequest request) {
        return montar(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> tratarAutenticacao(AuthenticationException ex, HttpServletRequest request) {
        return montar(HttpStatus.UNAUTHORIZED, MENSAGEM_NAO_AUTENTICADO, request, List.of());
    }

    /** Lançada por @PreAuthorize quando um ALUNO tenta criar, editar ou excluir (RN-01). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> tratarAcessoNegado(AccessDeniedException ex, HttpServletRequest request) {
        return montar(HttpStatus.FORBIDDEN, MENSAGEM_ACESSO_NEGADO, request, List.of());
    }

    @ExceptionHandler(ServicoExternoIndisponivelException.class)
    public ResponseEntity<ErrorResponse> tratarServicoExternoIndisponivel(ServicoExternoIndisponivelException ex,
            HttpServletRequest request) {
        log.error("Serviço externo indisponível em {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return montar(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> tratarValidacao(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatarCampo)
                .toList();
        return montar(HttpStatus.BAD_REQUEST, MENSAGEM_VALIDACAO, request, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> tratarJsonMalformado(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, MENSAGEM_JSON_MALFORMADO, request, List.of());
    }

    /** Ex.: GET /api/demandas/abc ou situacao=INVALIDA. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tratarTipoInvalido(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String mensagem = "Valor inválido para o parâmetro '%s'".formatted(ex.getName());
        return montar(HttpStatus.BAD_REQUEST, mensagem, request, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> tratarParametroAusente(MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        String mensagem = "Parâmetro obrigatório ausente: '%s'".formatted(ex.getParameterName());
        return montar(HttpStatus.BAD_REQUEST, mensagem, request, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        String mensagem = "Método %s não suportado nesta rota".formatted(ex.getMethod());
        return montar(HttpStatus.METHOD_NOT_ALLOWED, mensagem, request, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> tratarRotaInexistente(NoResourceFoundException ex,
            HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, "Rota não encontrada", request, List.of());
    }

    /**
     * Demais exceções do Spring MVC que já carregam um status HTTP
     * (415 tipo de mídia, 406, ResponseStatusException...), para nenhuma virar 500.
     */
    @ExceptionHandler({ ServletException.class, ErrorResponseException.class })
    public ResponseEntity<ErrorResponse> tratarErroSpring(Exception ex, HttpServletRequest request) {
        HttpStatus status = ex instanceof org.springframework.web.ErrorResponse erro
                ? HttpStatus.resolve(erro.getStatusCode().value())
                : null;
        if (status == null || status.is5xxServerError()) {
            return tratarErroInesperado(ex, request);
        }
        String detalhe = ((org.springframework.web.ErrorResponse) ex).getBody().getDetail();
        return montar(status, detalhe != null ? detalhe : status.getReasonPhrase(), request, List.of());
    }

    /** Qualquer outra exceção: mensagem genérica para o cliente, detalhe só no log. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> tratarErroInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, MENSAGEM_ERRO_INTERNO, request, List.of());
    }

    private ResponseEntity<ErrorResponse> montar(HttpStatus status, String mensagem, HttpServletRequest request,
            List<String> details) {
        if (status.is4xxClientError()) {
            log.warn("{} {} -> {}: {}", request.getMethod(), request.getRequestURI(), status.value(), mensagem);
        }
        ErrorResponse body = ErrorResponse.of(status, mensagem, request.getRequestURI(), details);
        return ResponseEntity.status(status).body(body);
    }

    private static String formatarCampo(FieldError erro) {
        return erro.getField() + ": " + erro.getDefaultMessage();
    }
}
