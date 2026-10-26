export type Perfil = 'ADMIN' | 'ALUNO';

export interface LoginRequest {
  login: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
  nome: string;
  perfil: Perfil;
}
