package br.com.uniamerica.sgd.service;

import br.com.uniamerica.sgd.client.FaculdadeGateway;
import br.com.uniamerica.sgd.client.UsuarioFaculdade;
import br.com.uniamerica.sgd.dto.LoginRequest;
import br.com.uniamerica.sgd.dto.LoginResponse;
import br.com.uniamerica.sgd.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final FaculdadeGateway faculdadeGateway;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        UsuarioFaculdade usuario = faculdadeGateway.autenticar(request.login(), request.senha());
        String token = jwtUtil.gerarToken(usuario);
        log.info("Login autorizado para {} ({})", usuario.login(), usuario.perfil());
        return new LoginResponse(token, usuario.nome(), usuario.perfil());
    }
}
