import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

/** Navbar (MDB) com o usuário logado e o botão Sair. As páginas de demandas abrem no <router-outlet>. */
@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss'
})
export class LayoutComponent {
  private readonly router = inject(Router);

  // nome e perfil vêm do token pelo auth.service (Integrante 1)
  nome = '';
  perfil = '';

  sair(): void {
    // auth.service.logout() (Integrante 1) entra aqui para apagar o token
    this.router.navigate(['/login']);
  }
}
