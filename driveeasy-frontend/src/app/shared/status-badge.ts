import { Component, input } from '@angular/core';

/** Badge colore pour les statuts (vehicule / location) et categories client. */
@Component({
  selector: 'app-badge',
  template: `<span class="badge" [class]="'badge ' + value().toLowerCase()">{{ label() || value() }}</span>`,
})
export class StatusBadge {
  readonly value = input.required<string>();
  readonly label = input<string>('');
}
