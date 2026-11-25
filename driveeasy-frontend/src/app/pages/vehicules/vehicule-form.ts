import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HasUnsavedChanges } from '../../core/guards/unsaved-changes.guard';
import { VehiculeRequest } from '../../core/models/models';
import { ToastService } from '../../core/services/toast.service';
import { VehiculeService } from '../../core/services/vehicule.service';

@Component({
  selector: 'app-vehicule-form',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <header class="page-head">
      <div><h1>{{ id() ? 'Modifier le vehicule' : 'Nouveau vehicule' }}</h1></div>
      <a class="btn" routerLink="/vehicules">Retour au catalogue</a>
    </header>

    <form class="card form-grid" [formGroup]="form" (ngSubmit)="enregistrer()">
      <label>Immatriculation *
        <input formControlName="immatriculation" placeholder="123 TUN 4567" />
        @if (invalid('immatriculation')) { <small class="err">Format attendu : 123 TUN 4567</small> }
      </label>
      <label>Marque *
        <input formControlName="marque" />
        @if (invalid('marque')) { <small class="err">Obligatoire</small> }
      </label>
      <label>Modele *
        <input formControlName="modele" />
        @if (invalid('modele')) { <small class="err">Obligatoire</small> }
      </label>
      <label>Annee *
        <input type="number" formControlName="annee" />
        @if (invalid('annee')) { <small class="err">Entre 1990 et {{ anneeCourante }}</small> }
      </label>
      <label>Couleur <input formControlName="couleur" /></label>
      <label>Categorie
        <select formControlName="categorie"><option>ECONOMIQUE</option><option>BERLINE</option><option>SUV</option><option>LUXE</option></select>
      </label>
      <label>Carburant
        <select formControlName="carburant"><option>ESSENCE</option><option>DIESEL</option><option>HYBRIDE</option><option>ELECTRIQUE</option></select>
      </label>
      <label>Boite
        <select formControlName="boite"><option>MANUELLE</option><option>AUTOMATIQUE</option></select>
      </label>
      <label>Nombre de places *
        <input type="number" formControlName="nbPlaces" />
        @if (invalid('nbPlaces')) { <small class="err">Entre 1 et 9</small> }
      </label>
      <label>Kilometrage *
        <input type="number" formControlName="kilometrage" />
        @if (invalid('kilometrage')) { <small class="err">Doit etre positif</small> }
      </label>
      <label>Prix journalier (TND) *
        <input type="number" step="0.5" formControlName="prixJournalier" />
        @if (invalid('prixJournalier')) { <small class="err">Doit etre strictement positif</small> }
      </label>
      <label>Statut
        <select formControlName="statut">
          <option>DISPONIBLE</option><option>MAINTENANCE</option><option disabled>LOUE</option>
        </select>
        @if (form.controls.statut.disabled) { <small class="muted">Gere automatiquement par le cycle de location.</small> }
      </label>
      <label class="span-2">URL de l'image <input formControlName="imageUrl" placeholder="https://..." /></label>

      <div class="span-2 actions">
        <button class="btn primary" type="submit" [disabled]="saving()">{{ id() ? 'Enregistrer' : 'Creer le vehicule' }}</button>
        <a class="btn" routerLink="/vehicules">Annuler</a>
      </div>
    </form>
  `,
})
export class VehiculeForm implements OnInit, HasUnsavedChanges {
  /** Parametre de route :id (withComponentInputBinding). */
  readonly id = input<string>();

  private readonly fb = inject(FormBuilder);
  private readonly service = inject(VehiculeService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  protected readonly anneeCourante = new Date().getFullYear();
  protected readonly saving = signal(false);
  private saved = false;

  protected readonly form = this.fb.nonNullable.group({
    immatriculation: ['', [Validators.required, Validators.pattern(/^\d{1,3} ?TUN ?\d{1,4}$/i)]],
    marque: ['', Validators.required],
    modele: ['', Validators.required],
    annee: [this.anneeCourante, [Validators.required, Validators.min(1990), Validators.max(this.anneeCourante)]],
    couleur: [''],
    categorie: ['ECONOMIQUE' as VehiculeRequest['categorie']],
    carburant: ['ESSENCE' as VehiculeRequest['carburant']],
    boite: ['MANUELLE' as VehiculeRequest['boite']],
    nbPlaces: [5, [Validators.required, Validators.min(1), Validators.max(9)]],
    kilometrage: [0, [Validators.required, Validators.min(0)]],
    prixJournalier: [0, [Validators.required, Validators.min(0.01)]],
    statut: ['DISPONIBLE' as VehiculeRequest['statut']],
    imageUrl: [''],
  });

  ngOnInit(): void {
    const id = this.id();
    if (!id) return;
    this.service.get(id).subscribe((v) => {
      this.form.patchValue({ ...v, couleur: v.couleur ?? '', imageUrl: v.imageUrl ?? '' });
      if (v.statut === 'LOUE') this.form.controls.statut.disable();
    });
  }

  hasUnsavedChanges(): boolean {
    return this.form.dirty && !this.saved;
  }

  protected invalid(name: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[name];
    return c.invalid && (c.touched || c.dirty);
  }

  protected enregistrer(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    const body: VehiculeRequest = { ...raw, couleur: raw.couleur || null, imageUrl: raw.imageUrl || null };
    const id = this.id();
    this.saving.set(true);
    const call = id ? this.service.update(id, body) : this.service.create(body);
    call.subscribe({
      next: () => {
        this.saved = true;
        this.toast.success(id ? 'Vehicule modifie' : 'Vehicule cree');
        this.router.navigateByUrl('/vehicules');
      },
      error: () => this.saving.set(false),
    });
  }
}
