import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Location, LocationDetail } from '../../core/models/models';
import { LocationService } from '../../core/services/location.service';
import { ToastService } from '../../core/services/toast.service';
import { diffDays, todayIso } from '../../core/utils/date-utils';

const MULTIPLICATEUR_PENALITE = 1.5;

/** Retour de vehicule : saisie du kilometrage, calcul des penalites, recapitulatif imprimable. */
@Component({
  selector: 'app-location-retour',
  imports: [RouterLink, DatePipe, DecimalPipe],
  template: `
    <header class="page-head no-print">
      <div><h1>Retour du vehicule - location #{{ id() }}</h1></div>
      <a class="btn" routerLink="/locations">Retour aux locations</a>
    </header>

    @if (detail(); as d) {
      @if (!resultat()) {
        <section class="grid-2 no-print">
          <div class="card">
            <h2>Location</h2>
            <dl class="info">
              <dt>Client</dt><dd>{{ d.client?.prenom }} {{ d.client?.nom }}</dd>
              <dt>Vehicule</dt><dd>{{ d.vehicule?.marque }} {{ d.vehicule?.modele }} ({{ d.vehicule?.immatriculation }})</dd>
              <dt>Periode prevue</dt><dd>{{ d.location.dateDebut | date: 'dd/MM/yyyy' }} &rarr; {{ d.location.dateFinPrevue | date: 'dd/MM/yyyy' }}</dd>
              <dt>Km au depart</dt><dd>{{ d.location.kmDepart | number }} km</dd>
              <dt>Montant initial</dt><dd>{{ d.location.montantTotal | number: '1.2-2' }} TND</dd>
            </dl>
          </div>

          <form class="card" (submit)="$event.preventDefault(); valider()">
            <h2>Retour</h2>
            <label>Kilometrage au retour
              <input type="number" [min]="d.location.kmDepart ?? 0" [value]="km() ?? ''" (input)="km.set($any($event.target).valueAsNumber || null)" />
            </label>
            @if (kmInvalide()) { <small class="err">Doit etre superieur ou egal a {{ d.location.kmDepart }} km</small> }
            <label>Date de retour effective
              <input type="date" [min]="d.location.dateDebut" [value]="dateRetour()" (change)="dateRetour.set($any($event.target).value)" />
            </label>

            <div class="preview">
              <div><span>Jours de retard</span><b>{{ retard() }}</b></div>
              <div><span>Penalite (1,5 x prix journalier x retard)</span><b [class.err]="penalite() > 0">{{ penalite() | number: '1.2-2' }} TND</b></div>
              <div class="total"><span>Total a payer</span><b>{{ d.location.montantTotal + penalite() | number: '1.2-2' }} TND</b></div>
            </div>
            <button class="btn primary" type="submit" [disabled]="saving() || km() === null || kmInvalide()">Valider le retour</button>
          </form>
        </section>
      } @else {
        <section class="card recap-print">
          <h2>Recapitulatif final - location #{{ resultat()!.id }}</h2>
          <dl class="info recap">
            <dt>Client</dt><dd>{{ d.client?.prenom }} {{ d.client?.nom }} (CIN {{ d.client?.cin }})</dd>
            <dt>Vehicule</dt><dd>{{ d.vehicule?.marque }} {{ d.vehicule?.modele }} - {{ d.vehicule?.immatriculation }}</dd>
            <dt>Periode prevue</dt><dd>{{ resultat()!.dateDebut | date: 'dd/MM/yyyy' }} &rarr; {{ resultat()!.dateFinPrevue | date: 'dd/MM/yyyy' }}</dd>
            <dt>Retour effectif</dt><dd>{{ resultat()!.dateRetourEffectif | date: 'dd/MM/yyyy' }}</dd>
            <dt>Kilometrage</dt><dd>{{ resultat()!.kmDepart | number }} &rarr; {{ resultat()!.kmRetour | number }} km ({{ (resultat()!.kmRetour ?? 0) - (resultat()!.kmDepart ?? 0) | number }} km parcourus)</dd>
            <dt>Montant de la location</dt><dd>{{ resultat()!.montantTotal - resultat()!.penalite | number: '1.2-2' }} TND</dd>
            <dt>Penalite de retard</dt><dd>{{ resultat()!.penalite | number: '1.2-2' }} TND</dd>
            <dt class="total">Total a payer</dt><dd class="total">{{ resultat()!.montantTotal | number: '1.2-2' }} TND</dd>
          </dl>
          <div class="actions no-print">
            <button class="btn primary" (click)="imprimer()">Imprimer le recapitulatif</button>
            <a class="btn" routerLink="/locations">Terminer</a>
          </div>
        </section>
      }
    }
  `,
})
export class LocationRetour implements OnInit {
  readonly id = input.required<string>();

  private readonly service = inject(LocationService);
  private readonly toast = inject(ToastService);

  protected readonly detail = signal<LocationDetail | null>(null);
  protected readonly km = signal<number | null>(null);
  protected readonly dateRetour = signal(todayIso());
  protected readonly saving = signal(false);
  protected readonly resultat = signal<Location | null>(null);

  protected readonly retard = computed(() => {
    const l = this.detail()?.location;
    return l ? Math.max(0, diffDays(l.dateFinPrevue, this.dateRetour())) : 0;
  });
  protected readonly penalite = computed(() => {
    const l = this.detail()?.location;
    return l ? Math.round(this.retard() * l.prixJournalierApplique * MULTIPLICATEUR_PENALITE * 100) / 100 : 0;
  });
  protected readonly kmInvalide = computed(() => {
    const l = this.detail()?.location;
    const km = this.km();
    return km !== null && !!l && km < (l.kmDepart ?? 0);
  });

  ngOnInit(): void {
    this.service.get(this.id()).subscribe((d) => this.detail.set(d));
  }

  protected valider(): void {
    if (this.km() === null || this.kmInvalide()) return;
    this.saving.set(true);
    this.service.retour(this.id(), { kmRetour: this.km()!, dateRetourEffectif: this.dateRetour() }).subscribe({
      next: (l) => {
        this.resultat.set(l);
        this.toast.success('Retour enregistre');
        this.saving.set(false);
      },
      error: () => this.saving.set(false),
    });
  }

  protected imprimer(): void {
    window.print();
  }
}
