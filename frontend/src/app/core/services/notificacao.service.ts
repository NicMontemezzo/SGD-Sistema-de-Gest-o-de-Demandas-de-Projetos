import { Injectable } from '@angular/core';
import Swal from 'sweetalert2';

/** Mensagens para o usuário com SweetAlert2. Use sempre este service, nunca o Swal direto. */
@Injectable({ providedIn: 'root' })
export class NotificacaoService {
  private readonly corConfirmar = '#3b71ca';
  private readonly corCancelar = '#9fa6b2';
  private readonly corPerigo = '#dc4c64';

  /** Toast discreto no canto da tela, que some sozinho. */
  sucesso(mensagem: string): void {
    Swal.fire({
      toast: true,
      position: 'top-end',
      icon: 'success',
      title: mensagem,
      showConfirmButton: false,
      timer: 3000,
      timerProgressBar: true
    });
  }

  /** Erro com lista opcional de detalhes (ex.: campos inválidos de um 400). */
  erro(mensagem: string, detalhes: string[] = [], titulo = 'Ops!'): void {
    Swal.fire({
      icon: 'error',
      title: titulo,
      html: this.montarHtml(mensagem, detalhes),
      confirmButtonText: 'OK',
      confirmButtonColor: this.corConfirmar
    });
  }

  aviso(mensagem: string, titulo = 'Atenção'): void {
    Swal.fire({
      icon: 'warning',
      title: titulo,
      text: mensagem,
      confirmButtonText: 'OK',
      confirmButtonColor: this.corConfirmar
    });
  }

  /**
   * Pergunta de confirmação. Resolve true se o usuário confirmar.
   * Ex.: if (await this.notificacao.confirmar('Excluir demanda?', 'Esta ação não pode ser desfeita.')) { ... }
   */
  async confirmar(titulo: string, texto = '', textoConfirmar = 'Sim, confirmar'): Promise<boolean> {
    const resultado = await Swal.fire({
      icon: 'question',
      title: titulo,
      text: texto,
      showCancelButton: true,
      confirmButtonText: textoConfirmar,
      cancelButtonText: 'Cancelar',
      confirmButtonColor: this.corPerigo,
      cancelButtonColor: this.corCancelar,
      reverseButtons: true,
      focusCancel: true
    });
    return resultado.isConfirmed;
  }

  private montarHtml(mensagem: string, detalhes: string[]): string {
    const texto = this.escapar(mensagem);
    if (detalhes.length === 0) {
      return texto;
    }
    const itens = detalhes.map((detalhe) => `<li>${this.escapar(detalhe)}</li>`).join('');
    return `${texto}<ul class="text-start mt-3 mb-0">${itens}</ul>`;
  }

  /** Evita que texto vindo da API seja interpretado como HTML. */
  private escapar(texto: string): string {
    const div = document.createElement('div');
    div.textContent = texto;
    return div.innerHTML;
  }
}
