package br.com.uniamerica.sgd.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import br.com.uniamerica.sgd.entity.Perfil;
import br.com.uniamerica.sgd.exception.JsonAccessDeniedHandler;
import br.com.uniamerica.sgd.exception.JsonAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;

/**
 * Regras de acesso da API (RN-01):
 * <ul>
 *   <li>público: login e Swagger;</li>
 *   <li>autenticado (ADMIN ou ALUNO): consultas;</li>
 *   <li>só ADMIN: criar, editar e excluir demandas.</li>
 * </ul>
 * API stateless: sem sessão e sem CSRF, a identidade vem do token JWT em cada requisição.
 * Erros 401/403 saem no formato ErrorResponse pelos handlers do pacote exception.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ROTA_DEMANDAS = "/api/demandas/**";
    private static final String[] ROTAS_PUBLICAS = {
        "/api/auth/login",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/error"
    };

    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    @Value("${sgd.cors.allowed-origins}")
    private String origensPermitidas;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String admin = Perfil.ADMIN.name();

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(erros -> erros
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(rotas -> rotas
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(ROTAS_PUBLICAS).permitAll()
                .requestMatchers(HttpMethod.POST, ROTA_DEMANDAS).hasRole(admin)
                .requestMatchers(HttpMethod.PUT, ROTA_DEMANDAS).hasRole(admin)
                .requestMatchers(HttpMethod.DELETE, ROTA_DEMANDAS).hasRole(admin)
                .anyRequest().authenticated());

        // Integrante 1: registrar o filtro do token aqui, quando o JwtAuthFilter existir:
        // http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origensPermitidas.split(","))
            .map(String::trim)
            .filter(origem -> !origem.isEmpty())
            .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    /**
     * Os usuários vêm da API da faculdade (FaculdadeGateway), não de um cadastro local.
     * Este bean só impede o Spring de criar o usuário padrão com senha gerada no log.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return login -> {
            throw new UsernameNotFoundException("Usuários são autenticados pela API da faculdade");
        };
    }
}
