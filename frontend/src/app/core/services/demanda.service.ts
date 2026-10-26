import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Demanda, DemandaRequest, Pagina, SituacaoDemanda } from '../../models/demanda.model';

export interface FiltroDemanda {
  titulo?: string;
  situacao?: SituacaoDemanda | '';
  empresaId?: number;
}

@Injectable({ providedIn: 'root' })
export class DemandaService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/demandas`;

  listar(pagina = 0, tamanho = 10, filtros: FiltroDemanda = {}): Observable<Pagina<Demanda>> {
    let params = new HttpParams().set('page', pagina).set('size', tamanho);
    if (filtros.titulo) {
      params = params.set('titulo', filtros.titulo);
    }
    if (filtros.situacao) {
      params = params.set('situacao', filtros.situacao);
    }
    if (filtros.empresaId) {
      params = params.set('empresaId', filtros.empresaId);
    }
    return this.http.get<Pagina<Demanda>>(this.api, { params });
  }

  buscarPorId(id: number): Observable<Demanda> {
    return this.http.get<Demanda>(`${this.api}/${id}`);
  }

  criar(dados: DemandaRequest): Observable<Demanda> {
    return this.http.post<Demanda>(this.api, dados);
  }

  atualizar(id: number, dados: DemandaRequest): Observable<Demanda> {
    return this.http.put<Demanda>(`${this.api}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
