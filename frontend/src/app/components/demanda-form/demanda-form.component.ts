import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MdbModalRef } from 'mdb-angular-ui-kit/modal';
import { Demanda, DemandaRequest } from '../../models/demanda.model';
import { Empresa } from '../../models/empresa.model';
import { DemandaService } from '../../core/services/demanda.service';
import { EmpresaService } from '../../core/services/empresa.service'; // Integrante 3

@Component({
  selector: 'app-demanda-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './demanda-form.component.html'
})
export class DemandaFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly demandaService = inject(DemandaService);
  private readonly empresaService = inject(EmpresaService);
  readonly modalRef = inject<MdbModalRef<DemandaFormComponent>>(MdbModalRef);

  // preenchido pelo modalService.open(DemandaFormComponent, { data: { demanda } })
  demanda: Demanda | null = null;

  empresas: Empresa[] = [];
  salvando = false;
  erro = '';

  form = this.fb.nonNullable.group({
    titulo: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(120)]],
    descricao: ['', [Validators.required, Validators.maxLength(2000)]],
    tecnologias: [''],
    empresaId: [null as number | null, Validators.required],
    situacao: ['DISPONIVEL' as Demanda['situacao']]
  });

  get editando(): boolean {
    return !!this.demanda;
  }

  ngOnInit(): void {
    this.empresaService.listar().subscribe(lista => (this.empresas = lista));

    if (this.demanda) {
      this.form.patchValue({
        titulo: this.demanda.titulo,
        descricao: this.demanda.descricao,
        tecnologias: this.demanda.tecnologias ?? '',
        empresaId: this.demanda.empresaId,
        situacao: this.demanda.situacao
      });
      if (this.demanda.situacao === 'ENCERRADA') {
        this.form.disable();
        this.erro = 'Demanda encerrada não pode ser editada.';
      }
    }
  }

  invalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!c && c.invalid && (c.dirty || c.touched);
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.salvando = true;
    this.erro = '';

    const v = this.form.getRawValue();
    const dados: DemandaRequest = {
      titulo: v.titulo.trim(),
      descricao: v.descricao.trim(),
      tecnologias: v.tecnologias?.trim() || undefined,
      empresaId: v.empresaId as number,
      situacao: v.situacao,
      versao: this.demanda?.versao
    };

    const chamada = this.demanda
      ? this.demandaService.atualizar(this.demanda.id, dados)
      : this.demandaService.criar(dados);

    chamada.subscribe({
      next: salva => this.modalRef.close(salva),
      error: e => {
        this.salvando = false;
        this.erro = e?.error?.details?.join(' · ') ?? e?.error?.message ?? 'Erro ao salvar a demanda.';
      }
    });
  }

  cancelar(): void {
    this.modalRef.close();
  }
}
