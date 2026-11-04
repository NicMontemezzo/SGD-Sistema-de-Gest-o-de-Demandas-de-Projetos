package br.com.uniamerica.sgd.client;

import br.com.uniamerica.sgd.client.MockFaculdadeProperties.Usuario;
import br.com.uniamerica.sgd.entity.Perfil;
import br.com.uniamerica.sgd.exception.CredenciaisInvalidasException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockFaculdadeGatewayTest {

    private final MockFaculdadeGateway gateway = new MockFaculdadeGateway(new MockFaculdadeProperties(List.of(
            new Usuario("admin", "Administrador Mock", Perfil.ADMIN, "senha-admin"),
            new Usuario("aluno", "Aluno Mock", Perfil.ALUNO, "senha-aluno"),
            new Usuario("semsenha", "Sem Senha", Perfil.ALUNO, ""),
            new Usuario("senhanula", "Senha Nula", Perfil.ALUNO, null))));

    @Test
    void autenticar_admin_devolvePerfilADMIN() {
        UsuarioFaculdade usuario = gateway.autenticar("admin", "senha-admin");

        assertEquals(new UsuarioFaculdade("admin", "Administrador Mock", Perfil.ADMIN), usuario);
    }

    @Test
    void autenticar_aluno_devolvePerfilALUNO() {
        UsuarioFaculdade usuario = gateway.autenticar("aluno", "senha-aluno");

        assertEquals(Perfil.ALUNO, usuario.perfil());
        assertEquals("Aluno Mock", usuario.nome());
    }

    @Test
    void autenticar_ignoraMaiusculasNoLogin() {
        UsuarioFaculdade usuario = gateway.autenticar("ADMIN", "senha-admin");

        assertEquals("admin", usuario.login());
    }

    @Test
    void autenticar_senhaErrada_lancaCredenciaisInvalidas() {
        assertCredenciaisInvalidas("admin", "senha-aluno");
    }

    @Test
    void autenticar_senhaComMaiusculasDiferentes_lancaCredenciaisInvalidas() {
        assertCredenciaisInvalidas("admin", "SENHA-ADMIN");
    }

    @Test
    void autenticar_loginInexistente_lancaCredenciaisInvalidas() {
        assertCredenciaisInvalidas("professor", "senha-admin");
    }

    @Test
    void autenticar_loginNulo_lancaCredenciaisInvalidas() {
        assertCredenciaisInvalidas(null, "senha-admin");
    }

    @Test
    void autenticar_senhaNula_lancaCredenciaisInvalidas() {
        assertCredenciaisInvalidas("admin", null);
    }

    @Test
    void autenticar_usuarioSemSenhaConfigurada_nuncaLoga() {
        assertCredenciaisInvalidas("semsenha", "");
        assertCredenciaisInvalidas("senhanula", "");
    }

    @Test
    void autenticar_semUsuariosConfigurados_lancaCredenciaisInvalidas() {
        MockFaculdadeGateway semUsuarios = new MockFaculdadeGateway(new MockFaculdadeProperties(null));

        assertThrows(CredenciaisInvalidasException.class, () -> semUsuarios.autenticar("admin", "senha-admin"));
    }

    @Test
    void properties_comListaNula_viraListaVazia() {
        assertTrue(new MockFaculdadeProperties(null).usuarios().isEmpty());
    }

    private void assertCredenciaisInvalidas(String login, String senha) {
        CredenciaisInvalidasException ex = assertThrows(CredenciaisInvalidasException.class,
                () -> gateway.autenticar(login, senha));
        assertEquals(MockFaculdadeGateway.MENSAGEM_CREDENCIAIS_INVALIDAS, ex.getMessage());
    }
}
