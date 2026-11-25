import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Vehicule } from '../../core/models/models';
import { ToastService } from '../../core/services/toast.service';
import { VehiculeService } from '../../core/services/vehicule.service';
import { Pagination } from '../../shared/pagination';
import { StatusBadge } from '../../shared/status-badge';

type Tri = 'prixAsc' | 'prixDesc' | 'anneeDesc' | 'marque';
const PAGE_SIZE = 6;

@Component({
  selector: 'app-vehicule-list',
  imports: [RouterLink, DecimalPipe, StatusBadge, Pagination],
  template: `
    <header class="page-head">
      <div><h1>Catalogue des vehicules</h1><p class="muted">{{ sorted().length }} vehicule(s)</p></div>
      <a class="btn primary" routerLink="/vehicules/new">+ Ajouter un vehicule</a>
    </header>

    <section class="card filters">
      <label>Carburant
        <select (change)="carburant.set($any($event.target).value); charger()">
          <option value="">Tous</option><option>ESSENCE</option><option>DIESEL</option><option>HYBRIDE</option><option>ELECTRIQUE</option>
        </select>
      </label>
      <label>Categorie
        <select (change)="categorie.set($any($event.target).value); charger()">
          <option value="">Toutes</option><option>ECONOMIQUE</option><option>BERLINE</option><option>SUV</option><option>LUXE</option>
        </select>
      </label>
      <label>Statut
        <select (change)="statut.set($any($event.target).value); charger()">
          <option value="">Tous</option><option>DISPONIBLE</option><option>LOUE</option><option>MAINTENANCE</option>
        </select>
      </label>
      <label>Prix max / jour (TND)
        <input type="number" min="0" placeholder="ex. 150" (change)="prixMax.set($any($event.target).valueAsNumber || null); charger()" />
      </label>
      <label>Tri
        <select (change)="tri.set($any($event.target).value); page.set(0)">
          <option value="marque">Marque (A-Z)</option><option value="prixAsc">Prix croissant</option>
          <option value="prixDesc">Prix decroissant</option><option value="anneeDesc">Plus recents</option>
        </select>
      </label>
    </section>

    @if (pageItems().length === 0) {
      <div class="card empty">Aucun vehicule ne correspond aux filtres.</div>
    }
    <section class="cards">
      @for (v of pageItems(); track v.id) {
        <article class="vcard">
          <div class="vimg">
            @if (v.imageUrl) { <img [src]="v.imageUrl" [alt]="v.marque + ' ' + v.modele" loading="lazy" /> }
            <app-badge class="vstatus" [value]="v.statut" />
          </div>
          <div class="vbody">
            <h3>{{ v.marque }} {{ v.modele }}</h3>
            <p class="muted">{{ v.immatriculation }} &middot; {{ v.annee }} &middot; {{ v.couleur }}</p>
            <ul class="specs">
              <li>{{ v.carburant }}</li><li>{{ v.boite }}</li><li>{{ v.nbPlaces }} places</li><li>{{ v.categorie }}</li>
            </ul>
            <p class="muted">{{ v.kilometrage | number }} km</p>
            <div class="vfoot">
              <strong class="price">{{ v.prixJournalier | number: '1.0-2' }} TND<small> / jour</small></strong>
              <span>
                <a class="btn small" [routerLink]="['/vehicules', v.id, 'edit']">Modifier</a>
                <button class="btn small danger" (click)="supprimer(v)">Supprimer</button>
              </span>
            </div>
          </div>
        </article>
      }
    </section>
    <app-pagination [page]="page()" [totalPages]="totalPages()" (pageChange)="page.set($event)" />
  `,
})
export class VehiculeList {
  private readonly service = inject(VehiculeService);
  private readonly toast = inject(ToastService);

  protected readonly vehicules = signal<Vehicule[]>([]);
  protected readonly carburant = signal('');
  protected readonly categorie = signal('');
  protected readonly statut = signal('');
  protected readonly prixMax = signal<number | null>(null);
  protected readonly tri = signal<Tri>('marque');
  protected readonly page = signal(0);

  protected readonly sorted = computed(() => {
    const list = [...this.vehicules()];
    switch (this.tri()) {
      case 'prixAsc': return list.sort((a, b) => a.prixJournalier - b.prixJournalier);
      case 'prixDesc': return list.sort((a, b) => b.prixJournalier - a.prixJournalier);
      case 'anneeDesc': return list.sort((a, b) => b.annee - a.annee);
      default: return list.sort((a, b) => (a.marque + a.modele).localeCompare(b.marque + b.modele));
    }
  });
  protected readonly totalPages = computed(() => Math.ceil(this.sorted().length / PAGE_SIZE));
  protected readonly pageItems = computed(() => this.sorted().slice(this.page() * PAGE_SIZE, (this.page() + 1) * PAGE_SIZE));

  constructor() { this.charger(); }

  protected charger(): void {
    this.page.set(0);
    this.service.list({
      carburant: this.carburant() as never, categorie: this.categorie() as never,
      statut: this.statut() as never, prixMax: this.prixMax(),
    }).subscribe((v) => this.vehicules.set(v));
  }

  protected supprimer(v: Vehicule): void {
    if (!confirm(`Supprimer ${v.marque} ${v.modele} (${v.immatriculation}) ?`)) return;
    this.service.delete(v.id).subscribe(() => {
      this.toast.success('Vehicule supprime');
      this.charger();
    });
  }
}
