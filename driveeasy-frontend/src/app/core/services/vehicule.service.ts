import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api';
import { StatutVehicule, Vehicule, VehiculeFilters, VehiculeRequest } from '../models/models';

@Injectable({ providedIn: 'root' })
export class VehiculeService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/vehicules`;

  list(filters: VehiculeFilters = {}): Observable<Vehicule[]> {
    let params = new HttpParams();
    if (filters.categorie) params = params.set('categorie', filters.categorie);
    if (filters.carburant) params = params.set('carburant', filters.carburant);
    if (filters.statut) params = params.set('statut', filters.statut);
    if (filters.prixMax != null && filters.prixMax > 0) params = params.set('prixMax', filters.prixMax);
    return this.http.get<Vehicule[]>(this.base, { params });
  }

  get(id: number | string): Observable<Vehicule> {
    return this.http.get<Vehicule>(`${this.base}/${id}`);
  }

  create(v: VehiculeRequest): Observable<Vehicule> {
    return this.http.post<Vehicule>(this.base, v);
  }

  update(id: number | string, v: VehiculeRequest): Observable<Vehicule> {
    return this.http.put<Vehicule>(`${this.base}/${id}`, v);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  changeStatut(id: number, statut: StatutVehicule): Observable<Vehicule> {
    return this.http.patch<Vehicule>(`${this.base}/${id}/statut`, { statut });
  }
}
