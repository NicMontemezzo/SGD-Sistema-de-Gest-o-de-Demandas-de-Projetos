import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MdbModalService } from 'mdb-angular-ui-kit/modal';
import { Demanda, Pagina, SituacaoDemanda } from '../../models/demanda.model';
import { Empresa } from '../../models/empresa.model';
import { AuthService } from '../../core/services/auth.service';
import { DemandaService } from '../../core/services/demanda.service';
import { EmpresaService } from '../../core/services/empresa.service';
import { NotificacaoService } from '../../core/services/notificacao.service';
import { DemandaFormComponent } from '../demanda-form/demanda-form.component';

@Component({
  selector: 'app-demanda-lista',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './demanda-lista.component.html'
})
export class DemandaListaComponent implements OnInit {
  private readonly demandaService = inject(DemandaService);
  private readonly empresaService = inject(EmpresaService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly modalService = inject(MdbModalService);
  private readonly auth = inject(AuthService);

  readonly tamanhoPagina = 10;
  readonly situacoes: { valor: SituacaoDemanda; rotulo: string }[] = [
    { valor: 'DISPONIVEL', rotulo: 'Disponível' },
    { valor: 'ENCERRADA', rotulo: 'Encerrada' }
  ];

  // RN-01: só ADMIN vê os botões. Esconder é só conforto: o back-end continua barrando quem não é ADMIN (403).
  readonly ehAdmin = this.auth.isAdmin();

  empresas: Empresa[] = [];
  pagina: Pagina<Demanda> | null = null;
  carregando = false;

  filtroTitulo = '';
  filtroSituacao: SituacaoDemanda | '' = '';
  filtroEmpresaId: number | null = null;

  ngOnInit(): void {
    this.empresaService.listar().subscribe(lista => (this.empresas = lista));
    this.carregar(0);
  }

  carregar(numeroPagina: number): void {
    this.carregando = true;
    this.demandaService
      .listar(numeroPagina, this.tamanhoPagina, {
        titulo: this.filtroTitulo.trim(),
        situacao: this.filtroSituacao,
        empresaId: this.filtroEmpresaId ?? undefined
      })
      .subscribe({
        next: resposta => {
          this.pagina = resposta;
          this.carregando = false;
        },
        // o aviso ao usuário já vem do errorInterceptor
        error: () => (this.carregando = false)
      });
  }

  buscar(): void {
    this.carregar(0);
  }

  limparFiltros(): void {
    this.filtroTitulo = '';
    this.filtroSituacao = '';
    this.filtroEmpresaId = null;
    this.carregar(0);
  }

  irParaPagina(numeroPagina: number): void {
    if (!this.pagina || numeroPagina < 0 || numeroPagina >= this.pagina.totalPages) {
      return;
    }
    this.carregar(numeroPagina);
  }

  rotuloSituacao(situacao: SituacaoDemanda): string {
    return this.situacoes.find(s => s.valor === situacao)?.rotulo ?? situacao;
  }

  novo(): void {
    this.abrirFormulario(null);
  }

  editar(demanda: Demanda): void {
    this.abrirFormulario(demanda);
  }

  async excluir(demanda: Demanda): Promise<void> {
    const confirmado = await this.notificacao.confirmar(
      `Excluir "${demanda.titulo}"?`,
      'Esta ação não pode ser desfeita.',
      'Sim, excluir'
    );
    if (!confirmado) {
      return;
    }

    this.demandaService.excluir(demanda.id).subscribe({
      next: () => {
        this.notificacao.sucesso('Demanda excluída.');
        // se apagou o último item de uma página que não é a primeira, volta uma página
        const eraUltimoDaPagina = this.pagina?.content.length === 1 && this.pagina.number > 0;
        this.carregar(eraUltimoDaPagina ? this.pagina!.number - 1 : (this.pagina?.number ?? 0));
      },
      // 403, 404 etc. já são avisados pelo errorInterceptor
      error: () => this.carregar(this.pagina?.number ?? 0)
    });
  }

  private abrirFormulario(demanda: Demanda | null): void {
    const modalRef = this.modalService.open(DemandaFormComponent, {
      data: { demanda },
      modalClass: 'modal-lg'
    });
    modalRef.onClose.subscribe(salva => {
      if (salva) {
        this.notificacao.sucesso(demanda ? 'Demanda atualizada.' : 'Demanda criada.');
      }
      // recarrega sempre: mesmo cancelando, outra pessoa pode ter alterado a demanda (RN-04)
      this.carregar(this.pagina?.number ?? 0);
    });
  }
}
