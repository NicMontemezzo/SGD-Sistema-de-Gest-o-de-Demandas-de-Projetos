package br.com.uniamerica.sgd.service;

import br.com.uniamerica.sgd.client.FaculdadeGateway;
import br.com.uniamerica.sgd.client.UsuarioFaculdade;
import br.com.uniamerica.sgd.dto.LoginRequest;
import br.com.uniamerica.sgd.dto.LoginResponse;
import br.com.uniamerica.sgd.entity.Perfil;
import br.com.uniamerica.sgd.exception.CredenciaisInvalidasException;
import br.com.uniamerica.sgd.exception.ServicoExternoIndisponivelException;
import br.com.uniamerica.sgd.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private FaculdadeGateway faculdadeGateway;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void deveLogarAdminEDevolverTokenComPerfilADMIN() {
        UsuarioFaculdade admin = new UsuarioFaculdade("admin", "Administrador", Perfil.ADMIN);
        when(faculdadeGateway.autenticar("admin", "admin123")).thenReturn(admin);
        when(jwtUtil.gerarToken(admin)).thenReturn("token-admin");

        LoginResponse resposta = authService.login(new LoginRequest("admin", "admin123"));

        assertEquals("token-admin", resposta.token());
        assertEquals("Administrador", resposta.nome());
        assertEquals(Perfil.ADMIN, resposta.perfil());
    }

    @Test
    void deveLogarAlunoEDevolverPerfilALUNO() {
        UsuarioFaculdade aluno = new UsuarioFaculdade("ra123", "Aluno Teste", Perfil.ALUNO);
        when(faculdadeGateway.autenticar("ra123", "senha")).thenReturn(aluno);
        when(jwtUtil.gerarToken(aluno)).thenReturn("token-aluno");

        LoginResponse resposta = authService.login(new LoginRequest("ra123", "senha"));

        assertEquals(Perfil.ALUNO, resposta.perfil());
        assertEquals("token-aluno", resposta.token());
    }

    @Test
    void deveLancarCredenciaisInvalidasQuandoGatewayLanca() {
        when(faculdadeGateway.autenticar("admin", "errada"))
                .thenThrow(new CredenciaisInvalidasException("login ou senha inválidos"));

        assertThrows(CredenciaisInvalidasException.class,
                () -> authService.login(new LoginRequest("admin", "errada")));
        verify(jwtUtil, never()).gerarToken(any());
    }

    @Test
    void deveLancarServicoExternoIndisponivelQuandoApiFora() {
        when(faculdadeGateway.autenticar("admin", "admin123"))
                .thenThrow(new ServicoExternoIndisponivelException("API da faculdade fora do ar"));

        assertThrows(ServicoExternoIndisponivelException.class,
                () -> authService.login(new LoginRequest("admin", "admin123")));
        verify(jwtUtil, never()).gerarToken(any());
    }
}
