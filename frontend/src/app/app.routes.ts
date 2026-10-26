import { Routes } from '@angular/router';
import { DemandaListaComponent } from './components/demanda-lista/demanda-lista.component';

export const routes: Routes = [
  { path: 'demandas', component: DemandaListaComponent },
  { path: '', redirectTo: 'demandas', pathMatch: 'full' }
];
