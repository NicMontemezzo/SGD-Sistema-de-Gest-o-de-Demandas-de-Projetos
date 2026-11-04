package br.com.uniamerica.sgd.security;

import br.com.uniamerica.sgd.client.UsuarioFaculdade;
import br.com.uniamerica.sgd.entity.Perfil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String CLAIM_NOME = "nome";
    private static final String CLAIM_PERFIL = "perfil";

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtUtil(
            @Value("${sgd.jwt.secret}") String secret,
            @Value("${sgd.jwt.expiration-ms}") long expiracaoMs
    ) {
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = expiracaoMs;
    }

    public String gerarToken(UsuarioFaculdade usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.login())
                .claim(CLAIM_NOME, usuario.nome())
                .claim(CLAIM_PERFIL, usuario.perfil().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusMillis(expiracaoMs)))
                .signWith(chave)
                .compact();
    }

    public boolean valido(String token) {
        try {
            lerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String lerLogin(String token) {
        return lerClaims(token).getSubject();
    }

    public String lerNome(String token) {
        return lerClaims(token).get(CLAIM_NOME, String.class);
    }

    public Perfil lerPerfil(String token) {
        return Perfil.valueOf(lerClaims(token).get(CLAIM_PERFIL, String.class));
    }

    private Claims lerClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
