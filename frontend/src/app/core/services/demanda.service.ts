import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Demanda, DemandaRequest, Pagina } from '../../models/demanda.model';

@Injectable({ providedIn: 'root' })
export class DemandaService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/demandas`;

  listar(pagina = 0, tamanho = 10): Observable<Pagina<Demanda>> {
    const params = new HttpParams().set('page', pagina).set('size', tamanho);
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
