import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Client, Eligibilite, Location, Vehicule } from '../../core/models/models';
import { ClientService } from '../../core/services/client.service';
import { LocationService } from '../../core/services/location.service';
import { VehiculeService } from '../../core/services/vehicule.service';
import { todayIso } from '../../core/utils/date-utils';
import { StatusBadge } from '../../shared/status-badge';

@Component({
  selector: 'app-client-detail',
  imports: [RouterLink, DatePipe, DecimalPipe, StatusBadge],
  template: `
    <header class="page-head">
      <div>
        <h1>{{ client()?.prenom }} {{ client()?.nom }}</h1>
        @if (client(); as c) { <p class="muted">Client depuis le {{ c.dateInscription | date: 'dd/MM/yyyy' }}</p> }
      </div>
      <a class="btn" routerLink="/clients">Retour a la liste</a>
    </header>

    @if (client(); as c) {
      <section class="grid-2">
        <div class="card">
          <h2>Informations</h2>
          <dl class="info">
            <dt>CIN</dt><dd>{{ c.cin }}</dd>
            <dt>Date de naissance</dt><dd>{{ c.dateNaissance | date: 'dd/MM/yyyy' }}</dd>
            <dt>Email</dt><dd>{{ c.email }}</dd>
            <dt>Telephone</dt><dd>{{ c.telephone || '-' }}</dd>
            <dt>Adresse</dt><dd>{{ c.adresse || '-' }}</dd>
            <dt>N° de permis</dt><dd>{{ c.numeroPermis }}</dd>
            <dt>Expiration du permis</dt>
            <dd>{{ c.dateExpirationPermis | date: 'dd/MM/yyyy' }}
              @if (c.dateExpirationPermis < today) { <span class="alert-chip">Expire</span> }</dd>
            <dt>Categorie</dt><dd><app-badge [value]="c.categorieClient" /></dd>
          </dl>
        </div>

        <div class="card">
          <h2>Eligibilite a la location</h2>
          @if (elig(); as e) {
            <div class="elig" [class.ok]="e.eligible" [class.ko]="!e.eligible">
              {{ e.eligible ? 'Client eligible' : 'Client non eligible' }}
            </div>
            <p class="muted">Age : {{ e.age }} ans &middot; Permis {{ e.permisValide ? 'valide' : 'expire' }}</p>
            @if (e.motifs.length) { <ul class="motifs">@for (m of e.motifs; track m) { <li>{{ m }}</li> }</ul> }
          }
        </div>
      </section>

      <section class="card table-card">
        <h2>Historique des locations</h2>
        <table>
          <thead><tr><th>#</th><th>Vehicule</th><th>Periode</th><th>Statut</th><th class="right">Montant</th></tr></thead>
          <tbody>
            @for (l of historique(); track l.id) {
              <tr>
                <td>{{ l.id }}</td>
                <td>{{ vehiculeLabel(l.vehiculeId) }}</td>
                <td>{{ l.dateDebut | date: 'dd/MM/yyyy' }} &rarr; {{ l.dateFinPrevue | date: 'dd/MM/yyyy' }}</td>
                <td><app-badge [value]="l.statut" /></td>
                <td class="right">{{ l.montantTotal | number: '1.2-2' }} TND</td>
              </tr>
            } @empty {
              <tr><td colspan="5" class="empty">Aucune location pour ce client.</td></tr>
            }
          </tbody>
        </table>
      </section>
    }
  `,
})
export class ClientDetail implements OnInit {
  readonly id = input.required<string>();

  private readonly clients = inject(ClientService);
  private readonly locations = inject(LocationService);
  private readonly vehicules = inject(VehiculeService);

  protected readonly today = todayIso();
  protected readonly client = signal<Client | null>(null);
  protected readonly elig = signal<Eligibilite | null>(null);
  protected readonly historique = signal<Location[]>([]);
  private readonly parc = signal<Vehicule[]>([]);

  ngOnInit(): void {
    const id = this.id();
    this.clients.get(id).subscribe((c) => this.client.set(c));
    this.clients.eligibilite(id).subscribe((e) => this.elig.set(e));
    this.locations.list({ clientId: Number(id), size: 100 }).subscribe((p) => this.historique.set(p.content));
    this.vehicules.list().subscribe((v) => this.parc.set(v));
  }

  protected vehiculeLabel(id: number): string {
    const v = this.parc().find((x) => x.id === id);
    return v ? `${v.marque} ${v.modele} (${v.immatriculation})` : `Vehicule #${id}`;
  }
}
