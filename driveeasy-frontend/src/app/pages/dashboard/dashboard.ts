import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Statistiques, StatutLocation, Vehicule } from '../../core/models/models';
import { LocationService } from '../../core/services/location.service';
import { VehiculeService } from '../../core/services/vehicule.service';

const STATUTS_LOCATION: { key: StatutLocation; label: string; color: string }[] = [
  { key: 'RESERVEE', label: 'Reservees', color: 'var(--info)' },
  { key: 'EN_COURS', label: 'En cours', color: 'var(--warn)' },
  { key: 'TERMINEE', label: 'Terminees', color: 'var(--ok)' },
  { key: 'ANNULEE', label: 'Annulees', color: 'var(--muted)' },
];

@Component({
  selector: 'app-dashboard',
  imports: [CurrencyPipe, DecimalPipe, RouterLink],
  template: `
    <header class="page-head">
      <div>
        <h1>Tableau de bord</h1>
        <p class="muted">Indicateurs du parc et des locations (mois en cours)</p>
      </div>
      <a class="btn primary" routerLink="/locations/new">+ Nouvelle location</a>
    </header>

    @if (loadFailed()) {
      <div class="card empty">Donnees indisponibles : verifiez que l'API Gateway et les services sont demarres.</div>
    } @else {
      <section class="kpis">
        <div class="kpi ok"><span>Vehicules disponibles</span><strong>{{ counts().DISPONIBLE }}</strong></div>
        <div class="kpi warn"><span>Vehicules loues</span><strong>{{ counts().LOUE }}</strong></div>
        <div class="kpi danger"><span>En maintenance</span><strong>{{ counts().MAINTENANCE }}</strong></div>
        <div class="kpi blue"><span>Locations en cours</span><strong>{{ enCours() }}</strong></div>
        <div class="kpi accent">
          <span>Chiffre d'affaires du mois</span>
          <strong>{{ (stats()?.chiffreAffaires ?? 0) | currency: 'TND' : 'symbol' : '1.2-2' }}</strong>
        </div>
      </section>

      <section class="grid-2">
        <div class="card">
          <h2>Etat du parc</h2>
          <div class="donut-wrap">
            <div class="donut" [style.background]="donut()"><div class="donut-hole">{{ total() }}<small>vehicules</small></div></div>
            <ul class="legend">
              <li><i style="background: var(--ok)"></i> Disponibles <b>{{ counts().DISPONIBLE }}</b></li>
              <li><i style="background: var(--warn)"></i> Loues <b>{{ counts().LOUE }}</b></li>
              <li><i style="background: var(--danger)"></i> Maintenance <b>{{ counts().MAINTENANCE }}</b></li>
            </ul>
          </div>
        </div>

        <div class="card">
          <h2>Locations par statut</h2>
          @for (s of barres(); track s.key) {
            <div class="bar-row">
              <span class="bar-label">{{ s.label }}</span>
              <div class="bar-track"><div class="bar-fill" [style.width.%]="s.pct" [style.background]="s.color"></div></div>
              <b>{{ s.value }}</b>
            </div>
          }
          <h2 class="mt">Taux d'occupation du parc</h2>
          <div class="bar-row">
            <div class="bar-track tall"><div class="bar-fill" [style.width.%]="stats()?.tauxOccupation ?? 0" style="background: var(--accent)"></div></div>
            <b>{{ stats()?.tauxOccupation ?? 0 | number: '1.0-1' }} %</b>
          </div>
        </div>
      </section>

      <section class="card">
        <h2>Vehicules les plus loues</h2>
        @if (stats()?.topVehicules?.length) {
          @for (t of stats()!.topVehicules; track t.vehiculeId) {
            <div class="bar-row">
              <span class="bar-label wide">{{ t.libelle }}</span>
              <div class="bar-track"><div class="bar-fill" [style.width.%]="(t.nombreLocations / maxTop()) * 100" style="background: var(--accent)"></div></div>
              <b>{{ t.nombreLocations }}</b>
            </div>
          }
        } @else {
          <p class="muted">Aucune location enregistree pour le moment.</p>
        }
      </section>
    }
  `,
})
export class Dashboard {
  private readonly vehicules = inject(VehiculeService);
  private readonly locations = inject(LocationService);

  protected readonly stats = signal<Statistiques | null>(null);
  protected readonly parc = signal<Vehicule[]>([]);
  protected readonly loadFailed = signal(false);

  protected readonly counts = computed(() => {
    const c = { DISPONIBLE: 0, LOUE: 0, MAINTENANCE: 0 };
    for (const v of this.parc()) c[v.statut]++;
    return c;
  });
  protected readonly total = computed(() => this.parc().length);
  protected readonly enCours = computed(() => this.stats()?.locationsParStatut?.EN_COURS ?? 0);
  protected readonly maxTop = computed(() => Math.max(1, ...(this.stats()?.topVehicules.map((t) => t.nombreLocations) ?? [1])));

  /** Donut en CSS pur (conic-gradient). */
  protected readonly donut = computed(() => {
    const t = this.total();
    if (!t) return 'var(--line)';
    const a = (this.counts().DISPONIBLE / t) * 100;
    const b = a + (this.counts().LOUE / t) * 100;
    return `conic-gradient(var(--ok) 0 ${a}%, var(--warn) ${a}% ${b}%, var(--danger) ${b}% 100%)`;
  });

  protected readonly barres = computed(() => {
    const par = this.stats()?.locationsParStatut ?? {};
    const max = Math.max(1, ...STATUTS_LOCATION.map((s) => par[s.key] ?? 0));
    return STATUTS_LOCATION.map((s) => ({ ...s, value: par[s.key] ?? 0, pct: ((par[s.key] ?? 0) / max) * 100 }));
  });

  constructor() {
    forkJoin({ v: this.vehicules.list(), s: this.locations.stats() }).subscribe({
      next: ({ v, s }) => { this.parc.set(v); this.stats.set(s); },
      error: () => this.loadFailed.set(true),
    });
  }
}
