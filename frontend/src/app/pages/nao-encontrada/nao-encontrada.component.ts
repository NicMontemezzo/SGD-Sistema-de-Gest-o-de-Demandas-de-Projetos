import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Página 404. Registrada como última rota em app.routes.ts: { path: '**', component: NaoEncontradaComponent }. */
@Component({
  selector: 'app-nao-encontrada',
  imports: [RouterLink],
  templateUrl: './nao-encontrada.component.html',
  styleUrl: './nao-encontrada.component.scss'
})
export class NaoEncontradaComponent {}
