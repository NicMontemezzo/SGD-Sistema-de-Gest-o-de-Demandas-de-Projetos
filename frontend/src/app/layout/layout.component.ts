import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/services/auth.service';

/** Navbar (MDB) com o usuário logado e o botão Sair. As páginas de demandas abrem no <router-outlet>. */
@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss'
})
export class LayoutComponent {
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);

  // nome e perfil gravados pelo auth.service no login
  readonly nome = this.auth.nome() ?? '';
  readonly perfil = this.auth.perfil() === 'ADMIN' ? 'Administrador' : 'Aluno';

  sair(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
