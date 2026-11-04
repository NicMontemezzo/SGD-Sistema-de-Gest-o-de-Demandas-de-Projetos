package br.com.uniamerica.sgd.security;

import br.com.uniamerica.sgd.client.UsuarioFaculdade;
import br.com.uniamerica.sgd.entity.Perfil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "chave-de-teste-com-no-minimo-32-caracteres-para-hs256";
    private static final long EXPIRACAO_MS = 3_600_000L;

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, EXPIRACAO_MS);
    private final UsuarioFaculdade admin = new UsuarioFaculdade("admin", "Administrador", Perfil.ADMIN);

    @Test
    void deveGerarTokenValidoEDevolverPerfilADMIN() {
        String token = jwtUtil.gerarToken(admin);

        assertTrue(jwtUtil.valido(token));
        assertEquals("admin", jwtUtil.lerLogin(token));
        assertEquals("Administrador", jwtUtil.lerNome(token));
        assertEquals(Perfil.ADMIN, jwtUtil.lerPerfil(token));
    }

    @Test
    void deveGerarTokenComPerfilALUNO() {
        UsuarioFaculdade aluno = new UsuarioFaculdade("ra123", "Aluno Teste", Perfil.ALUNO);

        String token = jwtUtil.gerarToken(aluno);

        assertEquals(Perfil.ALUNO, jwtUtil.lerPerfil(token));
    }

    @Test
    void deveInvalidarTokenAdulterado() {
        String token = jwtUtil.gerarToken(admin);
        String adulterado = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtUtil.valido(adulterado));
    }

    @Test
    void deveInvalidarTokenExpirado() throws Exception {
        JwtUtil jwtExpirado = new JwtUtil(SECRET, 1L);
        String token = jwtExpirado.gerarToken(admin);
        Thread.sleep(50);

        assertFalse(jwtExpirado.valido(token));
    }

    @Test
    void deveInvalidarTokenAssinadoComOutraChave() {
        JwtUtil outro = new JwtUtil("outra-chave-de-teste-com-no-minimo-32-caracteres!!", EXPIRACAO_MS);
        String tokenDeOutro = outro.gerarToken(admin);

        assertFalse(jwtUtil.valido(tokenDeOutro));
    }
}
