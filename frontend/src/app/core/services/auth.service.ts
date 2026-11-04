import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, Perfil } from '../../models/login.model';

const CHAVE_TOKEN = 'sgd.token';
const CHAVE_NOME = 'sgd.nome';
const CHAVE_PERFIL = 'sgd.perfil';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap((resposta) => this.guardar(resposta))
    );
  }

  logout(): void {
    localStorage.removeItem(CHAVE_TOKEN);
    localStorage.removeItem(CHAVE_NOME);
    localStorage.removeItem(CHAVE_PERFIL);
  }

  token(): string | null {
    return localStorage.getItem(CHAVE_TOKEN);
  }

  nome(): string | null {
    return localStorage.getItem(CHAVE_NOME);
  }

  perfil(): Perfil | null {
    return localStorage.getItem(CHAVE_PERFIL) as Perfil | null;
  }

  /** Há token e ele ainda não expirou. Token vencido é apagado na hora. */
  autenticado(): boolean {
    const token = this.token();
    if (!token) {
      return false;
    }
    if (this.expirado(token)) {
      this.logout();
      return false;
    }
    return true;
  }

  isAdmin(): boolean {
    return this.perfil() === 'ADMIN';
  }

  /**
   * Lê o campo "exp" (segundos) do payload do JWT. Só serve para a tela saber se a sessão acabou:
   * quem valida a assinatura de verdade é o back-end. Token ilegível conta como expirado.
   */
  private expirado(token: string): boolean {
    try {
      const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const { exp } = JSON.parse(atob(payload)) as { exp?: number };
      return typeof exp === 'number' && exp * 1000 <= Date.now();
    } catch {
      return true;
    }
  }

  private guardar(resposta: LoginResponse): void {
    localStorage.setItem(CHAVE_TOKEN, resposta.token);
    localStorage.setItem(CHAVE_NOME, resposta.nome);
    localStorage.setItem(CHAVE_PERFIL, resposta.perfil);
  }
}
