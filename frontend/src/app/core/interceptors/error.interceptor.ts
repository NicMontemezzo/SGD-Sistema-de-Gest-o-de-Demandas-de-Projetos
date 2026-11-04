import { HttpContextToken, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { ErrorResponse } from '../../models/error-response.model';
import { AuthService } from '../services/auth.service';
import { NotificacaoService } from '../services/notificacao.service';

/**
 * Desliga o alerta automático numa requisição específica, quando o componente
 * quer tratar o erro sozinho:
 * this.http.get(url, { context: new HttpContext().set(SILENCIAR_ERRO, true) })
 */
export const SILENCIAR_ERRO = new HttpContextToken<boolean>(() => false);

const ROTA_LOGIN = '/login';
const MENSAGEM_PADRAO = 'Não foi possível concluir a operação.';

/** Mostra todo erro HTTP da API com o NotificacaoService e relança o erro para quem chamou. */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notificacao = inject(NotificacaoService);
  const router = inject(Router);
  const auth = inject(AuthService);

  return next(req).pipe(
    catchError((erro: unknown) => {
      if (erro instanceof HttpErrorResponse && !req.context.get(SILENCIAR_ERRO)) {
        tratar(erro, req.url, notificacao, router, auth);
      }
      return throwError(() => erro);
    })
  );
};

function tratar(
  erro: HttpErrorResponse,
  url: string,
  notificacao: NotificacaoService,
  router: Router,
  auth: AuthService
): void {
  const corpo = lerCorpo(erro);
  const mensagem = corpo?.message || MENSAGEM_PADRAO;

  switch (erro.status) {
    case 0:
      notificacao.erro('Não foi possível conectar ao servidor. Verifique sua conexão.', [], 'Servidor indisponível');
      break;
    case 400:
      notificacao.erro(mensagem, corpo?.details ?? [], 'Dados inválidos');
      break;
    case 401:
      if (ehRequisicaoDeLogin(url)) {
        notificacao.erro(mensagem, [], 'Falha no login');
      } else if (auth.token()) {
        // Token expirado ou recusado: apaga a sessão e volta ao login uma vez só.
        // Se várias requisições derem 401 juntas, as seguintes já não acham token e não repetem o aviso.
        const retorno = router.url;
        auth.logout();
        notificacao.aviso('Sua sessão expirou. Faça login novamente.', 'Sessão encerrada');
        router.navigate([ROTA_LOGIN], { queryParams: { retorno } });
      }
      break;
    case 403:
      notificacao.aviso('Você não tem permissão para realizar esta ação.', 'Acesso negado');
      break;
    case 404:
      notificacao.aviso(mensagem, 'Não encontrado');
      break;
    case 409:
      notificacao.aviso(mensagem, 'Conflito');
      break;
    case 503:
      notificacao.erro(mensagem, [], 'Serviço indisponível');
      break;
    default:
      if (erro.status >= 500) {
        notificacao.erro('Erro interno no servidor. Tente novamente mais tarde.', [], 'Erro no servidor');
      } else {
        notificacao.erro(mensagem);
      }
  }
}

function lerCorpo(erro: HttpErrorResponse): ErrorResponse | null {
  const corpo = erro.error;
  return corpo && typeof corpo === 'object' && 'message' in corpo ? (corpo as ErrorResponse) : null;
}

function ehRequisicaoDeLogin(url: string): boolean {
  return url.includes('/auth/login');
}
