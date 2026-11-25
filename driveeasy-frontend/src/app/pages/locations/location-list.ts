import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Client, Location, Page, Vehicule } from '../../core/models/models';
import { ClientService } from '../../core/services/client.service';
import { LocationService } from '../../core/services/location.service';
import { ToastService } from '../../core/services/toast.service';
import { VehiculeService } from '../../core/services/vehicule.service';
import { Pagination } from '../../shared/pagination';
import { StatusBadge } from '../../shared/status-badge';

@Component({
  selector: 'app-location-list',
  imports: [RouterLink, DatePipe, DecimalPipe, StatusBadge, Pagination],
  template: `
    <header class="page-head">
      <div><h1>Locations</h1><p class="muted">{{ data()?.totalElements ?? 0 }} resultat(s)</p></div>
      <a class="btn primary" routerLink="/locations/new">+ Nouvelle location</a>
    </header>

    <section class="card filters">
      <label>Statut
        <select (change)="statut.set($any($event.target).value); chercher()">
          <option value="">Tous</option><option>RESERVEE</option><option>EN_COURS</option><option>TERMINEE</option><option>ANNULEE</option>
        </select>
      </label>
      <label>Periode du
        <input type="date" (change)="du.set($any($event.target).value); chercher()" />
      </label>
      <label>au
        <input type="date" (change)="au.set($any($event.target).value); chercher()" />
      </label>
    </section>

    <section class="card table-card">
      <table>
        <thead>
          <tr><th>#</th><th>Client</th><th>Vehicule</th><th>Periode</th><th>Statut</th><th class="right">Montant</th><th class="right">Actions</th></tr>
        </thead>
        <tbody>
          @for (l of data()?.content ?? []; track l.id) {
            <tr>
              <td>{{ l.id }}</td>
              <td>{{ clientNom(l.clientId) }}</td>
              <td>{{ vehiculeNom(l.vehiculeId) }}</td>
              <td>{{ l.dateDebut | date: 'dd/MM/yyyy' }} &rarr; {{ l.dateFinPrevue | date: 'dd/MM/yyyy' }}</td>
              <td><app-badge [value]="l.statut" /></td>
              <td class="right">
                {{ l.montantTotal | number: '1.2-2' }} TND
                @if (l.penalite > 0) { <small class="err"> (dont penalite {{ l.penalite | number: '1.2-2' }})</small> }
              </td>
              <td class="right actions-cell">
                @if (l.statut === 'RESERVEE') {
                  <button class="btn small primary" (click)="depart(l)">Depart</button>
                  <button class="btn small danger" (click)="annuler(l)">Annuler</button>
                }
                @if (l.statut === 'EN_COURS') {
                  <a class="btn small primary" [routerLink]="['/locations', l.id, 'retour']">Retour</a>
                }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="7" class="empty">Aucune location.</td></tr>
          }
        </tbody>
      </table>
    </section>
    <app-pagination [page]="page()" [totalPages]="data()?.totalPages ?? 0" (pageChange)="page.set($event); charger()" />
  `,
})
export class LocationList {
  private readonly service = inject(LocationService);
  private readonly clientsApi = inject(ClientService);
  private readonly vehiculesApi = inject(VehiculeService);
  private readonly toast = inject(ToastService);

  protected readonly data = signal<Page<Location> | null>(null);
  protected readonly page = signal(0);
  protected readonly statut = signal('');
  protected readonly du = signal('');
  protected readonly au = signal('');
  private readonly clients = signal<Client[]>([]);
  private readonly parc = signal<Vehicule[]>([]);

  constructor() {
    forkJoin({ c: this.clientsApi.list(), v: this.vehiculesApi.list() }).subscribe(({ c, v }) => {
      this.clients.set(c);
      this.parc.set(v);
    });
    this.charger();
  }

  protected chercher(): void {
    this.page.set(0);
    this.charger();
  }

  protected charger(): void {
    this.service.list({
      statut: this.statut() as never, dateDebut: this.du(), dateFin: this.au(), page: this.page(), size: 8,
    }).subscribe((p) => this.data.set(p));
  }

  protected clientNom(id: number): string {
    const c = this.clients().find((x) => x.id === id);
    return c ? `${c.prenom} ${c.nom}` : `Client #${id}`;
  }

  protected vehiculeNom(id: number): string {
    const v = this.parc().find((x) => x.id === id);
    return v ? `${v.marque} ${v.modele}` : `Vehicule #${id}`;
  }

  protected depart(l: Location): void {
    if (!confirm(`Demarrer la location #${l.id} ? Le vehicule passera a LOUE.`)) return;
    this.service.depart(l.id).subscribe(() => {
      this.toast.success(`Location #${l.id} demarree`);
      this.charger();
    });
  }

  protected annuler(l: Location): void {
    if (!confirm(`Annuler la reservation #${l.id} ?`)) return;
    this.service.annuler(l.id).subscribe(() => {
      this.toast.success(`Reservation #${l.id} annulee`);
      this.charger();
    });
  }
}
