// RN-03: só estas duas situações existem no back-end
export type SituacaoDemanda = 'DISPONIVEL' | 'ENCERRADA';

export interface Demanda {
  id: number;
  titulo: string;
  descricao: string;
  tecnologias?: string;
  empresaId: number;
  empresaNome: string;
  situacao: SituacaoDemanda;
  versao: number;
}

export interface DemandaRequest {
  titulo: string;
  descricao: string;
  tecnologias?: string;
  empresaId: number;
  situacao?: SituacaoDemanda;
  versao?: number; // obrigatório na edição
}

export interface Pagina<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
