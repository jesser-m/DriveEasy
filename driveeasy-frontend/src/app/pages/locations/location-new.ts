import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Client, Eligibilite, PriceDetail, Vehicule } from '../../core/models/models';
import { ClientService } from '../../core/services/client.service';
import { LocationService } from '../../core/services/location.service';
import { ToastService } from '../../core/services/toast.service';
import { addDays, diffDays, todayIso } from '../../core/utils/date-utils';
import { StatusBadge } from '../../shared/status-badge';

const ETAPES = ['Client', 'Periode', 'Vehicule', 'Simulation et confirmation'];

/** Assistant en 4 etapes : client -> periode -> vehicule disponible -> simulation du prix -> confirmation. */
@Component({
  selector: 'app-location-new',
  imports: [RouterLink, DatePipe, DecimalPipe, StatusBadge],
  template: `
    <header class="page-head">
      <div><h1>Nouvelle location</h1></div>
      <a class="btn" routerLink="/locations">Annuler</a>
    </header>

    <ol class="stepper">
      @for (e of etapes; track e; let i = $index) {
        <li [class.active]="step() === i + 1" [class.done]="step() > i + 1"><span>{{ i + 1 }}</span>{{ e }}</li>
      }
    </ol>

    <section class="card">
      @switch (step()) {
        @case (1) {
          <h2>1. Choisir le client</h2>
          <input class="search" type="search" placeholder="Rechercher un client..." (input)="filtre.set($any($event.target).value)" />
          <div class="pick-list">
            @for (c of clientsFiltres(); track c.id) {
              <button type="button" class="pick" [class.selected]="client()?.id === c.id" (click)="choisirClient(c)">
                <strong>{{ c.prenom }} {{ c.nom }}</strong>
                <span class="muted">CIN {{ c.cin }}</span>
                <app-badge [value]="c.categorieClient" />
              </button>
            }
          </div>
          @if (elig(); as e) {
            <div class="elig" [class.ok]="e.eligible" [class.ko]="!e.eligible">
              {{ e.eligible ? 'Client eligible' : 'Client non eligible' }}
            </div>
            @if (e.motifs.length) { <ul class="motifs">@for (m of e.motifs; track m) { <li>{{ m }}</li> }</ul> }
          }
        }
        @case (2) {
          <h2>2. Choisir la periode</h2>
          <div class="form-grid">
            <label>Date de debut
              <input type="date" [min]="today" [value]="debut()" (change)="debut.set($any($event.target).value)" />
            </label>
            <label>Date de fin prevue
              <input type="date" [min]="debut()" [value]="fin()" (change)="fin.set($any($event.target).value)" />
            </label>
          </div>
          <p [class]="periodeValide() ? 'muted' : 'err'">
            {{ periodeValide() ? duree() + ' jour(s) de location' : 'La fin doit etre posterieure au debut (debut a partir d\\'aujourd\\'hui).' }}
          </p>
        }
        @case (3) {
          <h2>3. Choisir un vehicule disponible</h2>
          <p class="muted">Du {{ debut() | date: 'dd/MM/yyyy' }} au {{ fin() | date: 'dd/MM/yyyy' }}</p>
          <div class="pick-list">
            @for (v of dispos(); track v.id) {
              <button type="button" class="pick" [class.selected]="vehicule()?.id === v.id" (click)="vehicule.set(v)">
                <strong>{{ v.marque }} {{ v.modele }}</strong>
                <span class="muted">{{ v.categorie }} &middot; {{ v.carburant }} &middot; {{ v.boite }}</span>
                <span class="price">{{ v.prixJournalier | number: '1.0-2' }} TND / jour</span>
              </button>
            } @empty {
              <p class="empty">Aucun vehicule disponible sur cette periode.</p>
            }
          </div>
        }
        @case (4) {
          <h2>4. Simulation du prix et confirmation</h2>
          @if (prix(); as p) {
            <dl class="info recap">
              <dt>Client</dt><dd>{{ client()?.prenom }} {{ client()?.nom }} (<app-badge [value]="p.categorieClient" />)</dd>
              <dt>Vehicule</dt><dd>{{ vehicule()?.marque }} {{ vehicule()?.modele }} ({{ p.categorieVehicule }})</dd>
              <dt>Periode</dt><dd>{{ debut() | date: 'dd/MM/yyyy' }} &rarr; {{ fin() | date: 'dd/MM/yyyy' }} ({{ p.jours }} jours)</dd>
              <dt>Prix journalier</dt><dd>{{ p.prixJournalier | number: '1.2-2' }} TND</dd>
              <dt>Coefficient categorie</dt><dd>x {{ p.coefficientCategorie }}</dd>
              <dt>Sous-total</dt><dd>{{ p.sousTotal | number: '1.2-2' }} TND</dd>
              <dt>Remise fidelite ({{ p.remisePourcent }} %)</dt><dd>- {{ p.montantRemise | number: '1.2-2' }} TND</dd>
              <dt class="total">Montant total</dt><dd class="total">{{ p.montantTotal | number: '1.2-2' }} TND</dd>
            </dl>
          }
        }
      }

      <div class="actions wizard-actions">
        <button class="btn" [disabled]="step() === 1" (click)="precedent()">&laquo; Precedent</button>
        @if (step() < 4) {
          <button class="btn primary" [disabled]="!peutAvancer()" (click)="suivant()">Suivant &raquo;</button>
        } @else {
          <button class="btn primary" [disabled]="saving() || !prix()" (click)="confirmer()">Confirmer la reservation</button>
        }
      </div>
    </section>
  `,
})
export class LocationNew {
  private readonly clientsApi = inject(ClientService);
  private readonly locations = inject(LocationService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  protected readonly etapes = ETAPES;
  protected readonly today = todayIso();
  protected readonly step = signal(1);
  protected readonly saving = signal(false);

  protected readonly clients = signal<Client[]>([]);
  protected readonly filtre = signal('');
  protected readonly client = signal<Client | null>(null);
  protected readonly elig = signal<Eligibilite | null>(null);
  protected readonly debut = signal(addDays(todayIso(), 1));
  protected readonly fin = signal(addDays(todayIso(), 4));
  protected readonly dispos = signal<Vehicule[]>([]);
  protected readonly vehicule = signal<Vehicule | null>(null);
  protected readonly prix = signal<PriceDetail | null>(null);

  protected readonly clientsFiltres = computed(() => {
    const q = this.filtre().trim().toLowerCase();
    return this.clients().filter((c) => !q || `${c.prenom} ${c.nom} ${c.cin}`.toLowerCase().includes(q));
  });
  protected readonly duree = computed(() => diffDays(this.debut(), this.fin()));
  protected readonly periodeValide = computed(() => this.duree() >= 1 && this.debut() >= this.today);

  constructor() {
    this.clientsApi.list().subscribe((c) => this.clients.set(c));
  }

  protected choisirClient(c: Client): void {
    this.client.set(c);
    this.elig.set(null);
    this.clientsApi.eligibilite(c.id).subscribe((e) => this.elig.set(e));
  }

  protected peutAvancer(): boolean {
    switch (this.step()) {
      case 1: return !!this.client() && !!this.elig()?.eligible;
      case 2: return this.periodeValide();
      case 3: return !!this.vehicule();
      default: return false;
    }
  }

  protected suivant(): void {
    if (this.step() === 2) {
      this.vehicule.set(null);
      this.locations.disponibilites(this.debut(), this.fin()).subscribe((v) => {
        this.dispos.set(v);
        this.step.set(3);
      });
    } else if (this.step() === 3) {
      this.locations.simulate(this.requete()).subscribe((p) => {
        this.prix.set(p);
        this.step.set(4);
      });
    } else {
      this.step.update((s) => s + 1);
    }
  }

  protected precedent(): void {
    this.step.update((s) => Math.max(1, s - 1));
  }

  protected confirmer(): void {
    this.saving.set(true);
    this.locations.create(this.requete()).subscribe({
      next: (l) => {
        this.toast.success(`Reservation #${l.id} creee`);
        this.router.navigateByUrl('/locations');
      },
      error: () => this.saving.set(false),
    });
  }

  private requete() {
    return {
      clientId: this.client()!.id, vehiculeId: this.vehicule()!.id,
      dateDebut: this.debut(), dateFinPrevue: this.fin(),
    };
  }
}
