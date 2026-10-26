import { Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout.component';
import { NaoEncontradaComponent } from './pages/nao-encontrada/nao-encontrada.component';

// Cada integrante só acrescenta a rota do próprio componente
export const routes: Routes = [
  { path: '', redirectTo: 'demandas', pathMatch: 'full' },
  // { path: 'login', component: LoginComponent },  (Integrante 1)
  {
    path: 'demandas',
    component: LayoutComponent,
    // canActivate: [authGuard],  (Integrante 1)
    children: [
      // A lista (Integrante 5) abre o modal de cadastro/edição (Integrante 2) em "novo" e ":id/editar"
      // { path: '', component: DemandaListaComponent },
      // { path: 'novo', component: DemandaListaComponent },
      // { path: ':id/editar', component: DemandaListaComponent }
    ]
  },
  { path: '**', component: NaoEncontradaComponent }
];
