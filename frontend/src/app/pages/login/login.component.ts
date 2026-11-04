import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MdbFormsModule } from 'mdb-angular-ui-kit/forms';
import { AuthService } from '../../core/services/auth.service';

const ROTA_PADRAO = '/demandas';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, MdbFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly form = this.fb.nonNullable.group({
    login: ['', [Validators.required]],
    senha: ['', [Validators.required]]
  });

  enviando = false;

  ngOnInit(): void {
    // Quem já está logado não precisa ver o login de novo
    if (this.auth.autenticado()) {
      this.router.navigateByUrl(this.destino());
    }
  }

  entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando = true;
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigateByUrl(this.destino());
      },
      // a mensagem de erro (401, 503...) já é mostrada pelo errorInterceptor
      error: () => {
        this.enviando = false;
      }
    });
  }

  /** Volta para a página onde a sessão expirou (?retorno=...). Só aceita rotas internas do próprio app. */
  private destino(): string {
    const retorno = this.route.snapshot.queryParamMap.get('retorno');
    const interno = !!retorno && retorno.startsWith('/') && !retorno.startsWith('//') && !retorno.startsWith('/login');
    return interno ? retorno : ROTA_PADRAO;
  }
}
