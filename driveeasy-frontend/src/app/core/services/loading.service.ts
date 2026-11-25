import { Injectable, computed, signal } from '@angular/core';

/** Compteur de requetes HTTP en cours (alimente la barre de chargement). */
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly pending = signal(0);
  readonly busy = computed(() => this.pending() > 0);

  start(): void { this.pending.update((n) => n + 1); }
  stop(): void { this.pending.update((n) => Math.max(0, n - 1)); }
}
