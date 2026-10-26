import { Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout.component';
import { NaoEncontradaComponent } from './pages/nao-encontrada/nao-encontrada.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'demandas', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'demandas',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: []
  },
  { path: '**', component: NaoEncontradaComponent }
];
