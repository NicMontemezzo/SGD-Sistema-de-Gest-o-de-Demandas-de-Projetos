package br.com.uniamerica.sgd.exception;

import br.com.uniamerica.sgd.entity.Demanda;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private static final String PATH = "/api/demandas/1";

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private MockHttpServletRequest request;

    @BeforeEach
    void preparar() {
        request = new MockHttpServletRequest("GET", PATH);
    }

    @Test
    void recursoNaoEncontrado_responde404ComMensagemDaExcecao() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarRecursoNaoEncontrado(
                new RecursoNaoEncontradoException("Demanda não encontrada"), request);

        assertErro(resposta, HttpStatus.NOT_FOUND, "Demanda não encontrada");
    }

    @Test
    void regraNegocio_responde400ComMensagemDaExcecao() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarRegraNegocio(
                new RegraNegocioException("Demanda encerrada não pode ser editada"), request);

        assertErro(resposta, HttpStatus.BAD_REQUEST, "Demanda encerrada não pode ser editada");
    }

    @Test
    void conflito_responde409ComMensagemDaExcecao() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarConflito(
                new ConflitoException("Título já existe nesta empresa"), request);

        assertErro(resposta, HttpStatus.CONFLICT, "Título já existe nesta empresa");
    }

    @Test
    void conflitoDeVersao_responde409ComMensagemFixa() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarConflitoDeVersao(
                new ObjectOptimisticLockingFailureException(Demanda.class, 1L), request);

        assertErro(resposta, HttpStatus.CONFLICT, GlobalExceptionHandler.MENSAGEM_CONFLITO_VERSAO);
    }

    @Test
    void violacaoDeIntegridade_responde409SemExporDetalheDoBanco() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("could not execute statement",
                new RuntimeException("duplicate key value violates unique constraint \"uk_demanda_titulo\""));

        ResponseEntity<ErrorResponse> resposta = handler.tratarIntegridade(ex, request);

        assertErro(resposta, HttpStatus.CONFLICT, GlobalExceptionHandler.MENSAGEM_INTEGRIDADE);
    }

    @Test
    void credenciaisInvalidas_responde401ComMensagemDaExcecao() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarCredenciaisInvalidas(
                new CredenciaisInvalidasException("Login ou senha inválidos"), request);

        assertErro(resposta, HttpStatus.UNAUTHORIZED, "Login ou senha inválidos");
    }

    @Test
    void autenticacao_responde401ComMensagemFixa() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarAutenticacao(
                new BadCredentialsException("token expirado"), request);

        assertErro(resposta, HttpStatus.UNAUTHORIZED, GlobalExceptionHandler.MENSAGEM_NAO_AUTENTICADO);
    }

    @Test
    void acessoNegado_responde403ComMensagemFixa() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarAcessoNegado(
                new AccessDeniedException("Access Denied"), request);

        assertErro(resposta, HttpStatus.FORBIDDEN, GlobalExceptionHandler.MENSAGEM_ACESSO_NEGADO);
    }

    @Test
    void servicoExternoIndisponivel_responde503() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarServicoExternoIndisponivel(
                new ServicoExternoIndisponivelException("API da faculdade indisponível"), request);

        assertErro(resposta, HttpStatus.SERVICE_UNAVAILABLE, "API da faculdade indisponível");
    }

    @Test
    void validacao_responde400ComUmDetalhePorCampo() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "demandaRequest");
        bindingResult.addError(new FieldError("demandaRequest", "titulo", "O título é obrigatório"));
        bindingResult.addError(new FieldError("demandaRequest", "empresaId", "A empresa é obrigatória"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> resposta = handler.tratarValidacao(ex, request);

        assertErro(resposta, HttpStatus.BAD_REQUEST, GlobalExceptionHandler.MENSAGEM_VALIDACAO,
                List.of("titulo: O título é obrigatório", "empresaId: A empresa é obrigatória"));
    }

    @Test
    void jsonMalformado_responde400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error",
                new MockHttpInputMessage("{".getBytes()));

        ResponseEntity<ErrorResponse> resposta = handler.tratarJsonMalformado(ex, request);

        assertErro(resposta, HttpStatus.BAD_REQUEST, GlobalExceptionHandler.MENSAGEM_JSON_MALFORMADO);
    }

    @Test
    void tipoInvalido_responde400ComNomeDoParametro() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");

        ResponseEntity<ErrorResponse> resposta = handler.tratarTipoInvalido(ex, request);

        assertErro(resposta, HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro 'id'");
    }

    @Test
    void ordenacaoInvalida_responde400ComNomeDoCampo() {
        PropertyReferenceException ex = new PropertyReferenceException("campoQueNaoExiste",
                TypeInformation.of(Demanda.class), List.of());

        ResponseEntity<ErrorResponse> resposta = handler.tratarOrdenacaoInvalida(ex, request);

        assertErro(resposta, HttpStatus.BAD_REQUEST, "Campo de ordenação inválido: 'campoQueNaoExiste'");
    }

    @Test
    void parametroAusente_responde400ComNomeDoParametro() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarParametroAusente(
                new MissingServletRequestParameterException("empresaId", "Long"), request);

        assertErro(resposta, HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente: 'empresaId'");
    }

    @Test
    void metodoNaoSuportado_responde405ComMetodo() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarMetodoNaoSuportado(
                new HttpRequestMethodNotSupportedException("PATCH"), request);

        assertErro(resposta, HttpStatus.METHOD_NOT_ALLOWED, "Método PATCH não suportado nesta rota");
    }

    @Test
    void rotaInexistente_responde404() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarRotaInexistente(
                mock(NoResourceFoundException.class), request);

        assertErro(resposta, HttpStatus.NOT_FOUND, "Rota não encontrada");
    }

    @Test
    void erroSpring4xx_usaStatusEDetalheDaExcecao() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarErroSpring(
                new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de mídia não suportado"),
                request);

        assertErro(resposta, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de mídia não suportado");
    }

    @Test
    void erroSpring4xxSemDetalhe_usaNomeDoStatus() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarErroSpring(
                new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE), request);

        assertErro(resposta, HttpStatus.NOT_ACCEPTABLE, HttpStatus.NOT_ACCEPTABLE.getReasonPhrase());
    }

    @Test
    void erroSpring5xx_viraErroInternoGenerico() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarErroSpring(
                new ResponseStatusException(HttpStatus.BAD_GATEWAY, "detalhe interno"), request);

        assertErro(resposta, HttpStatus.INTERNAL_SERVER_ERROR, GlobalExceptionHandler.MENSAGEM_ERRO_INTERNO);
    }

    @Test
    void erroSpringComStatusDesconhecido_viraErroInternoGenerico() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarErroSpring(
                new ResponseStatusException(HttpStatusCode.valueOf(499)), request);

        assertErro(resposta, HttpStatus.INTERNAL_SERVER_ERROR, GlobalExceptionHandler.MENSAGEM_ERRO_INTERNO);
    }

    @Test
    void servletExceptionSemStatus_viraErroInternoGenerico() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarErroSpring(
                new ServletException("falha no filtro"), request);

        assertErro(resposta, HttpStatus.INTERNAL_SERVER_ERROR, GlobalExceptionHandler.MENSAGEM_ERRO_INTERNO);
    }

    @Test
    void erroInesperado_responde500SemExporMensagemOriginal() {
        ResponseEntity<ErrorResponse> resposta = handler.tratarErroInesperado(
                new NullPointerException("campo x era nulo"), request);

        assertErro(resposta, HttpStatus.INTERNAL_SERVER_ERROR, GlobalExceptionHandler.MENSAGEM_ERRO_INTERNO);
    }

    private static void assertErro(ResponseEntity<ErrorResponse> resposta, HttpStatus status, String mensagem) {
        assertErro(resposta, status, mensagem, List.of());
    }

    private static void assertErro(ResponseEntity<ErrorResponse> resposta, HttpStatus status, String mensagem,
            List<String> details) {
        assertEquals(status, resposta.getStatusCode());
        ErrorResponse body = resposta.getBody();
        assertNotNull(body);
        assertNotNull(body.timestamp());
        assertEquals(status.value(), body.status());
        assertEquals(status.getReasonPhrase(), body.error());
        assertEquals(mensagem, body.message());
        assertEquals(PATH, body.path());
        assertEquals(details, body.details());
    }
}
