import { Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout.component';
import { NaoEncontradaComponent } from './pages/nao-encontrada/nao-encontrada.component';
import { DemandaListaComponent } from './components/demanda-lista/demanda-lista.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'demandas', pathMatch: 'full' },
  {
    path: 'login',
    title: 'SGD | Entrar',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent)
  },
  {
    // rotas filhas abrem dentro do <router-outlet> do layout (navbar fica fixa)
    path: 'demandas',
    component: LayoutComponent,
    canActivate: [authGuard],
    canActivateChild: [authGuard],
    children: [{ path: '', title: 'SGD | Demandas', component: DemandaListaComponent }]
  },
  { path: '**', title: 'SGD | Página não encontrada', component: NaoEncontradaComponent }
];
