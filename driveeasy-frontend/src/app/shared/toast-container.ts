import { Component, inject } from '@angular/core';
import { ToastService } from '../core/services/toast.service';

@Component({
  selector: 'app-toast-container',
  template: `
    <div class="toasts no-print">
      @for (t of toasts.toasts(); track t.id) {
        <div class="toast" [class]="'toast ' + t.type" (click)="toasts.dismiss(t.id)">{{ t.message }}</div>
      }
    </div>
  `,
})
export class ToastContainer {
  protected readonly toasts = inject(ToastService);
}
