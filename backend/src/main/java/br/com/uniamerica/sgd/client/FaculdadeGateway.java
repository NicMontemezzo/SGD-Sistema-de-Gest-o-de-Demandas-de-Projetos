package br.com.uniamerica.sgd.client;

import br.com.uniamerica.sgd.exception.CredenciaisInvalidasException;
import br.com.uniamerica.sgd.exception.ServicoExternoIndisponivelException;

/**
 * Contrato com a API da faculdade. O AuthService depende só desta interface.
 * <ul>
 *   <li>perfil {@code mock}: {@link MockFaculdadeGateway} (usado até a API chegar);</li>
 *   <li>demais perfis: {@link FeignFaculdadeGateway}.</li>
 * </ul>
 */
public interface FaculdadeGateway {

    /**
     * @return nome e perfil do usuário autenticado
     * @throws CredenciaisInvalidasException login ou senha incorretos
     * @throws ServicoExternoIndisponivelException a API da faculdade não respondeu
     */
    UsuarioFaculdade autenticar(String login, String senha);
}
