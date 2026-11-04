import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Empresa } from '../../models/empresa.model';

@Injectable({ providedIn: 'root' })
export class EmpresaService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/empresas`;

  listar(): Observable<Empresa[]> {
    return this.http.get<Empresa[]>(this.api);
  }
}
