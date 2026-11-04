package br.com.uniamerica.sgd.security;

import br.com.uniamerica.sgd.client.UsuarioFaculdade;
import br.com.uniamerica.sgd.entity.Perfil;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtAuthFilterTest {

    private static final String SECRET = "chave-de-teste-com-no-minimo-32-caracteres-para-hs256";
    private static final long EXPIRACAO_MS = 3_600_000L;

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, EXPIRACAO_MS);
    private final JwtAuthFilter filter = new JwtAuthFilter(jwtUtil);

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void naoAutenticaQuandoNaoHaHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void naoAutenticaQuandoTokenInvalido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-adulterado");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void autenticaComTokenValidoEDefineRolePorPerfil() throws Exception {
        UsuarioFaculdade admin = new UsuarioFaculdade("admin", "Administrador", Perfil.ADMIN);
        String token = jwtUtil.gerarToken(admin);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("admin", auth.getPrincipal());
        assertEquals(1, auth.getAuthorities().size());
        assertEquals("ROLE_ADMIN", auth.getAuthorities().iterator().next().getAuthority());
        verify(chain).doFilter(request, response);
    }
}
