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

  autenticado(): boolean {
    return this.token() !== null;
  }

  isAdmin(): boolean {
    return this.perfil() === 'ADMIN';
  }

  private guardar(resposta: LoginResponse): void {
    localStorage.setItem(CHAVE_TOKEN, resposta.token);
    localStorage.setItem(CHAVE_NOME, resposta.nome);
    localStorage.setItem(CHAVE_PERFIL, resposta.perfil);
  }
}
