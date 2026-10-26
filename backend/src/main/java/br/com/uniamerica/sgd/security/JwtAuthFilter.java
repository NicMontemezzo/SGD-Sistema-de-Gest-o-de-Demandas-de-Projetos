package br.com.uniamerica.sgd.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.uniamerica.sgd.entity.Perfil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Lê o cabeçalho {@code Authorization: Bearer <token>} e autentica a requisição com o perfil
 * do token ({@code ROLE_ADMIN} ou {@code ROLE_ALUNO}). Token ausente ou inválido segue sem
 * autenticação: nas rotas protegidas o JsonAuthenticationEntryPoint responde 401.
 * <p>
 * Não é {@code @Component} de propósito: é criado pelo SecurityConfig, senão o Spring Boot
 * também o registraria como filtro comum do servidor e ele rodaria fora do Spring Security.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIXO_BEARER = "Bearer ";
    private static final String PREFIXO_ROLE = "ROLE_";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecalho = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (cabecalho != null && cabecalho.startsWith(PREFIXO_BEARER)) {
            autenticar(cabecalho.substring(PREFIXO_BEARER.length()).trim(), request);
        }

        chain.doFilter(request, response);
    }

    private void autenticar(String token, HttpServletRequest request) {
        try {
            String login = jwtUtil.lerLogin(token);
            Perfil perfil = jwtUtil.lerPerfil(token);
            var autoridade = new SimpleGrantedAuthority(PREFIXO_ROLE + perfil.name());
            var autenticacao = UsernamePasswordAuthenticationToken.authenticated(
                    login, null, List.of(autoridade));
            autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacao);
            SecurityContextHolder.setContext(contexto);
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Token JWT inválido em {} {}: {}", request.getMethod(), request.getRequestURI(),
                    ex.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
}
